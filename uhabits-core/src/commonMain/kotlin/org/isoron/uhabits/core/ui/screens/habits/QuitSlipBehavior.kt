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
import org.isoron.platform.time.getLocalNow
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.commands.AddSlipCommand
import org.isoron.uhabits.core.commands.CommandRunner
import org.isoron.uhabits.core.commands.CreateRepetitionCommand
import org.isoron.uhabits.core.commands.DeleteSlipCommand
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.quitTimestamp

/**
 * Handles the user tapping a day of a quit habit, either on the main screen or on the history
 * card. Days without slips get a new slip: at the current time when the day is today, or at a
 * time chosen by the user otherwise. Days that already contain slips show the list of slips, so
 * the user can remove them or add another one.
 */
class QuitSlipBehavior(
    private val habitList: HabitList,
    private val commandRunner: CommandRunner,
    private val screen: Screen
) {
    fun onTap(habit: Habit, date: LocalDate) {
        val slips = habit.slips.getByDate(date)
        if (slips.isEmpty()) {
            addSlip(habit, date)
            return
        }
        screen.showSlipList(
            date = date,
            slips = slips,
            color = habit.color,
            isToday = date == getToday(),
            onAdd = { addSlip(habit, date) },
            onDelete = { timestamp ->
                commandRunner.run(DeleteSlipCommand(habitList, habit, timestamp))
            }
        )
    }

    /**
     * Lets the user edit the notes of a day. Slips are only added or removed through [onTap], so
     * the popup does not offer any value to choose from.
     */
    fun onEditNotes(habit: Habit, date: LocalDate) {
        val notes = habit.computedEntries.get(date).notes
        screen.showSlipNotesPopup(notes, habit.color) { newNotes ->
            commandRunner.run(CreateRepetitionCommand(habitList, habit, date, Entry.UNKNOWN, newNotes))
        }
    }

    private fun addSlip(habit: Habit, date: LocalDate) {
        if (date == getToday()) {
            commandRunner.run(AddSlipCommand(habitList, habit, getLocalNow()))
        } else {
            screen.showSlipTimePicker(date, habit.color) { hour, minute ->
                commandRunner.run(AddSlipCommand(habitList, habit, quitTimestamp(date, hour, minute)))
            }
        }
    }

    interface Screen {
        /**
         * Asks the user for the notes of a day, without offering any value to choose from.
         */
        fun showSlipNotesPopup(notes: String, color: PaletteColor, callback: (notes: String) -> Unit)

        /**
         * Asks the user at which time of the given day they slipped.
         */
        fun showSlipTimePicker(
            date: LocalDate,
            color: PaletteColor,
            callback: (hour: Int, minute: Int) -> Unit
        )

        /**
         * Shows the slips recorded on the given day (local wall-clock milliseconds, from the
         * earliest to the latest), allowing the user to delete them or to add another one.
         */
        fun showSlipList(
            date: LocalDate,
            slips: List<Long>,
            color: PaletteColor,
            isToday: Boolean,
            onAdd: () -> Unit,
            onDelete: (timestamp: Long) -> Unit
        )
    }
}
