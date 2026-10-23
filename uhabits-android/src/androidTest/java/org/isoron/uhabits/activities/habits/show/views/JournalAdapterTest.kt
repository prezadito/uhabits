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
package org.isoron.uhabits.activities.habits.show.views

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.BaseViewTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.ui.screens.habits.show.views.JournalCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.JournalEntryState
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@MediumTest
class JournalAdapterTest : BaseViewTest() {
    val PATH = "habits/show/JournalCard"
    private lateinit var tab: View
    private lateinit var list: RecyclerView
    private val adapter = JournalAdapter()

    @Before
    override fun setUp() {
        super.setUp()
        tab = LayoutInflater.from(targetContext).inflate(R.layout.show_habit_tab_journal, null)
        list = tab.findViewById(R.id.journalList)
        list.layoutManager = LinearLayoutManager(targetContext)
        list.adapter = adapter
    }

    @Test
    fun testRender() {
        adapter.setState(
            JournalCardState(
                entries = listOf(
                    JournalEntryState(LocalDate(2026, 10, 23), Entry.YES_MANUAL, "Ran a bit further than usual."),
                    JournalEntryState(LocalDate(2026, 10, 20), Entry.NO, "Rainy day.\nSkipped the morning run."),
                    JournalEntryState(LocalDate(2026, 10, 2), Entry.SKIP, "Travelling.")
                ),
                isNumerical = false,
                isQuit = false,
                unit = ""
            )
        )
        measureView(tab, 800f, 800f)
        assertRenders(tab, "$PATH/render.png")
    }

    @Test
    fun testRenderNumerical() {
        adapter.setState(
            JournalCardState(
                entries = listOf(
                    JournalEntryState(LocalDate(2026, 10, 23), 5500, "Good session.")
                ),
                isNumerical = true,
                isQuit = false,
                unit = "km"
            )
        )
        measureView(tab, 800f, 300f)
        assertRenders(tab, "$PATH/renderNumerical.png")
    }

    @Test
    fun testClickReportsDate() {
        val date = LocalDate(2026, 10, 23)
        adapter.setState(
            JournalCardState(
                entries = listOf(JournalEntryState(date, Entry.YES_MANUAL, "Note")),
                isNumerical = false,
                isQuit = false,
                unit = ""
            )
        )
        measureView(tab, 800f, 300f)
        var clicked: LocalDate? = null
        adapter.onEntryClicked = { clicked = it }
        list.getChildAt(0).performClick()
        assertEquals(date, clicked)
    }
}
