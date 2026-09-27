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
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
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
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(20)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(13)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(12)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(6)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(5)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today).value)
        assertEquals(UNKNOWN, habit.computedEntries.get(today.plus(1)).value)
    }

    @Test
    fun testComputedEntries_keepsNotes() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.originalEntries.add(Entry(today.minus(2), UNKNOWN, "Tough day"))
        habit.originalEntries.add(Entry(today.minus(30), UNKNOWN, "Before quitting"))
        habit.recompute()
        assertEquals(Entry(today.minus(2), YES_MANUAL, "Tough day"), habit.computedEntries.get(today.minus(2)))
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
        assertEquals(listOf(8, 6, 5), lengths)
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
        assertEquals(today.minus(4).unixTime, habit.getCleanSince())
        assertEquals(today.minus(5), habit.getLastSlipDate())
        assertEquals(CleanTime(4, 10, 15), habit.getCleanTime(today.unixTime + 10 * hour + 15 * 60_000))
    }

    @Test
    fun testCleanTime_noSlips() {
        val habit = fixtures.createQuitHabit()
        habit.originalEntries.clear()
        habit.recompute()
        val today = getToday()
        assertNull(habit.getLastSlipDate())
        assertEquals(habit.quitSince, habit.getCleanSince())
        assertEquals(CleanTime(20, 1, 30), habit.getCleanTime(today.unixTime + 10 * hour))
    }

    @Test
    fun testCleanTime_slippedToday() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        CreateRepetitionCommand(habitList, habit, today, NO, "").run()
        assertEquals(CleanTime(0, 0, 0), habit.getCleanTime(today.unixTime + 10 * hour))
        assertFalse(habit.isCompletedToday())
    }

    @Test
    fun testCleanTime_quitInFuture() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        habit.originalEntries.clear()
        habit.quitSince = quitTimestamp(today.plus(2), 0, 0)
        habit.recompute()
        assertEquals(CleanTime(0, 0, 0), habit.getCleanTime(today.unixTime))
    }

    @Test
    fun testCountSlips() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        assertEquals(2, habit.countSlips())
        assertEquals(1, habit.countSlips(since = today.minus(6)))
    }

    @Test
    fun testCreateRepetition_onlyStoresSlips() {
        val habit = fixtures.createQuitHabit()
        val today = getToday()
        CreateRepetitionCommand(habitList, habit, today.minus(5), YES_MANUAL, "").run()
        assertEquals(UNKNOWN, habit.originalEntries.get(today.minus(5)).value)
        assertEquals(YES_MANUAL, habit.computedEntries.get(today.minus(5)).value)

        CreateRepetitionCommand(habitList, habit, today.minus(3), SKIP, "").run()
        assertEquals(UNKNOWN, habit.originalEntries.get(today.minus(3)).value)

        CreateRepetitionCommand(habitList, habit, today.minus(3), NO, "").run()
        assertEquals(NO, habit.originalEntries.get(today.minus(3)).value)
        assertEquals(NO, habit.computedEntries.get(today.minus(3)).value)
    }

    @Test
    fun testNextQuitToggleValue() {
        assertEquals(NO, Entry.nextQuitToggleValue(YES_MANUAL))
        assertEquals(NO, Entry.nextQuitToggleValue(UNKNOWN))
        assertEquals(YES_MANUAL, Entry.nextQuitToggleValue(NO))
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
        assertEquals(2, total)
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
