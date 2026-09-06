package org.popisalerta.app.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        AccessEntity::class,
        RoomEntryEntity::class,
        RoomVisitEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AccessDatabase : RoomDatabase() {

    abstract fun accessDao(): AccessDao

    abstract fun roomEntryDao(): RoomEntryDao

    abstract fun roomVisitDao(): RoomVisitDao

    companion object {
        private const val TAG = "PopisAlerta"

        @Volatile
        private var instance: AccessDatabase? = null

        fun getInstance(context: Context): AccessDatabase = instance ?: synchronized(this) {
            instance ?: Room
                .databaseBuilder(
                    context.applicationContext,
                    AccessDatabase::class.java,
                    "popis_alerta_v3.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        Log.d(TAG, "Room database created")
                        super.onCreate(db)
                    }
                })
                .build()
                .also { instance = it }
        }
    }
}
