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
package org.isoron.uhabits.acceptance

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.BaseUserInterfaceTest.Companion.device
import org.isoron.uhabits.R
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.EDIT_HABIT
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.LIST_HABITS
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SELECT_HABIT_TYPE
import org.isoron.uhabits.acceptance.steps.CommonSteps.Screen.SHOW_HABIT
import org.isoron.uhabits.acceptance.steps.CommonSteps.clickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.launchApp
import org.isoron.uhabits.acceptance.steps.CommonSteps.longClickText
import org.isoron.uhabits.acceptance.steps.CommonSteps.pressBack
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyContentDescriptionContains
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplayGraphs
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDisplaysTextInSequence
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyDoesNotDisplayText
import org.isoron.uhabits.acceptance.steps.CommonSteps.verifyShowsScreen
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.clickSave
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickColor
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.pickFrequency
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeDescription
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeName
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.typeQuestion
import org.isoron.uhabits.acceptance.steps.EditHabitSteps.verifyColorName
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ADD
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.ARCHIVE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.DELETE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.EDIT
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_ARCHIVED
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.TOGGLE_COMPLETED
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.MenuItem.UNARCHIVE
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickMenu
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.clickNotesButton
import org.isoron.uhabits.acceptance.steps.ListHabitsSteps.longPressCheckmarks
import org.isoron.uhabits.core.preferences.HomeViewMode
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class HabitsTest : BaseUserInterfaceTest() {

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabit() {
        shouldCreateHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldCreateHabitBlankDescription() {
        shouldCreateHabit("")
    }

    @Throws(Exception::class)
    private fun shouldCreateHabit(description: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickMenu(ADD)
        verifyShowsScreen(SELECT_HABIT_TYPE)
        clickText("Yes or No")
        verifyShowsScreen(EDIT_HABIT)
        val testName = "Hello world"
        typeName(testName)
        typeQuestion("Did you say hello to the world today?")
        typeDescription(description)
        pickFrequency()
        pickColor(5)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText(testName)
    }

    @Test
    @Throws(Exception::class)
    fun shouldShowHabitStatistics() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(SHOW_HABIT)
        verifyDisplayGraphs()
    }

    @Test
    @Throws(Exception::class)
    fun shouldDeleteHabit() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(DELETE)
        clickText("Yes")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit() {
        shouldEditHabit("this is a test description")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabitBlankDescription() {
        shouldEditHabit("")
    }

    @Throws(Exception::class)
    private fun shouldEditHabit(description: String) {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        typeDescription(description)
        clickSave()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysTextInSequence("Wake up early", "Take a walk", "Meditate")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldEditHabit_fromStatisticsScreen() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Track time")
        verifyShowsScreen(SHOW_HABIT)
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT)
        typeName("Take a walk")
        typeQuestion("Did you take a walk today?")
        pickColor(10)
        clickSave()
        verifyShowsScreen(SHOW_HABIT)
        verifyDisplaysText("Take a walk")
        pressBack()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Take a walk")
        verifyDoesNotDisplayText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldPickAddedColors() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        val added = mapOf(20 to "Brown", 21 to "Sky", 22 to "Mint", 23 to "Rose")
        for ((index, name) in added) {
            longClickText("Track time")
            clickMenu(EDIT)
            verifyShowsScreen(EDIT_HABIT)
            pickColor(index)
            verifyColorName(name)
            clickSave()
            verifyShowsScreen(LIST_HABITS)
        }
        longClickText("Track time")
        clickMenu(EDIT)
        verifyShowsScreen(EDIT_HABIT)
        verifyColorName("Rose")
    }

    @Test
    @Throws(Exception::class)
    fun shouldArchiveAndUnarchiveHabits() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longClickText("Track time")
        clickMenu(ARCHIVE)
        verifyDoesNotDisplayText("Track time")
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
        longClickText("Track time")
        clickMenu(UNARCHIVE)
        clickMenu(TOGGLE_ARCHIVED)
        verifyDisplaysText("Track time")
    }

    @Test
    @Throws(Exception::class)
    fun shouldToggleCheckmarksAndUpdateScore() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        longPressCheckmarks("Wake up early", count = 2)
        clickText("Wake up early")
        verifyShowsScreen(SHOW_HABIT)
        // TODO: find a better way than sleeping in tests
        Thread.sleep(2001L)
        verifyContentDescriptionContains(R.id.scoreRing, "10%")
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotShowNotesButtonInWeeklyView() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        assertNull(device.findObject(By.descContains("Add note")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldAddAndDeleteNoteFromNotesButton() {
        prefs.homeViewMode = HomeViewMode.MONTHLY
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickNotesButton("Wake up early", "Add note")
        Espresso.onView(ViewMatchers.withId(R.id.note)).perform(ViewActions.typeText("Felt great"))
        Espresso.closeSoftKeyboard()
        pressBack()
        verifyShowsScreen(LIST_HABITS)
        clickNotesButton("Wake up early", "Edit note")
        Espresso.onView(ViewMatchers.withId(R.id.note)).check(ViewAssertions.matches(ViewMatchers.withText("Felt great")))
        Espresso.onView(ViewMatchers.withId(R.id.delete)).perform(ViewActions.click())
        device.waitForIdle()
        verifyShowsScreen(LIST_HABITS)
        clickNotesButton("Wake up early", "Add note")
        Espresso.onView(ViewMatchers.withId(R.id.note)).check(ViewAssertions.matches(ViewMatchers.withText("")))
    }

    @Test
    @Throws(Exception::class)
    fun shouldAddNoteFromHistoryDialog() {
        prefs.isShortToggleEnabled = false
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        clickText("Wake up early")
        verifyShowsScreen(SHOW_HABIT)
        Espresso.onView(ViewMatchers.withId(R.id.edit)).perform(ViewActions.scrollTo(), ViewActions.click())
        device.waitForIdle()

        // Today is the bottom-right square of the history editor.
        clickTodayInHistoryEditor()
        Espresso.onView(ViewMatchers.withId(R.id.notesBtn)).perform(ViewActions.click())
        device.waitForIdle()
        Espresso.onView(ViewMatchers.withId(R.id.note)).perform(ViewActions.typeText("From history"))
        Espresso.closeSoftKeyboard()
        pressBack()

        clickTodayInHistoryEditor()
        Espresso.onView(ViewMatchers.withId(R.id.notesBtn)).perform(ViewActions.click())
        device.waitForIdle()
        Espresso.onView(ViewMatchers.withId(R.id.note)).check(ViewAssertions.matches(ViewMatchers.withText("From history")))
    }

    private fun clickTodayInHistoryEditor() {
        device.click((device.displayWidth * 0.785).toInt(), (device.displayHeight * 0.697).toInt())
        device.waitForIdle()
    }

    @Test
    @Throws(Exception::class)
    fun shouldHideCompleted() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
        clickMenu(TOGGLE_COMPLETED)
        verifyDoesNotDisplayText("Track time")
        verifyDisplaysText("Wake up early")
        longPressCheckmarks("Wake up early", count = 1)
        // TODO: find a better way than sleeping in tests
        Thread.sleep(2001L)
        verifyDoesNotDisplayText("Wake up early")
        clickMenu(TOGGLE_COMPLETED)
        verifyDisplaysText("Track time")
        verifyDisplaysText("Wake up early")
    }

    @Test
    @Throws(Exception::class)
    fun shouldAllowMultipleSelection() {
        launchApp()
        verifyShowsScreen(LIST_HABITS)
        verifyDisplaysText("Track time")
        longClickText("Wake up early")
        longClickText("Track time")
        verifyDisplaysText("2")
    }
}
