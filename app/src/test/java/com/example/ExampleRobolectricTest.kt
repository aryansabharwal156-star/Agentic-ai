package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.BusinessEntity
import com.example.data.local.CallLogEntity
import com.example.data.local.SeedData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("VoiceBiz AI", appName)
    }

    @Test
    fun `verify seed data contains 50 diverse businesses across industries`() {
        val businesses = SeedData.getInitialBusinesses()
        assertEquals(50, businesses.size)

        val industries = businesses.map { it.industry }.distinct()
        assertTrue("Expected at least 8 industries", industries.size >= 8)

        val sampleCalls = SeedData.getInitialSampleCallLogs()
        assertTrue("Expected sample call logs", sampleCalls.isNotEmpty())
    }

    @Test
    fun `verify room database inserts and queries businesses`() = runBlocking {
        val businessDao = db.businessDao()
        val initialList = SeedData.getInitialBusinesses()
        businessDao.insertAll(initialList)

        val retrieved = businessDao.getAllBusinesses().first()
        assertEquals(50, retrieved.size)

        // Insert new custom business
        val customBiz = BusinessEntity(
            name = "Apex Financial Advisors",
            industry = "Wealth Management",
            countryCity = "Zurich, Switzerland",
            phone = "+41 44 123 4567",
            email = "contact@apexfinancial.example",
            whyNeedVoiceBot = "High net-worth clients calling across global timezones",
            estMonthlyMissedRev = 18000.0,
            botPersonaName = "Oliver (Wealth AI Advisor)",
            demoCode = "DEMO-APEX1"
        )
        val newId = businessDao.insert(customBiz)
        assertTrue(newId > 0)

        val found = businessDao.getBusinessById(newId)
        assertNotNull(found)
        assertEquals("Apex Financial Advisors", found?.name)
    }

    @Test
    fun `verify call log persistence and metrics`() = runBlocking {
        val callLogDao = db.callLogDao()
        val sampleCalls = SeedData.getInitialSampleCallLogs()
        callLogDao.insertAll(sampleCalls)

        val allLogs = callLogDao.getAllCallLogs().first()
        assertTrue(allLogs.size >= sampleCalls.size)

        val firstCall = allLogs.first()
        assertTrue(firstCall.durationSeconds > 0)
        assertNotNull(firstCall.summary)
    }
}
