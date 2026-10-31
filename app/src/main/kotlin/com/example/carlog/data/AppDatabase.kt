package com.example.carlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Car::class, WorkEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun workEntryDao(): WorkEntryDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        // One shared database for the whole app.
        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_log.db",
                ).build().also { instance = it }
            }
    }
}