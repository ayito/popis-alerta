package org.popisalerta.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AccessDao {
    @Query("SELECT * FROM access_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<AccessEntity>>

    @Query(
        """
    SELECT * FROM access_logs
    WHERE timestamp >= :startMs
    ORDER BY timestamp DESC
    """
    )
    fun observeSince(startMs: Long): Flow<List<AccessEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(access: AccessEntity): Long

    @Query("DELETE FROM access_logs")
    suspend fun deleteAll()
}
