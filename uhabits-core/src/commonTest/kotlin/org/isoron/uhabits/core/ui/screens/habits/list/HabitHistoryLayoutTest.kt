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
package org.isoron.uhabits.core.ui.screens.habits.list

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.NumericalHabitType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HabitHistoryLayoutTest : BaseUnitTest() {
    // BaseUnitTest fixes today at Sunday, 25 January 2015. 1 January 2015 was a Thursday.

    @Test
    fun testMonthCells_mondayFirst() {
        val cells = HabitHistoryLayout.monthCells(getToday(), DayOfWeek.MONDAY)
        assertEquals(31, cells.size)
        assertEquals(LocalDate(2015, 1, 1), cells.first().date)
        // Thursday is the fourth column of a Monday-first week, in the first row.
        assertEquals(0, cells.first().row)
        assertEquals(3, cells.first().column)
        // Monday 5 January starts the second row.
        val fifth = cells.first { it.date == LocalDate(2015, 1, 5) }
        assertEquals(1, fifth.row)
        assertEquals(0, fifth.column)
        assertEquals(LocalDate(2015, 1, 31), cells.last().date)
        assertEquals(5, HabitHistoryLayout.monthRowCount(getToday(), DayOfWeek.MONDAY))
    }

    @Test
    fun testMonthCells_sundayFirst() {
        val cells = HabitHistoryLayout.monthCells(getToday(), DayOfWeek.SUNDAY)
        assertEquals(4, cells.first().column)
        val today = cells.first { it.date == getToday() }
        assertEquals(4, today.row)
        assertEquals(0, today.column)
        assertEquals(5, HabitHistoryLayout.monthRowCount(getToday(), DayOfWeek.SUNDAY))
    }

    @Test
    fun testMonthRowCount_sixWeeks() {
        // 1 March 2015 was a Sunday, so a Monday-first March needs six rows.
        assertEquals(6, HabitHistoryLayout.monthRowCount(LocalDate(2015, 3, 10), DayOfWeek.MONDAY))
        assertEquals(5, HabitHistoryLayout.monthRowCount(LocalDate(2015, 3, 10), DayOfWeek.SUNDAY))
    }

    @Test
    fun testGridCells_endsOnToday() {
        val cells = HabitHistoryLayout.gridCells(getToday(), DayOfWeek.MONDAY, columns = 4)
        // Today is a Sunday, the last day of a Monday-first week, so all four weeks are full.
        assertEquals(28, cells.size)
        assertEquals(getToday(), cells.last().date)
        assertEquals(6, cells.last().row)
        assertEquals(3, cells.last().column)
        assertEquals(getToday().minus(27), cells.first().date)
    }

    @Test
    fun testGridCells_skipsFutureDays() {
        val cells = HabitHistoryLayout.gridCells(getToday(), DayOfWeek.SUNDAY, columns = 2)
        // Sunday-first: today starts a new week, so only one day of the last column exists.
        assertEquals(8, cells.size)
        assertEquals(getToday(), cells.last().date)
        assertEquals(0, cells.last().row)
        assertTrue(cells.none { it.date.isNewerThan(getToday()) })
    }

    @Test
    fun testGridCells_zeroColumns() {
        assertTrue(HabitHistoryLayout.gridCells(getToday(), DayOfWeek.MONDAY, 0).isEmpty())
    }

    @Test
    fun testStatus_yesNo() {
        val habit = fixtures.createEmptyHabit()
        val today = getToday()
        habit.originalEntries.add(Entry(today, Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(1), Entry.SKIP))
        habit.originalEntries.add(Entry(today.minus(2), Entry.NO))
        habit.recompute()
        assertEquals(HistoryCellStatus.DONE, HabitHistoryLayout.status(habit, today))
        assertEquals(HistoryCellStatus.SKIPPED, HabitHistoryLayout.status(habit, today.minus(1)))
        assertEquals(HistoryCellStatus.MISSED, HabitHistoryLayout.status(habit, today.minus(2)))
        assertEquals(HistoryCellStatus.UNKNOWN, HabitHistoryLayout.status(habit, today.minus(3)))
    }

    @Test
    fun testStatus_numerical() {
        val habit = fixtures.createEmptyHabit()
        habit.type = HabitType.NUMERICAL
        habit.targetType = NumericalHabitType.AT_LEAST
        habit.targetValue = 10.0
        val today = getToday()
        habit.originalEntries.add(Entry(today, 12_000))
        habit.originalEntries.add(Entry(today.minus(1), 4_000))
        habit.originalEntries.add(Entry(today.minus(2), 0))
        habit.recompute()
        assertEquals(HistoryCellStatus.DONE, HabitHistoryLayout.status(habit, today))
        assertEquals(HistoryCellStatus.PARTIAL, HabitHistoryLayout.status(habit, today.minus(1)))
        assertEquals(HistoryCellStatus.MISSED, HabitHistoryLayout.status(habit, today.minus(2)))
    }
}
