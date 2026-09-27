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

data class Entry(
    val date: LocalDate,
    val value: Int,
    val notes: String = ""
) {
    val formattedValue: String
        get() = when (value) {
            YES_MANUAL -> "YES_MANUAL"
            YES_AUTO -> "YES_AUTO"
            NO -> "NO"
            SKIP -> "SKIP"
            UNKNOWN -> "UNKNOWN"
            else -> value.toString()
        }

    companion object {
        /**
         * Value indicating that the habit is not applicable for this timestamp.
         */
        const val SKIP = 3

        /**
         * Value indicating that the user has performed the habit at this timestamp.
         */
        const val YES_MANUAL = 2

        /**
         * Value indicating that the user did not perform the habit, but they were not
         * expected to, because of the frequency of the habit.
         */
        const val YES_AUTO = 1

        /**
         * Value indicating that the user did not perform the habit, even though they were
         * expected to perform it.
         */
        const val NO = 0

        /**
         * Value indicating that no data is available for the given timestamp.
         */
        const val UNKNOWN = -1

        /**
         * For quit habits, returns the value a day should have after being toggled: clean days
         * (or unknown days) become slips, and slips become clean again.
         */
        fun nextQuitToggleValue(value: Int): Int = if (value == NO) YES_MANUAL else NO

        /**
         * For quit habits, converts a value chosen by the user into the value that should be
         * stored. Quit habits only store slips (NO); every other value means "clean", which is
         * the default for days without entries, so it is stored as UNKNOWN.
         */
        fun toQuitStoredValue(value: Int): Int = if (value == NO) NO else UNKNOWN

        fun nextToggleValue(
            value: Int,
            isSkipEnabled: Boolean,
            areQuestionMarksEnabled: Boolean
        ): Int {
            return when (value) {
                YES_AUTO -> YES_MANUAL
                YES_MANUAL -> if (isSkipEnabled) SKIP else NO
                SKIP -> NO
                NO -> if (areQuestionMarksEnabled) UNKNOWN else YES_MANUAL
                UNKNOWN -> YES_MANUAL
                else -> YES_MANUAL
            }
        }
    }
}
