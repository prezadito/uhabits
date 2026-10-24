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

package org.isoron.uhabits.core.ui.screens.habits

import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList

/**
 * Rules of the note editor. Only the note of a day is ever changed: the value stored for that
 * day (yes, no, skip, a number) is kept as it is. A day left with no value and no note is the
 * same as a day that was never touched.
 */
class NoteEditorBehavior(
    private val habitList: HabitList,
    private val commandRunner: CommandRunner
) {
    fun getNotes(habit: Habit, date: LocalDate): String {
        return habit.computedEntries.get(date).notes
    }

    /** Saves the note of the day. A blank note removes the existing one. */
    fun save(habit: Habit, date: LocalDate, notes: String) {
        val trimmed = notes.trim()
        val stored = habit.originalEntries.get(date)
        if (stored.notes == trimmed) return
        commandRunner.run(CreateRepetitionCommand(habitList, habit, date, stored.value, trimmed))
    }

    fun delete(habit: Habit, date: LocalDate) = save(habit, date, "")
}
