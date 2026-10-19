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
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.NumericalHabitType

/**
 * One day in a history heatmap. In the grid, [row] is the weekday (counted from the first
 * weekday) and [column] the week. In the month calendar it is the other way around: [row] is the
 * week and [column] the weekday.
 */
data class HistoryCell(val date: LocalDate, val row: Int, val column: Int)

/** How a day should be painted in a history heatmap. */
enum class HistoryCellStatus {
    /** The habit was done (or, for numerical habits, the target was met). */
    DONE,

    /** Done by the schedule rather than by the user, e.g. the clean days of a quit habit. */
    AUTO,

    /** A numerical habit with some progress that did not meet the target. */
    PARTIAL,

    SKIPPED,
    MISSED,
    UNKNOWN
}

object HabitHistoryLayout {
    /**
     * The current calendar month of [today], as a calendar with one row per week and one column
     * per weekday, starting at [firstWeekday].
     */
    fun monthCells(today: LocalDate, firstWeekday: DayOfWeek): List<HistoryCell> {
        val first = today.startOfMonth()
        val offset = weekdayRow(first, firstWeekday)
        return (0 until first.monthLength).map { i ->
            val index = offset + i
            HistoryCell(first.plus(i), index / 7, index % 7)
        }
    }

    fun monthRowCount(today: LocalDate, firstWeekday: DayOfWeek): Int {
        val first = today.startOfMonth()
        return (weekdayRow(first, firstWeekday) + first.monthLength + 6) / 7
    }

    /**
     * The most recent [columns] weeks up to [today], oldest first. Weeks are aligned to
     * [firstWeekday]; days after [today] are left out.
     */
    fun gridCells(today: LocalDate, firstWeekday: DayOfWeek, columns: Int): List<HistoryCell> {
        if (columns <= 0) return emptyList()
        val lastWeekStart = today.startOfWeek(firstWeekday)
        val firstDay = lastWeekStart.minus(7 * (columns - 1))
        return (0 until columns * 7)
            .map { HistoryCell(firstDay.plus(it), it % 7, it / 7) }
            .filter { !it.date.isNewerThan(today) }
    }

    fun status(habit: Habit, date: LocalDate): HistoryCellStatus {
        val value = habit.computedEntries.get(date).value
        if (habit.isNumerical) {
            if (value == Entry.UNKNOWN) return HistoryCellStatus.UNKNOWN
            if (value == Entry.SKIP) return HistoryCellStatus.SKIPPED
            val amount = value / 1000.0
            return when (habit.targetType) {
                NumericalHabitType.AT_LEAST -> when {
                    amount >= habit.targetValue -> HistoryCellStatus.DONE
                    amount > 0 -> HistoryCellStatus.PARTIAL
                    else -> HistoryCellStatus.MISSED
                }
                NumericalHabitType.AT_MOST -> when {
                    amount <= habit.targetValue -> HistoryCellStatus.DONE
                    else -> HistoryCellStatus.MISSED
                }
            }
        }
        return when (value) {
            Entry.YES_MANUAL -> HistoryCellStatus.DONE
            Entry.YES_AUTO -> HistoryCellStatus.AUTO
            Entry.SKIP -> HistoryCellStatus.SKIPPED
            Entry.NO -> HistoryCellStatus.MISSED
            else -> HistoryCellStatus.UNKNOWN
        }
    }

    private fun weekdayRow(date: LocalDate, firstWeekday: DayOfWeek): Int =
        (date.dayOfWeek.daysSinceSunday - firstWeekday.daysSinceSunday + 7) % 7
}
