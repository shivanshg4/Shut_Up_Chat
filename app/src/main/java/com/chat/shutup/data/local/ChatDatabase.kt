package com.chat.shutup.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.chat.shutup.data.local.entity.ChatMessageEntity
import com.chat.shutup.data.local.entity.TripEntity

@Database(
    entities = [ChatMessageEntity::class, TripEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val dao: ChatDao
    abstract val tripDao: TripDao
}
