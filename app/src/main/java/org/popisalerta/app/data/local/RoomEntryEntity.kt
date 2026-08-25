package org.popisalerta.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "room_entries",
    indices = [
        Index(value = ["timestamp"])
    ]
)
data class RoomEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Momento de la entrada (epoch millis)
    val timestamp: Long = System.currentTimeMillis(),

    // Señales que participaron en la detección
    val motionSpike: Boolean,
    val lightSpike: Boolean,

    // Nivel de confianza (0.0–1.0), por ahora siempre 1.0
    val confidence: Float = 1.0f
)
