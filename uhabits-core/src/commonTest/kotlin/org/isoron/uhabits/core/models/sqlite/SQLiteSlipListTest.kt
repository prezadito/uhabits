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

import kotlinx.coroutines.test.runTest
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.BaseUnitTest.Companion.buildMemoryDatabase
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.HabitType
import org.isoron.uhabits.core.models.quitTimestamp
import kotlin.test.Test
import kotlin.test.assertEquals

class SQLiteSlipListTest {
    private val today = LocalDate(2015, 1, 25)

    @Test
    fun testAddRemoveAndReload() = runTest {
        val database = buildMemoryDatabase()
        val factory = SQLModelFactory(database)
        val habitList = factory.buildHabitList()
        val habit = factory.buildHabit()
        habit.type = HabitType.QUIT
        habitList.add(habit)

        val first = quitTimestamp(today, 8, 0)
        val second = quitTimestamp(today, 13, 30)
        val third = quitTimestamp(today.minus(1), 22, 0)
        habit.slips.add(first)
        habit.slips.add(second)
        habit.slips.add(third)
        habit.slips.add(second)
        habit.slips.remove(first)

        val reloaded = SQLiteSlipList(factory.slipRepository)
        reloaded.habitId = habit.id
        assertEquals(listOf(second, third), reloaded.getAll())

        habitList.remove(habit)
        assertEquals(emptyList(), factory.slipRepository.findAllByHabitId(habit.id!!))
    }

    @Test
    fun testRecomputeFromDatabase() = runTest {
        val database = buildMemoryDatabase()
        val factory = SQLModelFactory(database)
        val habitList = factory.buildHabitList()
        val habit = factory.buildHabit()
        habit.type = HabitType.QUIT
        habit.quitSince = quitTimestamp(today.minus(3), 0, 0)
        habitList.add(habit)
        factory.slipRepository.insert(habit.id!!, quitTimestamp(today.minus(1), 12, 0))

        val loaded = factory.buildHabitList().getById(habit.id!!)!!
        loaded.recompute()
        assertEquals(Entry.YES_MANUAL, loaded.computedEntries.get(today.minus(2)).value)
        assertEquals(Entry.NO, loaded.computedEntries.get(today.minus(1)).value)
    }
}
