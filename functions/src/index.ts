import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.database();

/**
 * Triggered when a new event is created under /tripEvents/{tripId}/{eventId}
 */
export const onTripEventCreated = functions.database.ref("/tripEvents/{tripId}/{eventId}")
  .onCreate(async (snapshot, context) => {
    const tripId = context.params.tripId;
    const eventId = context.params.eventId;
    const eventData = snapshot.val();

    if (!eventData) {
      console.error(`No data for event ${eventId} in trip ${tripId}`);
      return null;
    }

    // 1. Idempotency Check
    const processedRef = db.ref(`/tripEventsProcessed/${eventId}`);
    const processedSnapshot = await processedRef.get();
    if (processedSnapshot.exists()) {
      console.log(`Event ${eventId} already processed. Skipping.`);
      return null;
    }

    const {
      type,
      actorUserId,
      actorName,
      title: eventTitle,
      body: eventBody,
    } = eventData;

    console.log(`Processing event ${type} for trip ${tripId} by actor ${actorUserId}`);

    // 2. Validate Trip and Members
    const tripRef = db.ref(`/trips/${tripId}`);
    const tripSnapshot = await tripRef.get();
    if (!tripSnapshot.exists()) {
      console.error(`Trip ${tripId} does not exist. Ignoring event.`);
      return null;
    }

    const trip = tripSnapshot.val();
    const members = trip.members || {};
    const tripName = trip.name || "Trip";

    if (!members[actorUserId]) {
      console.error(`Actor ${actorUserId} is not a member of trip ${tripId}. Ignoring.`);
      return null;
    }

    // 3. Resolve Recipients (Exclude Actor)
    const recipientUids = Object.keys(members).filter((uid) => uid !== actorUserId);
    if (recipientUids.length === 0) {
      console.log("No recipients to notify.");
      await markProcessed(eventId);
      return null;
    }

    // 4. Load FCM Tokens
    const tokens: string[] = [];
    const tokenPromises = recipientUids.map(async (uid) => {
      const userSnapshot = await db.ref(`/users/${uid}/fcmToken`).get();
      if (userSnapshot.exists()) {
        return {uid, token: userSnapshot.val()};
      }
      return null;
    });

    const results = await Promise.all(tokenPromises);
    const validResults = results.filter((res) => res !== null) as {uid: string, token: string}[];
    validResults.forEach((res) => tokens.push(res.token));

    if (tokens.length === 0) {
      console.log("No FCM tokens found for recipients.");
      await markProcessed(eventId);
      return null;
    }

    // 5. Construct Notification
    // Use data payload as expected by ShutUpFirebaseMessagingService
    const payload: admin.messaging.MessagingPayload = {
      data: {
        "type": "TRIP_TOGETHER",
        "tripId": tripId,
        "eventId": eventId,
        "notificationType": type,
        "actorUserId": actorUserId,
        "tripName": tripName,
        "title": eventTitle || "TripTogether Update",
        "body": eventBody || `${actorName} performed an action`,
      },
    };

    // 6. Send FCM
    try {
      const response = await admin.messaging().sendToDevice(tokens, payload);
      console.log(`Successfully sent ${response.successCount} messages.`);

      // 7. Handle Invalid Tokens
      if (response.failureCount > 0) {
        const cleanupPromises: Promise<any>[] = [];
        response.results.forEach((result, index) => {
          const error = result.error;
          if (error) {
            console.error(`Failure sending to ${recipientUids[index]}:`, error.code);
            if (error.code === "messaging/invalid-registration-token" ||
                error.code === "messaging/registration-token-not-registered") {
              const uid = validResults[index].uid;
              console.log(`Cleaning up invalid token for user ${uid}`);
              cleanupPromises.push(db.ref(`/users/${uid}/fcmToken`).remove());
            }
          }
        });
        await Promise.all(cleanupPromises);
      }
    } catch (error) {
      console.error("Error sending FCM:", error);
    }

    // 8. Mark Processed
    await markProcessed(eventId);
    return null;
  });

/**
 * Marks an event as processed for idempotency.
 */
async function markProcessed(eventId: string) {
  await db.ref(`/tripEventsProcessed/${eventId}`).set({
    processedAt: admin.database.ServerValue.TIMESTAMP,
  });
}
