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

package org.isoron.uhabits.activities.habits.note

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.core.widget.ImageViewCompat
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.activities.AndroidThemeSwitcher
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.ui.screens.habits.NoteEditorBehavior
import org.isoron.uhabits.databinding.ActivityNoteEditorBinding
import org.isoron.uhabits.utils.applyBottomInset
import org.isoron.uhabits.utils.applyToolbarInsets
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Full-screen editor for the note of one habit on one day. The note is saved whenever the screen
 * is left, and it never changes the value of the day.
 */
class NoteEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var habit: Habit
    private lateinit var date: LocalDate
    private lateinit var behavior: NoteEditorBehavior
    private var deleted = false

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val component = (application as HabitsApplication).component
        val themeSwitcher = AndroidThemeSwitcher(this, component.preferences)
        themeSwitcher.apply()

        val found = component.habitList.getById(intent.getLongExtra(EXTRA_HABIT_ID, -1))
        if (found == null) {
            finish()
            return
        }
        habit = found
        date = LocalDate.fromUnixTime(intent.getLongExtra(EXTRA_DATE, 0))
        behavior = NoteEditorBehavior(component.habitList, component.commandRunner)

        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.topBar.applyToolbarInsets()
        binding.root.applyBottomInset()
        applyColors(themeSwitcher)

        val javaDate = java.time.LocalDate.of(date.year, date.month, date.day)
        binding.date.text = javaDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
        if (state == null) binding.note.setText(behavior.getNotes(habit, date))

        binding.back.setOnClickListener { finish() }
        binding.delete.setOnClickListener {
            deleted = true
            behavior.delete(habit, date)
            finish()
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = finish()
            }
        )
        if (binding.note.text.isEmpty()) binding.note.requestFocus()
    }

    /** The habit's tint fills the screen, with black or white ink on top, whichever reads better. */
    private fun applyColors(themeSwitcher: AndroidThemeSwitcher) {
        val background = themeSwitcher.currentTheme.habitTint(habit.color).toInt()
        val isLight = ColorUtils.calculateLuminance(background) > 0.5
        val ink = if (isLight) Color.BLACK else Color.WHITE
        binding.root.setBackgroundColor(background)
        window.statusBarColor = background
        window.navigationBarColor = background
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isLight
            isAppearanceLightNavigationBars = isLight
        }
        ImageViewCompat.setImageTintList(binding.back, ColorStateList.valueOf(ink))
        val muted = ColorUtils.setAlphaComponent(ink, MUTED_ALPHA)
        binding.date.setTextColor(muted)
        binding.note.setTextColor(ink)
        binding.note.setHintTextColor(muted)
    }

    override fun onPause() {
        if (::binding.isInitialized && !deleted) {
            behavior.save(habit, date, binding.note.text.toString())
        }
        super.onPause()
    }

    companion object {
        const val EXTRA_HABIT_ID = "habitId"
        const val EXTRA_DATE = "date"
        private const val MUTED_ALPHA = 153
    }
}
