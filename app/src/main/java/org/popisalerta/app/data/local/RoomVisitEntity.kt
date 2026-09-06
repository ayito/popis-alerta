package org.popisalerta.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evento de alto nivel: un posible acceso a una sala.
 *
 * A diferencia de [RoomEntryEntity], que registra picos individuales de sensores,
 * esta entidad conserva una única fila por episodio de acceso.
 */
@Entity(
    tableName = "room_visits",
    indices = [
        Index(value = ["startedAt"])
    ]
)
data class RoomVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Momento en el que detectamos el acceso a la sala, en milisegundos desde epoch.
    val startedAt: Long,

    // Indica si ya se lanzó la llamada telefónica asociada a este acceso.
    val notified: Boolean = false
)
