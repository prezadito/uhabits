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

/** Classic paletteIndex 0..19 -> hue 0..11. */
private val CLASSIC_TO_HUE = intArrayOf(0, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 7, 8, 9, 9, 10, 2, 11, 11, 11)

/** Hue -> paletteIndex the picker saves. */
val HUE_CANONICAL_INDEX = intArrayOf(0, 2, 3, 5, 7, 8, 9, 11, 12, 13, 15, 18)

data class PaletteColor(val paletteIndex: Int) {
    val hue: Int
        get() = CLASSIC_TO_HUE[paletteIndex]

    /**
     * Color to store after the user picks [hue] in the color picker. Re-picking
     * the current hue keeps the stored index, so editing never changes it.
     */
    fun afterPickingHue(hue: Int): PaletteColor {
        return if (hue == this.hue) this else fromHue(hue)
    }

    fun toCsvColor(): String {
        return arrayOf(
            "#D32F2F", //  0 red
            "#E64A19", //  1 deep orange
            "#F57C00", //  2 orange
            "#FF8F00", //  3 amber
            "#F9A825", //  4 yellow
            "#AFB42B", //  5 lime
            "#7CB342", //  6 light green
            "#388E3C", //  7 green
            "#00897B", //  8 teal
            "#00ACC1", //  9 cyan
            "#039BE5", // 10 light blue
            "#1976D2", // 11 blue
            "#303F9F", // 12 indigo
            "#5E35B1", // 13 deep purple
            "#8E24AA", // 14 purple
            "#D81B60", // 15 pink
            "#5D4037", // 16 brown
            "#303030", // 17 dark grey
            "#757575", // 18 grey
            "#aaaaaa" // 19 light grey
        )[paletteIndex]
    }

    fun compareTo(other: PaletteColor): Int {
        return paletteIndex.compareTo(other.paletteIndex)
    }

    companion object {
        fun fromHue(hue: Int) = PaletteColor(HUE_CANONICAL_INDEX[hue])
    }
}
