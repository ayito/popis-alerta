package org.popisalerta.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evento de alto nivel: una visita al cuarto de baño.
 *
 * A diferencia de RoomEntryEntity (picos de sensores),
 * aquí queremos una sola fila por "episodio" de visita.
 */
@Entity(
    tableName = "bathroom_visits",
    indices = [
        Index(value = ["startedAt"])
    ]
)
data class BathroomVisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // Momento en el que detectamos la entrada al baño (epoch millis)
    val startedAt: Long,

    // Indica si ya se lanzó la llamada telefónica asociada a esta visita
    val notified: Boolean = false
)
