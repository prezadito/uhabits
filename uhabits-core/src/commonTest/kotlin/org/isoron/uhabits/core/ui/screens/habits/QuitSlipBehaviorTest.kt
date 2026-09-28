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
import org.isoron.platform.time.setLocalNow
import org.isoron.uhabits.core.BaseUnitTest
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.models.quitTimestamp
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuitSlipBehaviorTest : BaseUnitTest() {
    private lateinit var habit: Habit
    private lateinit var behavior: QuitSlipBehavior
    private val screen = FakeScreen()

    @BeforeTest
    override fun setUp() {
        super.setUp()
        habit = fixtures.createQuitHabit()
        habitList.add(habit)
        behavior = QuitSlipBehavior(habitList, commandRunner, screen)
    }

    @AfterTest
    fun tearDown() {
        setLocalNow(null)
    }

    @Test
    fun testTapToday_addsSlipNow() {
        val today = getToday()
        setLocalNow(quitTimestamp(today, 13, 20))
        behavior.onTap(habit, today)
        assertEquals(listOf(quitTimestamp(today, 13, 20)), habit.slips.getByDate(today))
        assertEquals(Entry.NO, habit.computedEntries.get(today).value)
        assertNull(screen.pickerDate)
        assertNull(screen.listDate)
    }

    @Test
    fun testTapPastDay_asksForTime() {
        val date = getToday().minus(3)
        behavior.onTap(habit, date)
        assertEquals(date, screen.pickerDate)
        assertTrue(habit.slips.getByDate(date).isEmpty())
        screen.pickerCallback!!(22, 10)
        assertEquals(listOf(quitTimestamp(date, 22, 10)), habit.slips.getByDate(date))
        assertEquals(Entry.NO, habit.computedEntries.get(date).value)
    }

    @Test
    fun testTapDayWithSlips_showsList() {
        val date = getToday().minus(5)
        behavior.onTap(habit, date)
        assertEquals(date, screen.listDate)
        assertEquals(listOf(quitTimestamp(date, 9, 15), quitTimestamp(date, 18, 45)), screen.listSlips)
        assertFalse(screen.listIsToday!!)

        screen.onDelete!!(quitTimestamp(date, 9, 15))
        assertEquals(listOf(quitTimestamp(date, 18, 45)), habit.slips.getByDate(date))

        screen.onAdd!!()
        assertEquals(date, screen.pickerDate)
        screen.pickerCallback!!(23, 0)
        assertEquals(
            listOf(quitTimestamp(date, 18, 45), quitTimestamp(date, 23, 0)),
            habit.slips.getByDate(date)
        )
    }

    @Test
    fun testTapTodayWithSlips_addsAnotherSlipNow() {
        val today = getToday()
        setLocalNow(quitTimestamp(today, 8, 0))
        behavior.onTap(habit, today)
        behavior.onTap(habit, today)
        assertEquals(today, screen.listDate)
        assertTrue(screen.listIsToday!!)
        setLocalNow(quitTimestamp(today, 11, 0))
        screen.onAdd!!()
        assertEquals(
            listOf(quitTimestamp(today, 8, 0), quitTimestamp(today, 11, 0)),
            habit.slips.getByDate(today)
        )
    }

    @Test
    fun testNotesSaved_keepsSlips() {
        val date = getToday().minus(5)
        behavior.onNotesSaved(habit, date, Entry.NO, "Party")
        assertEquals("Party", habit.computedEntries.get(date).notes)
        assertEquals(2, habit.slips.getByDate(date).size)
        assertNull(screen.listDate)
    }

    @Test
    fun testNotesSaved_markingSlipAsksForTime() {
        val date = getToday().minus(3)
        behavior.onNotesSaved(habit, date, Entry.NO, "Oops")
        assertEquals("Oops", habit.computedEntries.get(date).notes)
        assertEquals(date, screen.pickerDate)
    }

    @Test
    fun testNotesSaved_markingCleanShowsSlips() {
        val date = getToday().minus(5)
        behavior.onNotesSaved(habit, date, Entry.YES_MANUAL, "")
        assertEquals(date, screen.listDate)
        assertEquals(2, habit.slips.getByDate(date).size)
    }

    class FakeScreen : QuitSlipBehavior.Screen {
        var pickerDate: LocalDate? = null
        var pickerCallback: ((Int, Int) -> Unit)? = null
        var listDate: LocalDate? = null
        var listSlips: List<Long>? = null
        var listIsToday: Boolean? = null
        var onAdd: (() -> Unit)? = null
        var onDelete: ((Long) -> Unit)? = null

        override fun showSlipTimePicker(
            date: LocalDate,
            color: PaletteColor,
            callback: (hour: Int, minute: Int) -> Unit
        ) {
            pickerDate = date
            pickerCallback = callback
        }

        override fun showSlipList(
            date: LocalDate,
            slips: List<Long>,
            color: PaletteColor,
            isToday: Boolean,
            onAdd: () -> Unit,
            onDelete: (timestamp: Long) -> Unit
        ) {
            listDate = date
            listSlips = slips
            listIsToday = isToday
            this.onAdd = onAdd
            this.onDelete = onDelete
        }
    }
}
