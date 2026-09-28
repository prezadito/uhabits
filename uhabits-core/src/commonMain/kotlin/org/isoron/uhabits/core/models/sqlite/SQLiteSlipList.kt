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
package org.isoron.uhabits.core.models.sqlite

import org.isoron.uhabits.core.database.SlipRepository
import org.isoron.uhabits.core.models.SlipList

class SQLiteSlipList(val repository: SlipRepository) : SlipList() {
    var habitId: Long? = null
    private var isLoaded = false

    private fun loadRecords() {
        if (isLoaded) return
        val habitId = habitId ?: throw IllegalStateException("habitId must be set")
        for (timestamp in repository.findAllByHabitId(habitId)) super.add(timestamp)
        isLoaded = true
    }

    override fun getAll(): List<Long> {
        loadRecords()
        return super.getAll()
    }

    override fun add(timestamp: Long) {
        loadRecords()
        repository.insert(habitId!!, timestamp)
        super.add(timestamp)
    }

    override fun remove(timestamp: Long) {
        loadRecords()
        repository.delete(habitId!!, timestamp)
        super.remove(timestamp)
    }

    override fun clear() {
        super.clear()
        repository.deleteByHabitId(habitId!!)
    }
}
