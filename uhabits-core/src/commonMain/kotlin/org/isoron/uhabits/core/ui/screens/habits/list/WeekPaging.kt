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

import kotlin.math.max
import kotlin.math.min

/** Weekly view scrolling in whole weeks: offsets are counted in days before today. */
object WeekPaging {
    const val DAYS_PER_WEEK = 7

    /**
     * The largest week offset, in days, that still has data for every visible column, given
     * [cacheDays] days of cached history and [visibleColumns] columns.
     */
    fun maxOffset(cacheDays: Int, visibleColumns: Int): Int {
        val room = max(cacheDays - visibleColumns, 0)
        return room / DAYS_PER_WEEK * DAYS_PER_WEEK
    }

    /**
     * Snaps [offset] to a multiple of 7 within 0..[max]. Without [towards] it rounds to the
     * nearest week. With [towards] set to 1 (older) or -1 (newer), it rounds that way, for flings.
     */
    fun snap(offset: Int, max: Int, towards: Int = 0): Int {
        val weeks = when {
            towards > 0 -> (offset + DAYS_PER_WEEK - 1).floorDiv(DAYS_PER_WEEK)
            towards < 0 -> offset.floorDiv(DAYS_PER_WEEK)
            else -> (offset + DAYS_PER_WEEK / 2).floorDiv(DAYS_PER_WEEK)
        }
        return min(max(weeks * DAYS_PER_WEEK, 0), max(max, 0) / DAYS_PER_WEEK * DAYS_PER_WEEK)
    }
}
