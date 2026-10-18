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

import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.TruncateField

/**
 * Whether the habit's target is a number of checkmarks per week, such as "5 times per week".
 */
val Habit.isWeeklyTarget: Boolean
    get() = !isNumerical && frequency.denominator == 7 && frequency.numerator < 7

/**
 * Number of consecutive weeks, up to [today], in which the weekly target was met. The current week
 * does not break the streak while it is still in progress, but counts once its target is met.
 */
fun Habit.getCurrentWeeklyStreak(today: LocalDate, firstWeekday: Int): Int {
    val oldest = computedEntries.getKnown().lastOrNull()?.date ?: return 0
    val weekly = computedEntries.getByInterval(oldest, today).groupedSum(
        truncateField = TruncateField.WEEK_NUMBER,
        firstWeekday = firstWeekday,
        isNumerical = false
    ).associate { it.date to it.value }

    val required = frequency.numerator * 1000
    val currentWeek = today.startOfWeek(DayOfWeek.values()[firstWeekday - 1])
    val oldestWeek = oldest.startOfWeek(DayOfWeek.values()[firstWeekday - 1])

    var streak = if ((weekly[currentWeek] ?: 0) >= required) 1 else 0
    var week = currentWeek.minus(7)
    while (week.daysSince2000 >= oldestWeek.daysSince2000 && (weekly[week] ?: 0) >= required) {
        streak++
        week = week.minus(7)
    }
    return streak
}
