package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY callDate DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE businessId = :businessId ORDER BY callDate DESC")
    fun getCallLogsForBusiness(businessId: Long): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE id = :id")
    suspend fun getCallLogById(id: Long): CallLogEntity?

    @Query("SELECT COUNT(*) FROM call_logs")
    suspend fun getTotalCallsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callLog: CallLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(callLogs: List<CallLogEntity>)

    @Update
    suspend fun update(callLog: CallLogEntity)

    @Delete
    suspend fun delete(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
