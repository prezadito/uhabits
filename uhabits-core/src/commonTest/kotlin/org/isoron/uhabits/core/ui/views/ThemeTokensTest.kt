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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeTokensTest {
    private fun check(theme: Theme, v: List<Int>) {
        assertEquals(Color(v[0]), theme.surfaceColor)
        assertEquals(Color(v[1]), theme.surfaceRaisedColor)
        assertEquals(Color(v[2]), theme.inkColor)
        assertEquals(Color(v[3]), theme.inkMutedColor)
        assertEquals(Color(v[4]), theme.lineColor)
        assertEquals(Color(v[5]), theme.lineStrongColor)
        assertEquals(Color(v[6]), theme.accentColor)
        assertEquals(Color(v[7]), theme.onAccentColor)
        assertEquals(Color(v[8]), theme.accentTintColor)
        assertEquals(Color(v[9]), theme.onHabitColor)
        assertEquals(Color(v[10]), theme.onHabitTextColor)
    }

    @Test
    fun light() = check(
        LightTheme(),
        listOf(0xf5f5f5, 0xffffff, 0x1c1c1c, 0x666666, 0xe6e6e6, 0x8a8a8a, 0x1f6fca, 0xffffff, 0xe1eefe, 0xffffff, 0x1c1c1c)
    )

    @Test
    fun dark() = check(
        DarkTheme(),
        listOf(0x161616, 0x212121, 0xf2f2f2, 0xa6a6a6, 0x2e2e2e, 0x7a7a7a, 0x7baff2, 0x161616, 0x222f3f, 0x161616, 0x0f0f0f)
    )

    @Test
    fun pureBlack() = check(
        PureBlackTheme(),
        listOf(0x000000, 0x121212, 0xf2f2f2, 0xa6a6a6, 0x222222, 0x6e6e6e, 0x7baff2, 0x000000, 0x1a2433, 0x000000, 0x000000)
    )
}
