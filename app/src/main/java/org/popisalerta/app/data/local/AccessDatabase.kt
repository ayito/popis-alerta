package org.popisalerta.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

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
        @Volatile
        private var instance: AccessDatabase? = null

        fun getInstance(context: Context): AccessDatabase = instance ?: synchronized(this) {
            instance ?: Room
                .databaseBuilder(
                    context.applicationContext,
                    AccessDatabase::class.java,
                    "popis_alerta.db"
                )
                // En desarrollo: si cambia el esquema, borra y recrea la BD
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
    }
}
