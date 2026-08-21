package com.chat.shutup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chat.shutup.data.local.entity.ChatMessageEntity

@Database(
    entities = [ChatMessageEntity::class],
    version = 1
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val dao: ChatDao
}
