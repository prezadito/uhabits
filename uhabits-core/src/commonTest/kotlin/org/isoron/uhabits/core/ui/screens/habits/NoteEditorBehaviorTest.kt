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
import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteEditorBehaviorTest : BaseUnitTest() {
    private lateinit var habit: Habit
    private lateinit var behavior: NoteEditorBehavior
    private lateinit var date: LocalDate

    @BeforeTest
    override fun setUp() {
        super.setUp()
        date = getToday().minus(3)
        habit = fixtures.createEmptyHabit()
        habitList.add(habit)
        behavior = NoteEditorBehavior(habitList, commandRunner)
    }

    @Test
    fun testSave_onUntouchedDay_keepsValueUnknown() {
        behavior.save(habit, date, "Felt great")
        assertEquals(Entry(date, Entry.UNKNOWN, "Felt great"), habit.computedEntries.get(date))
    }

    @Test
    fun testSave_keepsValueOfDay() {
        habit.originalEntries.add(Entry(date, Entry.SKIP))
        habit.recompute()
        behavior.save(habit, date, "Sick")
        assertEquals(Entry(date, Entry.SKIP, "Sick"), habit.computedEntries.get(date))
    }

    @Test
    fun testSave_trimsNotes() {
        behavior.save(habit, date, "  Felt great \n")
        assertEquals("Felt great", behavior.getNotes(habit, date))
    }

    @Test
    fun testSave_blankNoteRemovesExistingNote() {
        habit.originalEntries.add(Entry(date, Entry.YES_MANUAL, "Felt great"))
        habit.recompute()
        behavior.save(habit, date, "   ")
        assertEquals(Entry(date, Entry.YES_MANUAL, ""), habit.computedEntries.get(date))
    }

    @Test
    fun testSave_blankNoteOnDayWithoutValueLeavesDayUntouched() {
        behavior.save(habit, date, "Felt great")
        behavior.save(habit, date, "")
        assertEquals(Entry(date, Entry.UNKNOWN, ""), habit.computedEntries.get(date))
        assertEquals(0, habit.computedEntries.getKnown().size)
    }

    @Test
    fun testSave_unchangedNoteDoesNothing() {
        behavior.save(habit, date, "")
        assertEquals(0, habit.originalEntries.getKnown().size)
    }

    @Test
    fun testDelete_keepsValue() {
        habit.originalEntries.add(Entry(date, Entry.YES_MANUAL, "Felt great"))
        habit.recompute()
        behavior.delete(habit, date)
        assertEquals(Entry(date, Entry.YES_MANUAL, ""), habit.computedEntries.get(date))
    }

    @Test
    fun testSave_numericalHabitKeepsNumber() {
        val numerical = fixtures.createNumericalHabit()
        habitList.add(numerical)
        numerical.originalEntries.add(Entry(date, 5000))
        numerical.recompute()
        behavior.save(numerical, date, "Heavy")
        assertEquals(Entry(date, 5000, "Heavy"), numerical.computedEntries.get(date))
    }

    @Test
    fun testSave_quitHabitKeepsSlips() {
        val quit = fixtures.createQuitHabit()
        habitList.add(quit)
        val slips = quit.slips.getAll().size
        behavior.save(quit, date, "Party")
        assertEquals("Party", quit.computedEntries.get(date).notes)
        assertEquals(slips, quit.slips.getAll().size)
    }
}
