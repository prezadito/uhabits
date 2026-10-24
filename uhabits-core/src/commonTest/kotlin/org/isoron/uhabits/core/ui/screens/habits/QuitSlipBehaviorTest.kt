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
    fun testTapToday_asksForTimeStartingNow() {
        val today = getToday()
        setLocalNow(quitTimestamp(today, 13, 20))
        behavior.onTap(habit, today)
        assertEquals(today, screen.pickerDate)
        assertEquals(13 to 20, screen.pickerInitial)
        assertTrue(habit.slips.getByDate(today).isEmpty())
        screen.pickerCallback!!(10, 5)
        assertEquals(listOf(quitTimestamp(today, 10, 5)), habit.slips.getByDate(today))
        assertEquals(Entry.NO, habit.computedEntries.get(today).value)
        assertNull(screen.listDate)
    }

    @Test
    fun testEditSlip_movesSlip() {
        val date = getToday().minus(5)
        behavior.onTap(habit, date)
        screen.onEdit!!(quitTimestamp(date, 9, 15))
        assertEquals(9 to 15, screen.pickerInitial)
        screen.pickerCallback!!(7, 30)
        assertEquals(
            listOf(quitTimestamp(date, 7, 30), quitTimestamp(date, 18, 45)),
            habit.slips.getByDate(date)
        )
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
    fun testTapTodayWithSlips_addsAnotherSlip() {
        val today = getToday()
        setLocalNow(quitTimestamp(today, 8, 0))
        behavior.onTap(habit, today)
        screen.pickerCallback!!(8, 0)
        behavior.onTap(habit, today)
        assertEquals(today, screen.listDate)
        assertTrue(screen.listIsToday!!)
        setLocalNow(quitTimestamp(today, 11, 0))
        screen.onAdd!!()
        assertEquals(11 to 0, screen.pickerInitial)
        screen.pickerCallback!!(11, 0)
        assertEquals(
            listOf(quitTimestamp(today, 8, 0), quitTimestamp(today, 11, 0)),
            habit.slips.getByDate(today)
        )
    }

    @Test
    fun testTapOnDayWithSlips_offersNotesWhenAsked() {
        val date = getToday().minus(5)
        var opened = false
        behavior.onTap(habit, date) { opened = true }
        screen.onEditNotes!!()
        assertTrue(opened)
    }

    @Test
    fun testTapOnDayWithSlips_hasNoNotesActionByDefault() {
        behavior.onTap(habit, getToday().minus(5))
        assertNull(screen.onEditNotes)
    }

    class FakeScreen : QuitSlipBehavior.Screen {
        var pickerDate: LocalDate? = null
        var pickerInitial: Pair<Int, Int>? = null
        var pickerCallback: ((Int, Int) -> Unit)? = null
        var listDate: LocalDate? = null
        var listSlips: List<Long>? = null
        var listIsToday: Boolean? = null
        var onAdd: (() -> Unit)? = null
        var onEdit: ((Long) -> Unit)? = null
        var onDelete: ((Long) -> Unit)? = null
        var onEditNotes: (() -> Unit)? = null

        override fun showSlipTimePicker(
            date: LocalDate,
            color: PaletteColor,
            initialHour: Int,
            initialMinute: Int,
            callback: (hour: Int, minute: Int) -> Unit
        ) {
            pickerDate = date
            pickerInitial = initialHour to initialMinute
            pickerCallback = callback
        }

        override fun showSlipList(
            date: LocalDate,
            slips: List<Long>,
            color: PaletteColor,
            isToday: Boolean,
            onAdd: () -> Unit,
            onEdit: (timestamp: Long) -> Unit,
            onDelete: (timestamp: Long) -> Unit,
            onEditNotes: (() -> Unit)?
        ) {
            this.onEditNotes = onEditNotes
            listDate = date
            listSlips = slips
            listIsToday = isToday
            this.onAdd = onAdd
            this.onEdit = onEdit
            this.onDelete = onDelete
        }
    }
}
