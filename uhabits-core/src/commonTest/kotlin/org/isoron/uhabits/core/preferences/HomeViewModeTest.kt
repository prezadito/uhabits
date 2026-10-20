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

package org.isoron.uhabits.core.preferences

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModeTest {
    @Test
    fun testDayRowModes() {
        assertFalse(HomeViewMode.WEEKLY.usesDayRow)
        assertTrue(HomeViewMode.MONTHLY.usesDayRow)
        assertTrue(HomeViewMode.GRID.usesDayRow)
        assertTrue(HomeViewMode.COMPACT.usesDayRow)
    }

    @Test
    fun testDayRowCoversAMonth() {
        assertEquals(31, HomeViewMode.DAY_ROW_LENGTH)
    }
}
