package com.chat.shutup.di

import android.app.Application
import androidx.room.Room
import com.chat.shutup.data.local.ChatDao
import com.chat.shutup.data.local.ChatDatabase
import com.chat.shutup.data.remote.ChatApi
import com.chat.shutup.data.repository.AuthRepositoryImpl
import com.chat.shutup.data.repository.ChatRepositoryImpl
import com.chat.shutup.domain.repository.AuthRepository
import com.chat.shutup.domain.repository.ChatRepository
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

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
            return com.google.firebase.auth.FirebaseAuth.getInstance()
        }

        @Provides
        @Singleton
        fun provideFirebaseDatabase(): com.google.firebase.database.FirebaseDatabase {
            return com.google.firebase.database.FirebaseDatabase.getInstance("https://shut-up-chat-default-rtdb.firebaseio.com")
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
        fun provideChatApi(): ChatApi {
            val contentType = "application/json".toMediaType()
            val json = Json { ignoreUnknownKeys = true }
            return Retrofit.Builder()
                .baseUrl(ChatApi.BASE_URL)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(ChatApi::class.java)
        }
    }
}
