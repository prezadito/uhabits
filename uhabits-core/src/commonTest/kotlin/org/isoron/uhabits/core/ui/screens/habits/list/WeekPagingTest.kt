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

import kotlin.test.Test
import kotlin.test.assertEquals

class WeekPagingTest {
    @Test
    fun testMaxOffsetIsLastWholeWeekThatFitsTheCache() {
        // 60 cached days, 5 visible columns: offsets up to 55 fit, so 7 weeks back.
        assertEquals(49, WeekPaging.maxOffset(cacheDays = 60, visibleColumns = 5))
        assertEquals(56, WeekPaging.maxOffset(cacheDays = 60, visibleColumns = 4))
        assertEquals(49, WeekPaging.maxOffset(cacheDays = 60, visibleColumns = 11))
    }

    @Test
    fun testMaxOffsetNeverNegative() {
        assertEquals(0, WeekPaging.maxOffset(cacheDays = 60, visibleColumns = 60))
        assertEquals(0, WeekPaging.maxOffset(cacheDays = 60, visibleColumns = 80))
        assertEquals(0, WeekPaging.maxOffset(cacheDays = 3, visibleColumns = 1))
    }

    @Test
    fun testSnapRoundsToNearestWeek() {
        assertEquals(0, WeekPaging.snap(0, max = 49))
        assertEquals(0, WeekPaging.snap(3, max = 49))
        assertEquals(7, WeekPaging.snap(4, max = 49))
        assertEquals(7, WeekPaging.snap(7, max = 49))
        assertEquals(14, WeekPaging.snap(12, max = 49))
    }

    @Test
    fun testSnapIsClampedToTheLimit() {
        assertEquals(49, WeekPaging.snap(60, max = 49))
        assertEquals(0, WeekPaging.snap(-5, max = 49))
        assertEquals(0, WeekPaging.snap(20, max = 0))
    }

    @Test
    fun testPageTowardsDirectionOfFling() {
        // A fling moves a whole number of weeks even if it ends before the halfway point.
        assertEquals(7, WeekPaging.snap(2, max = 49, towards = 1))
        assertEquals(0, WeekPaging.snap(5, max = 49, towards = -1))
        assertEquals(7, WeekPaging.snap(7, max = 49, towards = 1))
        assertEquals(0, WeekPaging.snap(0, max = 49, towards = -1))
    }
}
