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

import org.isoron.platform.time.LocalDate

private const val MILLIS_PER_MINUTE = 60_000L
private const val MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE
private const val MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR

/**
 * Amount of time a quit habit has been kept clean, split into days, hours and minutes.
 */
data class CleanTime(val days: Long, val hours: Long, val minutes: Long) {
    companion object {
        fun fromMillis(millis: Long): CleanTime {
            val m = maxOf(0L, millis)
            return CleanTime(
                days = m / MILLIS_PER_DAY,
                hours = (m % MILLIS_PER_DAY) / MILLIS_PER_HOUR,
                minutes = (m % MILLIS_PER_HOUR) / MILLIS_PER_MINUTE
            )
        }
    }
}

/**
 * Builds a quit timestamp (local wall-clock milliseconds) from a date and a time of day.
 */
fun quitTimestamp(date: LocalDate, hour: Int, minute: Int): Long =
    date.unixTime + hour * MILLIS_PER_HOUR + minute * MILLIS_PER_MINUTE

/**
 * Returns the day the user quit, or null if the habit has no quit date.
 */
fun Habit.getQuitDate(): LocalDate? = quitSince?.let { LocalDate.fromUnixTime(it) }

/**
 * Returns the most recent day in which the user slipped, or null if they never did.
 */
fun Habit.getLastSlipDate(): LocalDate? =
    originalEntries.getKnown().firstOrNull { it.value == Entry.NO }?.date

/**
 * Returns the number of slips recorded on or after the given date.
 */
fun Habit.countSlips(since: LocalDate? = null): Int =
    originalEntries.getKnown().count {
        it.value == Entry.NO && (since == null || it.date >= since)
    }

/**
 * Returns the moment (local wall-clock milliseconds) from which the current clean period is
 * counted. This is either the quit time or the beginning of the day after the most recent slip,
 * whichever is later. Returns null if the habit has neither a quit date nor any slips.
 */
fun Habit.getCleanSince(): Long? {
    val afterLastSlip = getLastSlipDate()?.plus(1)?.unixTime
    return listOfNotNull(quitSince, afterLastSlip).maxOrNull()
}

/**
 * Returns how long the habit has been kept clean at the given moment (local wall-clock
 * milliseconds). If the user slipped today, or the quit date is in the future, returns zero.
 */
fun Habit.getCleanTime(nowLocalMillis: Long): CleanTime {
    val since = getCleanSince() ?: return CleanTime(0, 0, 0)
    return CleanTime.fromMillis(nowLocalMillis - since)
}

/**
 * Returns the number of slips for each month, grouped by day of week, in the same format as
 * [EntryList.computeWeekdayFrequency].
 */
fun Habit.computeSlipWeekdayFrequency(): HashMap<LocalDate, Array<Int>> {
    val slips = EntryList()
    originalEntries.getKnown()
        .filter { it.value == Entry.NO }
        .forEach { slips.add(Entry(it.date, Entry.YES_MANUAL)) }
    return slips.computeWeekdayFrequency(isNumerical = false)
}
