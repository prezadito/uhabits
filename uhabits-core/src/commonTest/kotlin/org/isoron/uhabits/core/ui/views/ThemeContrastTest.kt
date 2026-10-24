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
import org.isoron.uhabits.core.models.HUE_CANONICAL_INDEX
import kotlin.test.Test
import kotlin.test.assertTrue

/** Locks in the WCAG contrast guarantees of the redesigned palette. */
class ThemeContrastTest {
    private val themes = mapOf(
        "light" to LightTheme(),
        "dark" to DarkTheme(),
        "pure black" to PureBlackTheme()
    )

    private fun check(label: String, foreground: Color, background: Color, minimum: Double) {
        val ratio = foreground.wcagContrast(background)
        assertTrue(ratio >= minimum, "$label: contrast $ratio is below $minimum")
    }

    @Test
    fun wcagContrastMatchesKnownValues() {
        assertTrue(Color.BLACK.wcagContrast(Color.WHITE) in 20.99..21.01)
        assertTrue(Color.WHITE.wcagContrast(Color.WHITE) in 0.99..1.01)
    }

    @Test
    fun textAndControlsOnSurfaces() {
        for ((name, theme) in themes) {
            for (surface in listOf(theme.surfaceColor, theme.surfaceRaisedColor)) {
                check("$name ink", theme.inkColor, surface, 4.5)
                check("$name inkMuted", theme.inkMutedColor, surface, 4.5)
                check("$name lineStrong", theme.lineStrongColor, surface, 3.0)
                check("$name accent", theme.accentColor, surface, 4.5)
            }
        }
    }

    @Test
    fun habitColorsInEveryTheme() {
        for ((name, theme) in themes) {
            for (i in 0..HUE_CANONICAL_INDEX.max()) {
                for (surface in listOf(theme.surfaceColor, theme.surfaceRaisedColor)) {
                    check("$name habitInk($i)", theme.habitInk(i), surface, 4.5)
                }
                val fill = theme.habitFill(i)
                check("$name habitFill($i)", fill, theme.surfaceRaisedColor, 3.0)
                check("$name onHabit on fill($i)", theme.onHabitColor, fill, 3.0)
                check("$name onHabitText on fill($i)", theme.onHabitTextColor, fill, 4.5)
                val tint = theme.habitTint(i)
                check("$name ink on tint($i)", theme.inkColor, tint, 4.5)
                check("$name inkMuted on tint($i)", theme.inkMutedColor, tint, 4.5)
            }
        }
    }
}
