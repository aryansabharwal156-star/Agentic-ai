package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OutreachDao {
    @Query("SELECT * FROM outreach_logs ORDER BY sentTimestamp DESC")
    fun getAllOutreachLogs(): Flow<List<OutreachLogEntity>>

    @Query("SELECT * FROM outreach_logs WHERE businessId = :businessId ORDER BY sentTimestamp DESC")
    fun getOutreachForBusiness(businessId: Long): Flow<List<OutreachLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(outreach: OutreachLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(outreachLogs: List<OutreachLogEntity>)
}
