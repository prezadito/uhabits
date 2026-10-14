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
package org.isoron.uhabits.core.ui.screens.habits.show.views

import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.ui.views.LightTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class BarCardPresenterTest : BaseUnitTest() {
    @Test
    fun testQuitHabit_canGroupSlipsByDay() {
        val habit = fixtures.createQuitHabit()
        val state = BarCardPresenter.buildState(
            habit = habit,
            firstWeekday = 1,
            numericalSpinnerPosition = 0,
            boolSpinnerPosition = 3,
            theme = LightTheme()
        )
        assertEquals(1, state.bucketSize)
        val slipsByDay = state.entries.associate { it.date to it.value }
        assertEquals(2000, slipsByDay[getToday().minus(5)])
        assertEquals(1000, slipsByDay[getToday().minus(12)])
    }
}
