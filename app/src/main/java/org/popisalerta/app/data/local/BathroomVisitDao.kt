package org.popisalerta.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BathroomVisitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(visit: BathroomVisitEntity): Long

    @Query(
        """
        SELECT * FROM bathroom_visits
        ORDER BY startedAt DESC
        """
    )
    fun getAllVisits(): Flow<List<BathroomVisitEntity>>

    @Query(
        """
        SELECT * FROM bathroom_visits
        ORDER BY startedAt DESC
        LIMIT 1
        """
    )
    suspend fun getLastVisit(): BathroomVisitEntity?

    @Query("SELECT COUNT(*) FROM bathroom_visits")
    suspend fun getVisitCount(): Long
}
