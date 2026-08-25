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
        BathroomVisitEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AccessDatabase : RoomDatabase() {

    abstract fun accessDao(): AccessDao

    abstract fun roomEntryDao(): RoomEntryDao

    abstract fun bathroomVisitDao(): BathroomVisitDao

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
                        Log.d(TAG, "Room Callback: onCreate() called")
                        super.onCreate(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        Log.d(TAG, "Room Callback: onOpen() called")
                        // Verificar tablas
                        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table'")
                        val tables = mutableListOf<String>()
                        while (cursor.moveToNext()) {
                            tables.add(cursor.getString(0))
                        }
                        cursor.close()
                        Log.d(TAG, "Room Callback: Tables in DB: ${tables.joinToString()}")
                        super.onOpen(db)
                    }
                })
                .build()
                .also { instance = it }
        }
    }
}
