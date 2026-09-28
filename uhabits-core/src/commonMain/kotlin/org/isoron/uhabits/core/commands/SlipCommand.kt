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
package org.isoron.uhabits.core.commands

import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.HabitList

/**
 * A command that adds or removes a slip of a quit habit. Like [CreateRepetitionCommand], it only
 * affects the data of a single habit.
 */
sealed interface SlipCommand : Command {
    val habitList: HabitList
    val habit: Habit
    val timestamp: Long
}

/**
 * Records that the user slipped at the given moment (local wall-clock milliseconds).
 */
data class AddSlipCommand(
    override val habitList: HabitList,
    override val habit: Habit,
    override val timestamp: Long
) : SlipCommand {
    override fun run() {
        habit.slips.add(timestamp)
        habit.recompute()
        habitList.resort()
    }
}

/**
 * Removes the slip recorded at the given moment (local wall-clock milliseconds).
 */
data class DeleteSlipCommand(
    override val habitList: HabitList,
    override val habit: Habit,
    override val timestamp: Long
) : SlipCommand {
    override fun run() {
        habit.slips.remove(timestamp)
        habit.recompute()
        habitList.resort()
    }
}
