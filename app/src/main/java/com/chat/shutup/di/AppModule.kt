package com.chat.shutup.di

import android.app.Application
import androidx.room.Room
import com.chat.shutup.data.local.ChatDao
import com.chat.shutup.data.local.ChatDatabase
import com.chat.shutup.data.local.TripDao
import com.chat.shutup.data.local.TripMemberDao
import com.chat.shutup.data.remote.ChatApi
import com.chat.shutup.data.remote.RoutesApi
import com.chat.shutup.data.repository.AuthRepositoryImpl
import com.chat.shutup.data.repository.ChatRepositoryImpl
import com.chat.shutup.data.repository.DefaultLocationClient
import com.chat.shutup.data.repository.FcmRepositoryImpl
import com.chat.shutup.data.repository.LocationSearchRepositoryImpl
import com.chat.shutup.data.repository.PreferenceTrackingRepository
import com.chat.shutup.data.repository.RouteRepositoryImpl
import com.chat.shutup.data.repository.TripChatRepositoryImpl
import com.chat.shutup.data.repository.TripLocationRepositoryImpl
import com.chat.shutup.data.repository.TripPreferencesRepositoryImpl
import com.chat.shutup.data.repository.TripRepositoryImpl
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
import com.chat.shutup.domain.repository.FcmRepository
import com.chat.shutup.domain.repository.LocationClient
import com.chat.shutup.domain.repository.LocationSearchRepository
import com.chat.shutup.domain.repository.RouteRepository
import com.chat.shutup.domain.repository.TrackingRepository
import com.chat.shutup.domain.repository.TripChatRepository
import com.chat.shutup.domain.repository.TripLocationRepository
import com.chat.shutup.domain.repository.TripPreferencesRepository
import com.chat.shutup.domain.repository.TripRepository
import com.chat.shutup.feature.trip.presentation.util.VehicleSpriteProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        chatRepositoryImpl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        tripRepositoryImpl: TripRepositoryImpl
    ): TripRepository

    @Binds
    @Singleton
    abstract fun bindTripChatRepository(
        tripChatRepositoryImpl: TripChatRepositoryImpl
    ): TripChatRepository

    @Binds
    @Singleton
    abstract fun bindTripPreferencesRepository(
        tripPreferencesRepositoryImpl: TripPreferencesRepositoryImpl
    ): TripPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindTripLocationRepository(
        tripLocationRepositoryImpl: TripLocationRepositoryImpl
    ): TripLocationRepository

    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        routeRepositoryImpl: RouteRepositoryImpl
    ): RouteRepository

    @Binds
    @Singleton
    abstract fun bindLocationSearchRepository(
        locationSearchRepositoryImpl: LocationSearchRepositoryImpl
    ): LocationSearchRepository

    @Binds
    @Singleton
    abstract fun bindTrackingRepository(
        preferenceTrackingRepository: PreferenceTrackingRepository
    ): TrackingRepository

    @Binds
    @Singleton
    abstract fun bindFcmRepository(
        fcmRepositoryImpl: FcmRepositoryImpl
    ): FcmRepository

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        defaultLocationClient: DefaultLocationClient
    ): LocationClient

    companion object {
        @Provides
        @Singleton
        fun provideVehicleSpriteProvider(app: Application): VehicleSpriteProvider {
            return VehicleSpriteProvider(app)
        }

        @Provides
        @Singleton
        fun provideFusedLocationProviderClient(app: Application): FusedLocationProviderClient {
            return LocationServices.getFusedLocationProviderClient(app)
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
            return com.google.firebase.auth.FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseDatabase(): com.google.firebase.database.FirebaseDatabase {
            return com.google.firebase.database.FirebaseDatabase.getInstance(com.chat.shutup.BuildConfig.FIREBASE_DATABASE_URL)
        }

        @Provides
        @Singleton
        fun provideChatDatabase(app: Application): ChatDatabase {
            return Room.databaseBuilder(
                app,
                ChatDatabase::class.java,
                "shutup_db"
            ).build()
        }

        @Provides
        @Singleton
        fun provideChatDao(db: ChatDatabase): ChatDao {
            return db.dao
        }

        @Provides
        @Singleton
        fun provideTripDao(db: ChatDatabase): TripDao {
            return db.tripDao
        }

        @Provides
        @Singleton
        fun provideTripMemberDao(db: ChatDatabase): TripMemberDao {
            return db.tripMemberDao
        }

        @Provides
        @Singleton
        fun provideChatApi(): ChatApi {
            val contentType = "application/json".toMediaType()
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(ChatApi.BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(ChatApi::class.java)
        }

        @Provides
        @Singleton
        fun provideRoutesApi(): RoutesApi {
            val contentType = "application/json".toMediaType()
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(RoutesApi.BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(RoutesApi::class.java)
        }
    }
}
