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
 * Formats local wall-clock milliseconds as "yyyy-MM-dd HH:mm".
 */
fun formatLocalDateTime(timestamp: Long): String {
    val date = LocalDate.fromUnixTime(timestamp)
    val minutes = ((timestamp - date.unixTime) / MILLIS_PER_MINUTE).toInt()
    fun pad(n: Int) = n.toString().padStart(2, '0')
    return "${date.year}-${pad(date.month)}-${pad(date.day)} ${pad(minutes / 60)}:${pad(minutes % 60)}"
}

/**
 * Returns the day the user quit, or null if the habit has no quit date.
 */
fun Habit.getQuitDate(): LocalDate? = quitSince?.let { LocalDate.fromUnixTime(it) }

/**
 * Returns the moment of the most recent slip, or null if the user never slipped.
 */
fun Habit.getLastSlip(): Long? = slips.getLatest()

/**
 * Returns the most recent day in which the user slipped, or null if they never did.
 */
fun Habit.getLastSlipDate(): LocalDate? = getLastSlip()?.let { LocalDate.fromUnixTime(it) }

/**
 * Returns the oldest day in which the user slipped, or null if they never did.
 */
fun Habit.getOldestSlipDate(): LocalDate? =
    slips.getAll().lastOrNull()?.let { LocalDate.fromUnixTime(it) }

/**
 * Returns the number of slips recorded on or after the given date. Every slip is counted, so a
 * day with several slips contributes more than one.
 */
fun Habit.countSlips(since: LocalDate? = null): Int =
    slips.getAll().count { since == null || it >= since.unixTime }

/**
 * Returns the moment (local wall-clock milliseconds) from which the current clean period is
 * counted. This is either the quit time or the moment of the most recent slip, whichever is
 * later. Returns null if the habit has neither a quit date nor any slips.
 */
fun Habit.getCleanSince(): Long? = listOfNotNull(quitSince, getLastSlip()).maxOrNull()

/**
 * Returns how long the habit has been kept clean at the given moment (local wall-clock
 * milliseconds). If the quit date or the last slip is in the future, returns zero.
 */
fun Habit.getCleanTime(nowLocalMillis: Long): CleanTime {
    val since = getCleanSince() ?: return CleanTime(0, 0, 0)
    return CleanTime.fromMillis(nowLocalMillis - since)
}

/**
 * Returns, for each day between [from] and [to] (inclusive), an entry whose value is the number
 * of slips on that day multiplied by 1000, in the same format as numerical entries. Entries are
 * sorted from the most recent day to the oldest.
 */
fun Habit.countSlipsByDay(from: LocalDate, to: LocalDate): List<Entry> {
    val counts = slips.getAll()
        .groupingBy { LocalDate.fromUnixTime(it) }
        .eachCount()
    val result = mutableListOf<Entry>()
    var current = to
    while (current >= from) {
        result.add(Entry(current, (counts[current] ?: 0) * 1000))
        current = current.minus(1)
    }
    return result
}

/**
 * Returns the number of slips for each month, grouped by day of week, in the same format as
 * [EntryList.computeWeekdayFrequency].
 */
fun Habit.computeSlipWeekdayFrequency(): HashMap<LocalDate, Array<Int>> {
    val map = hashMapOf<LocalDate, Array<Int>>()
    for (timestamp in slips.getAll()) {
        val date = LocalDate.fromUnixTime(timestamp)
        val weekday = (date.dayOfWeek.daysSinceSunday + 1) % 7
        val list = map.getOrPut(date.startOfMonth()) { arrayOf(0, 0, 0, 0, 0, 0, 0) }
        list[weekday] += 1
    }
    return map
}
