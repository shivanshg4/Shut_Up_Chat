package com.chat.shutup.feature.call.manager

import android.content.Context
import android.view.SurfaceView
import android.view.ViewGroup
import dagger.hilt.android.qualifiers.ApplicationContext
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import javax.inject.Inject
import javax.inject.Singleton

/*
@Singleton
class AgoraCallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var rtcEngine: RtcEngine? = null
    private val appId = "0a08620a21fd4bd194c2c5da40539e29" // Should ideally come from BuildConfig or strings

    fun initEngine(handler: IRtcEngineEventHandler) {
        try {
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = appId
            config.mEventHandler = handler
            rtcEngine = RtcEngine.create(config)
            
            // Basic setup
            rtcEngine?.enableAudio()
            rtcEngine?.enableVideo()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun joinChannel(channelId: String, token: String?, uid: Int = 0) {
        rtcEngine?.joinChannel(token, channelId, uid, ChannelMediaOptions().apply {
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        })
    }

    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    fun setupLocalVideo(containerView: android.view.ViewGroup) {
        val surfaceView = android.view.SurfaceView(context)
        containerView.addView(surfaceView)
        rtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
    }

    fun setupRemoteVideo(uid: Int, containerView: android.view.ViewGroup) {
        val surfaceView = android.view.SurfaceView(context)
        containerView.addView(surfaceView)
        rtcEngine?.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid))
    }

    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}
*/

@Singleton
class AgoraCallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var rtcEngine: RtcEngine? = null

    private val myAppId = "0a08620a21fd4bd194c2c5da40539e29"

    fun initEngine(
        handler: IRtcEngineEventHandler
    ): Boolean {

        return try {

            if (rtcEngine != null) {
                android.util.Log.d(
                    "AGORA_CALL",
                    "Engine already initialized, adding handler"
                )
                rtcEngine?.addHandler(handler)
                return true
            }

            val config = RtcEngineConfig().apply {
                mContext = context
                mAppId = myAppId
                mEventHandler = handler
            }

            rtcEngine = RtcEngine.create(config)

            rtcEngine?.enableAudio()
            rtcEngine?.enableVideo()
            rtcEngine?.startPreview()
            rtcEngine?.setEnableSpeakerphone(true)

            android.util.Log.d(
                "AGORA_CALL",
                "Agora engine initialized"
            )

            true

        } catch (e: Exception) {
            android.util.Log.e(
                "AGORA_CALL",
                "Agora initialization failed",
                e
            )
            false
        }
    }

    fun removeHandler(handler: IRtcEngineEventHandler) {
        rtcEngine?.removeHandler(handler)
    }

    fun joinChannel(
        channelId: String,
        token: String?,
        uid: Int = 0
    ): Int {

        val engine = rtcEngine

        if (engine == null) {
            android.util.Log.e(
                "AGORA_CALL",
                "joinChannel: Engine is null"
            )
            return -1
        }

        val options = ChannelMediaOptions().apply {

            clientRoleType =
                Constants.CLIENT_ROLE_BROADCASTER

            channelProfile =
                Constants.CHANNEL_PROFILE_COMMUNICATION

            publishMicrophoneTrack = true
            publishCameraTrack = true

            autoSubscribeAudio = true
            autoSubscribeVideo = true
        }

        val result = engine.joinChannel(
            token,
            channelId,
            uid,
            options
        )

        android.util.Log.d(
            "AGORA_CALL",
            """
            joinChannel()
            result=$result
            channel=$channelId
            uid=$uid
            tokenPresent=${!token.isNullOrEmpty()}
            """.trimIndent()
        )

        return result
    }

    fun setupLocalVideo(
        containerView: ViewGroup
    ) {

        val engine = rtcEngine ?: return

        val surfaceView =
            SurfaceView(context)

        containerView.addView(surfaceView)

        engine.setupLocalVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
            )
        )
    }

    fun setupRemoteVideo(
        uid: Int,
        containerView: ViewGroup
    ) {

        val engine = rtcEngine ?: return

        val surfaceView =
            SurfaceView(context)

        containerView.addView(surfaceView)

        engine.setupRemoteVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid
            )
        )
    }

    fun leaveChannel() {

        rtcEngine?.leaveChannel()

        android.util.Log.d(
            "AGORA_CALL",
            "Left Agora channel"
        )
    }

    fun destroy() {

        rtcEngine?.leaveChannel()

        RtcEngine.destroy()

        rtcEngine = null

        android.util.Log.d(
            "AGORA_CALL",
            "Agora engine destroyed"
        )
    }
}
