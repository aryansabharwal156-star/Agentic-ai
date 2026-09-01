package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM businesses ORDER BY id ASC")
    fun getAllBusinesses(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE id = :id")
    suspend fun getBusinessById(id: Long): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE id = :id")
    fun getBusinessByIdFlow(id: Long): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses WHERE demoCode = :demoCode LIMIT 1")
    suspend fun getBusinessByDemoCode(demoCode: String): BusinessEntity?

    @Query("SELECT * FROM businesses WHERE industry = :industry ORDER BY name ASC")
    fun getBusinessesByIndustry(industry: String): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE status = :status ORDER BY name ASC")
    fun getBusinessesByStatus(status: String): Flow<List<BusinessEntity>>

    @Query("SELECT COUNT(*) FROM businesses")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(business: BusinessEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(businesses: List<BusinessEntity>)

    @Update
    suspend fun update(business: BusinessEntity)

    @Delete
    suspend fun delete(business: BusinessEntity)

    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE businesses SET status = :newStatus, lastContactedDate = :timestamp, emailSentCount = emailSentCount + 1 WHERE id = :businessId")
    suspend fun recordEmailSent(businessId: Long, newStatus: String, timestamp: Long)
}
