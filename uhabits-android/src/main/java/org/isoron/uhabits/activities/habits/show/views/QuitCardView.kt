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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.DateUtils
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.CleanTime
import org.isoron.uhabits.core.ui.screens.habits.show.views.QuitCardState
import org.isoron.uhabits.databinding.ShowHabitQuitBinding
import org.isoron.uhabits.utils.formatCleanTime
import org.isoron.uhabits.utils.toSimpleDataFormat
import java.util.Date

class QuitCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitQuitBinding.inflate(LayoutInflater.from(context), this)

    private var cleanSince: Long? = null

    private val ticker = object : Runnable {
        override fun run() {
            updateCleanTime()
            postDelayed(this, REFRESH_INTERVAL)
        }
    }

    fun setState(state: QuitCardState) {
        val androidColor = state.theme.color(state.color).toInt()
        cleanSince = state.cleanSince
        binding.title.setTextColor(androidColor)
        binding.cleanTimeLabel.setTextColor(androidColor)
        binding.cleanSinceLabel.setTextColor(androidColor)
        binding.bestStreakLabel.setTextColor(androidColor)
        binding.recentSlipsLabel.setTextColor(androidColor)

        binding.cleanSinceLabel.text = state.cleanSince?.let {
            "MMMd".toSimpleDataFormat().format(Date(it))
        } ?: "-"
        binding.bestStreakLabel.text = resources.getQuantityString(
            R.plurals.x_days,
            state.longestStreakDays,
            state.longestStreakDays
        )
        binding.recentSlipsLabel.text = state.slipsLast30Days.toString()
        updateCleanTime()
        postInvalidate()
    }

    private fun updateCleanTime() {
        val since = cleanSince
        val time = if (since == null) {
            CleanTime(0, 0, 0)
        } else {
            CleanTime.fromMillis(DateUtils.getLocalTime() - since)
        }
        binding.cleanTimeLabel.text = formatCleanTime(resources, time)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(ticker, REFRESH_INTERVAL)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    companion object {
        private const val REFRESH_INTERVAL = 60_000L
    }
}
