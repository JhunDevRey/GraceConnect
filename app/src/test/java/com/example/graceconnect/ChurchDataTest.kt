package com.example.graceconnect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ChurchDataTest {

    private val sundayService = ChurchData.events.first { it.id == "sunday_worship" } // Sun 9:00 AM

    /** Wednesday, Sep 30 2026 at the given time. */
    private fun wednesday(hour: Int = 12, minute: Int = 0): Calendar = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.SEPTEMBER, 30, hour, minute)
    }

    @Test
    fun nextStart_isLaterThisWeek() {
        val next = sundayService.nextStart(wednesday())
        assertEquals(Calendar.SUNDAY, next.get(Calendar.DAY_OF_WEEK))
        assertEquals(Calendar.OCTOBER, next.get(Calendar.MONTH)) // Sun, Oct 4
        assertEquals(4, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(9, next.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun nextStart_isTodayWhenEventHasNotStarted() {
        val prayer = ChurchData.events.first { it.id == "midweek_prayer" } // Wed 7:00 PM
        val next = prayer.nextStart(wednesday(hour = 18))
        assertEquals(30, next.get(Calendar.DAY_OF_MONTH))
        assertEquals(19, next.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun nextStart_skipsToNextWeekOnceEventHasStarted() {
        val prayer = ChurchData.events.first { it.id == "midweek_prayer" }
        val next = prayer.nextStart(wednesday(hour = 19, minute = 1))
        assertEquals(Calendar.OCTOBER, next.get(Calendar.MONTH))
        assertEquals(7, next.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun nextStart_isNeverInThePast() {
        val now = wednesday(hour = 23, minute = 59)
        ChurchData.events.forEach { event ->
            val next = event.nextStart(now)
            assertFalse(event.id, next.before(now))
            assertTrue(event.id, next.timeInMillis - now.timeInMillis <= 7L * 24 * 60 * 60 * 1000)
        }
    }

    @Test
    fun sermonDates_areSundaysNewestFirst() {
        val today = Calendar.getInstance()
        val dates = ChurchData.sermons.map { it.date() }
        dates.forEach { assertEquals(Calendar.SUNDAY, it.get(Calendar.DAY_OF_WEEK)) }
        assertFalse(dates.first().after(today))
        assertEquals(dates.sortedByDescending { it.timeInMillis }, dates)
    }

    @Test
    fun ids_areUnique() {
        listOf(
            ChurchData.sermons.map { it.id },
            ChurchData.events.map { it.id },
            ChurchData.groups.map { it.id },
            ChurchData.verses.map { it.id }
        ).forEach { ids -> assertEquals(ids.size, ids.toSet().size) }
    }
}
