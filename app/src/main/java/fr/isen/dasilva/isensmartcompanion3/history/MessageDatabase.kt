package fr.isen.dasilva.isensmartcompanion3.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.isen.dasilva.isensmartcompanion3.event.EventDao
import fr.isen.dasilva.isensmartcompanion3.event.EventEntity

@Database(entities = [Message::class, EventEntity::class], version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile private var INSTANCE: MessageDatabase? = null

        fun getDatabase(context: Context): MessageDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MessageDatabase::class.java,
                    "chat_history_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}