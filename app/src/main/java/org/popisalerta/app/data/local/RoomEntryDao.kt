package org.popisalerta.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: RoomEntryEntity): Long

    @Query(
        """
        SELECT * FROM room_entries
        ORDER BY timestamp DESC
        """
    )
    fun getAllEntries(): Flow<List<RoomEntryEntity>>

    @Query(
        """
        SELECT * FROM room_entries
        WHERE timestamp BETWEEN :from AND :to
        ORDER BY timestamp ASC
        """
    )
    fun getEntriesBetween(
        from: Long,
        to: Long,
    ): Flow<List<RoomEntryEntity>>

    @Query("SELECT COUNT(*) FROM room_entries")
    suspend fun getEntryCount(): Long
}
