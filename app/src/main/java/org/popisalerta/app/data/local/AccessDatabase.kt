package org.popisalerta.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [AccessEntity::class],
  version = 1,
  exportSchema = false,
)
abstract class AccessDatabase : RoomDatabase() {
  abstract fun accessDao(): AccessDao

  companion object {
    @Volatile
    private var instance: AccessDatabase? = null

    fun getInstance(context: Context): AccessDatabase =
      instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          AccessDatabase::class.java,
          "popis_alerta.db",
        ).build().also { instance = it }
      }
  }
}
