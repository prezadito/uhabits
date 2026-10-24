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

package org.isoron.uhabits.intents

import android.content.Context
import android.content.Intent
import android.net.Uri
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.activities.habits.edit.EditHabitActivity
import org.isoron.uhabits.activities.habits.note.NoteEditorActivity
import org.isoron.uhabits.activities.habits.show.ShowHabitActivity
import org.isoron.uhabits.activities.intro.IntroActivity
import org.isoron.uhabits.activities.settings.SettingsActivity
import org.isoron.uhabits.core.models.Habit

@Inject
class IntentFactory() {

    fun openDocument() = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    fun startIntroActivity(context: Context) =
        Intent(context, IntroActivity::class.java)

    fun startSettingsActivity(context: Context) =
        Intent(context, SettingsActivity::class.java)

    fun startShowHabitActivity(context: Context, habit: Habit) =
        Intent(context, ShowHabitActivity::class.java).apply {
            data = Uri.parse(habit.uriString)
        }

    fun startNoteEditorActivity(context: Context, habit: Habit, date: LocalDate) =
        Intent(context, NoteEditorActivity::class.java).apply {
            putExtra(NoteEditorActivity.EXTRA_HABIT_ID, habit.id)
            putExtra(NoteEditorActivity.EXTRA_DATE, date.unixTime)
        }

    private fun startEditActivity(context: Context): Intent {
        return Intent(context, EditHabitActivity::class.java)
    }

    fun startEditActivity(context: Context, habit: Habit): Intent {
        val intent = startEditActivity(context)
        intent.putExtra("habitId", habit.id)
        intent.putExtra("habitType", habit.type)
        return intent
    }

    fun startEditActivity(context: Context, habitType: Int): Intent {
        val intent = startEditActivity(context)
        intent.putExtra("habitType", habitType)
        return intent
    }
}
