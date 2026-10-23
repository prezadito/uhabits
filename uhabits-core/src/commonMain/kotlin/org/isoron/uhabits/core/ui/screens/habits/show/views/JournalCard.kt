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

import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.models.Habit

data class JournalEntryState(
    val date: LocalDate,
    val value: Int,
    val notes: String
)

data class JournalCardState(
    val entries: List<JournalEntryState>,
    val isNumerical: Boolean,
    val isQuit: Boolean,
    val unit: String
)

class JournalCardPresenter {
    companion object {
        /**
         * Lists every day of the habit that has a note, from the most recent to the oldest.
         */
        fun buildState(habit: Habit) = JournalCardState(
            entries = habit.computedEntries
                .getKnown()
                .filter { it.notes.isNotEmpty() }
                .sortedByDescending { it.date }
                .map { JournalEntryState(it.date, it.value, it.notes) },
            isNumerical = habit.isNumerical,
            isQuit = habit.isQuit,
            unit = habit.unit
        )
    }
}
