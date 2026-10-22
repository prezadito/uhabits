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
package org.isoron.uhabits.acceptance.steps

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import org.isoron.uhabits.BaseUserInterfaceTest
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor

object EditHabitSteps {
    fun clickSave() {
        Espresso.onView(ViewMatchers.withId(R.id.buttonSave)).perform(ViewActions.click())
    }

    fun pickFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.save)).perform(ViewActions.click())
    }

    fun pickMonthFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.xTimesPerMonthRadioButton))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.xTimesPerMonthTextView))
            .perform(ViewActions.replaceText("1"))
        Espresso.onView(ViewMatchers.withText(R.string.save)).perform(ViewActions.click())
    }

    fun pickDailyFrequency() {
        Espresso.onView(ViewMatchers.withId(R.id.boolean_frequency_picker))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.everyDayRadioButton))
            .perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.save)).perform(ViewActions.click())
    }

    fun pickColor(color: Int) {
        // The form scrolls when a field further down gets focus, so bring the button back into view.
        Espresso.onView(ViewMatchers.withId(R.id.colorButton))
            .perform(ViewActions.scrollTo(), ViewActions.click())
        // Swatches are described by hue name, so map the classic palette index to its hue.
        val hueNames = InstrumentationRegistry.getInstrumentation().targetContext
            .resources.getStringArray(R.array.hue_names)
        val name = hueNames[PaletteColor(color).hue]
        BaseUserInterfaceTest.device.findObject(By.descStartsWith(name)).click()
    }

    fun typeName(name: String) {
        typeTextWithId(R.id.nameInput, name)
    }

    fun typeQuestion(name: String) {
        typeTextWithId(R.id.questionInput, name)
    }

    fun typeDescription(description: String) {
        typeTextWithId(R.id.notesInput, description)
    }

    fun setReminder() {
        Espresso.onView(ViewMatchers.withId(R.id.reminderTimePicker)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.done_button)).perform(ViewActions.click())
    }

    fun clickReminderDays() {
        Espresso.onView(ViewMatchers.withId(R.id.reminderDatePicker)).perform(ViewActions.click())
    }

    fun unselectAllDays() {
        Espresso.onView(ViewMatchers.withText("Saturday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Sunday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Monday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Tuesday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Wednesday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Thursday")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("Friday")).perform(ViewActions.click())
    }

    private fun typeTextWithId(id: Int, name: String) {
        Espresso.onView(ViewMatchers.withId(id)).perform(
            ViewActions.clearText(),
            ViewActions.typeText(name),
            ViewActions.closeSoftKeyboard()
        )
    }
}
