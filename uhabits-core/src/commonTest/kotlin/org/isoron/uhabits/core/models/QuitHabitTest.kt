/*
 * Copyright (C) 2016-2025 Álinson Santos Xavier <git@axavier.org>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isoron.uhabits.core.models

import org.isoron.platform.time.getToday
import org.isoron.platform.time.setToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.commands.AddSlipCommand
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteSlipCommand
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuitHabitTest : BaseUnitTest() {
    private val hour = 3_600_000L

    @Test
    fun testComputedEntries() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        assertEquals(UNKNOWN, habit.computedEntries.get(today.minus(21)).value)
        // Quit at 08:30, so the quit day itself was not clean for the entire day
        assertEquals(UNKNOWN, habit.computedEntries.get(today.minus(20)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(19)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(13)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(12)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(11)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(6)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(5)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(4)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today).value)
        assertEquals(UNKNOWN, habit.computedEntries.get(today.plus(1)).value)
    }

    @Test
    fun testComputedEntries_quitAtMidnight() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.slips.clear()
        habit.quitSince = quitTimestamp(today.minus(3), 0, 0)
        habit.recompute()
        assertEquals(UNKNOWN, habit.computedEntries.get(today.minus(4)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(3)).value)
    }

    @Test
    fun testComputedEntries_quitToday() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.slips.clear()
        habit.quitSince = quitTimestamp(today, 14, 0)
        habit.recompute()
        // Today is optimistically considered clean
        assertEquals(YES_MANUAL, habit.computedEntries.get(today).value)
        assertEquals(UNKNOWN, habit.computedEntries.get(today.minus(1)).value)
    }

    @Test
    fun testComputedEntries_slipLateAtNight() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        AddSlipCommand(habitList, habit, quitTimestamp(today.minus(2), 23, 0)).run()
        assertEquals(NO, habit.computedEntries.get(today.minus(2)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(1)).value)
        assertEquals(2, habit.streaks.getBest(10).first { it.end == today }.length)
    }

    @Test
    fun testComputedEntries_slipToday() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        assertTrue(habit.isCompletedToday())
        AddSlipCommand(habitList, habit, quitTimestamp(today, 7, 0)).run()
        assertEquals(NO, habit.computedEntries.get(today).value)
        assertFalse(habit.isCompletedToday())
    }

    @Test
    fun testComputedEntries_deleteSlip() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        DeleteSlipCommand(habitList, habit, quitTimestamp(today.minus(12), 21, 0)).run()
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(12)).value)

        // The day stays a slip until every slip of that day is removed
        DeleteSlipCommand(habitList, habit, quitTimestamp(today.minus(5), 9, 15)).run()
        assertEquals(NO, habit.computedEntries.get(today.minus(5)).value)
        DeleteSlipCommand(habitList, habit, quitTimestamp(today.minus(5), 18, 45)).run()
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(5)).value)
    }

    @Test
    fun testComputedEntries_keepsNotes() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.originalEntries.add(Entry(today.minus(2), UNKNOWN, "Tough day"))
        habit.originalEntries.add(Entry(today.minus(5), UNKNOWN, "Party"))
        habit.originalEntries.add(Entry(today.minus(30), UNKNOWN, "Before quitting"))
        habit.recompute()
        assertEquals(Entry(today.minus(2), YES_MANUAL, "Tough day"), habit.computedEntries.get(today.minus(2)))
        assertEquals(Entry(today.minus(5), NO, "Party"), habit.computedEntries.get(today.minus(5)))
        assertEquals(Entry(today.minus(30), UNKNOWN, "Before quitting"), habit.computedEntries.get(today.minus(30)))
    }

    @Test
    fun testComputedEntries_withoutQuitDate() {
        val habit = fixtures.createQuitHabit()
        habit.quitSince = null
        habit.recompute()
        val today = getToday()
        assertEquals(UNKNOWN, habit.computedEntries.get(today.minus(13)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(12)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(11)).value)
    }

    @Test
    fun testRecomputeIfStale() {
        val habit = fixtures.createQuitHabit()
        val tomorrow = getToday().plus(1)
        setToday(tomorrow)
        assertEquals(UNKNOWN, habit.computedEntries.get(tomorrow).value)
        habit.recomputeIfStale()
        assertEquals(YES_MANUAL, habit.computedEntries.get(tomorrow).value)
    }

    @Test
    fun testStreaks() {
        val habit = fixtures.createQuitHabit()
        val lengths = habit.streaks.getBest(10).map { it.length }.sortedDescending()
        assertEquals(listOf(7, 6, 5), lengths)
    }

    @Test
    fun testScoreIncreasesWhileClean() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        val scoreAfterSlip = habit.scores[today.minus(5)].value
        val scoreToday = habit.scores[today].value
        assertTrue(scoreToday > scoreAfterSlip)
    }

    @Test
    fun testCleanTime() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        // Counted from the last of the two slips of that day
        assertEquals(quitTimestamp(today.minus(5), 18, 45), habit.getCleanSince())
        assertEquals(today.minus(5), habit.getLastSlipDate())
        assertEquals(CleanTime(4, 15, 30), habit.getCleanTime(today.unixTime + 10 * hour + 15 * 60_000))
    }

    @Test
    fun testCleanTime_noSlips() {
        val habit = fixtures.createQuitHabit()
        habit.slips.clear()
        habit.recompute()
        val today = getToday()
        assertNull(habit.getLastSlipDate())
        assertEquals(habit.quitSince, habit.getCleanSince())
        assertEquals(CleanTime(20, 1, 30), habit.getCleanTime(today.unixTime + 10 * hour))
    }

    @Test
    fun testCleanTime_multipleSlipsToday() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        AddSlipCommand(habitList, habit, quitTimestamp(today, 7, 0)).run()
        assertEquals(CleanTime(0, 3, 0), habit.getCleanTime(today.unixTime + 10 * hour))
        AddSlipCommand(habitList, habit, quitTimestamp(today, 9, 30)).run()
        assertEquals(CleanTime(0, 0, 30), habit.getCleanTime(today.unixTime + 10 * hour))
        assertFalse(habit.isCompletedToday())
    }

    @Test
    fun testCleanTime_quitInFuture() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.slips.clear()
        habit.quitSince = quitTimestamp(today.plus(2), 0, 0)
        habit.recompute()
        assertEquals(CleanTime(0, 0, 0), habit.getCleanTime(today.unixTime))
    }

    @Test
    fun testCountSlips() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        assertEquals(3, habit.countSlips())
        assertEquals(2, habit.countSlips(since = today.minus(6)))
    }

    @Test
    fun testCountSlipsByDay() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        val counts = habit.countSlipsByDay(today.minus(12), today)
        assertEquals(13, counts.size)
        assertEquals(Entry(today, 0), counts.first())
        assertEquals(Entry(today.minus(5), 2000), counts[5])
        assertEquals(Entry(today.minus(12), 1000), counts.last())
    }

    @Test
    fun testSlipList() {
        val slips = SlipList()
        slips.add(300)
        slips.add(100)
        slips.add(200)
        slips.add(200)
        assertEquals(listOf(300L, 200L, 100L), slips.getAll())
        assertEquals(300L, slips.getLatest())
        slips.remove(300)
        assertEquals(listOf(200L, 100L), slips.getAll())
    }

    @Test
    fun testSlipList_getByDate() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        assertEquals(
            listOf(quitTimestamp(today.minus(5), 9, 15), quitTimestamp(today.minus(5), 18, 45)),
            habit.slips.getByDate(today.minus(5))
        )
        assertEquals(emptyList(), habit.slips.getByDate(today.minus(4)))
    }

    @Test
    fun testCreateRepetition_onlyStoresNotes() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        CreateRepetitionCommand(habitList, habit, today.minus(3), NO, "Almost").run()
        assertEquals(Entry(today.minus(3), UNKNOWN, "Almost"), habit.originalEntries.get(today.minus(3)))
        assertEquals(Entry(today.minus(3), YES_MANUAL, "Almost"), habit.computedEntries.get(today.minus(3)))
        assertEquals(3, habit.countSlips())
    }

    @Test
    fun testMatcher_neverHidesQuitHabits() {
        val habit = fixtures.createQuitHabit()
        assertTrue(habit.isCompletedToday())
        assertTrue(HabitMatcher(isCompletedAllowed = false).matches(habit))
        assertTrue(HabitMatcher(isEnteredAllowed = false).matches(habit))
    }

    @Test
    fun testSlipWeekdayFrequency() {
        val habit = fixtures.createQuitHabit()
        val total = habit.computeSlipWeekdayFrequency().values.sumOf { it.sum() }
        assertEquals(3, total)
    }

    @Test
    fun testFormatLocalDateTime() {
        val today = getToday()
        assertEquals("2015-01-25 07:05", formatLocalDateTime(quitTimestamp(today, 7, 5)))
    }

    @Test
    fun testWriteCSV() {
        val habit = fixtures.createQuitHabit()
        habitList.add(habit)
        val csv = habitList.writeCSV().lines()
        assertEquals(
            "001,Quit smoking,QUIT,Did you smoke today?,,1,1,#F57C00,,,,false,2015-01-05 08:30",
            csv[1]
        )
    }
}
