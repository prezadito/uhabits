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

import org.isoron.platform.time.getToday
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JournalCardPresenterTest : BaseUnitTest() {
    @Test
    fun testListsOnlyEntriesWithNotes_newestFirst() {
        val habit = fixtures.createEmptyHabit()
        val today = getToday()
        habit.originalEntries.add(Entry(today.minus(10), Entry.YES_MANUAL, "oldest"))
        habit.originalEntries.add(Entry(today.minus(5), Entry.YES_MANUAL))
        habit.originalEntries.add(Entry(today.minus(2), Entry.NO, "newest"))
        habit.originalEntries.add(Entry(today.minus(7), Entry.SKIP, "middle"))
        habit.recompute()

        val state = JournalCardPresenter.buildState(habit)

        assertEquals(listOf("newest", "middle", "oldest"), state.entries.map { it.notes })
        assertEquals(listOf(today.minus(2), today.minus(7), today.minus(10)), state.entries.map { it.date })
        assertEquals(Entry.NO, state.entries[0].value)
    }

    @Test
    fun testNoNotes_returnsEmptyList() {
        val habit = fixtures.createEmptyHabit()
        habit.originalEntries.add(Entry(getToday(), Entry.YES_MANUAL))
        habit.recompute()
        assertTrue(JournalCardPresenter.buildState(habit).entries.isEmpty())
    }
}
