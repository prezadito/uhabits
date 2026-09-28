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
package org.isoron.uhabits.core.database

import org.isoron.platform.io.Database
import org.isoron.platform.io.StepResult

class SlipRepository(private val db: Database) {
    private val findAllByHabitStmt by lazy {
        db.prepareStatement(
            "SELECT timestamp FROM QuitSlips WHERE habit = ? ORDER BY timestamp DESC"
        )
    }

    private val insertStmt by lazy {
        db.prepareStatement("INSERT OR IGNORE INTO QuitSlips(habit, timestamp) VALUES (?, ?)")
    }

    private val deleteStmt by lazy {
        db.prepareStatement("DELETE FROM QuitSlips WHERE habit = ? AND timestamp = ?")
    }

    private val deleteByHabitStmt by lazy {
        db.prepareStatement("DELETE FROM QuitSlips WHERE habit = ?")
    }

    fun findAllByHabitId(habitId: Long): List<Long> {
        findAllByHabitStmt.reset()
        findAllByHabitStmt.bindLong(1, habitId)
        val results = mutableListOf<Long>()
        while (findAllByHabitStmt.step() == StepResult.ROW) {
            results.add(findAllByHabitStmt.getLong(0))
        }
        return results
    }

    fun insert(habitId: Long, timestamp: Long) {
        insertStmt.reset()
        insertStmt.bindLong(1, habitId)
        insertStmt.bindLong(2, timestamp)
        insertStmt.step()
    }

    fun delete(habitId: Long, timestamp: Long) {
        deleteStmt.reset()
        deleteStmt.bindLong(1, habitId)
        deleteStmt.bindLong(2, timestamp)
        deleteStmt.step()
    }

    fun deleteByHabitId(habitId: Long) {
        deleteByHabitStmt.reset()
        deleteByHabitStmt.bindLong(1, habitId)
        deleteByHabitStmt.step()
    }
}
