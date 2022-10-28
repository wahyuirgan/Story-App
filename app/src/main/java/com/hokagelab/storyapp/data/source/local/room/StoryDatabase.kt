package com.hokagelab.storyapp.data.source.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hokagelab.storyapp.data.source.local.entity.RemoteEntity
import com.hokagelab.storyapp.data.source.local.entity.StoryEntity

@Database(entities = [StoryEntity::class, RemoteEntity::class], version = 1, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun remoteDao(): RemoteDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    "story_db"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}