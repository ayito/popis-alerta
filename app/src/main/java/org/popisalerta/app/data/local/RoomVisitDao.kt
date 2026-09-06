package org.popisalerta.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomVisitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(visit: RoomVisitEntity): Long

    @Query(
        """
        SELECT * FROM room_visits
        ORDER BY startedAt DESC
        """
    )
    fun getAllVisits(): Flow<List<RoomVisitEntity>>

    @Query(
        """
        SELECT * FROM room_visits
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    suspend fun getLastVisit(): RoomVisitEntity?

    @Query("SELECT COUNT(*) FROM room_visits")
    suspend fun getVisitCount(): Long

    @Query("DELETE FROM room_visits")
    suspend fun deleteAll()
}
