package com.chat.shutup.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.chat.shutup.data.local.entity.ChatMessageEntity
import com.chat.shutup.data.local.entity.TripEntity
import com.chat.shutup.data.local.entity.TripMemberEntity

@Database(
    entities = [ChatMessageEntity::class, TripEntity::class, TripMemberEntity::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6)
    ]
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val dao: ChatDao
    abstract val tripDao: TripDao
    abstract val tripMemberDao: TripMemberDao
}
