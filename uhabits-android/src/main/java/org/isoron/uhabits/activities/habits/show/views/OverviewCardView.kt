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
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.screens.habits.show.views.OverviewCardState
import org.isoron.uhabits.databinding.ShowHabitOverviewBinding
import org.isoron.uhabits.utils.StyledResources
import kotlin.math.abs
import kotlin.math.roundToInt

class OverviewCardView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val binding = ShowHabitOverviewBinding.inflate(LayoutInflater.from(context), this)

    private fun formatPercentageDiff(percentageDiff: Float): String {
        return String.format(
            "%s%.0f%%",
            if (percentageDiff >= 0) "+" else "\u2212",
            abs(percentageDiff) * 100
        )
    }

    fun setState(state: OverviewCardState) {
        val theme = state.theme
        val ink = theme.habitInk(state.color).toInt()
        val inkMuted = StyledResources(context).getColor(R.attr.dsInkMuted)
        binding.monthDiffLabel.setTextColor(if (state.scoreMonthDiff >= 0) ink else inkMuted)
        binding.monthDiffLabel.text = formatPercentageDiff(state.scoreMonthDiff)
        binding.scoreRing.apply {
            setColor(theme.habitFill(state.color).toInt())
            setInactiveColor(theme.habitTint(state.color).toInt())
            setTextColor(ink)
            setBoldText(true)
            setText(String.format("%.0f%%", state.scoreToday * 100))
            setPercentage(state.scoreToday)
            contentDescription = resources.getString(
                R.string.cd_habit_strength,
                (state.scoreToday * 100).roundToInt()
            )
        }
        binding.totalCountLabel.setTextColor(ink)
        binding.totalCountLabel.text = state.totalCount.toString()
        binding.totalCountTitle.setText(if (state.isQuit) R.string.slips else R.string.total)
        binding.yearDiffLabel.setTextColor(if (state.scoreYearDiff >= 0) ink else inkMuted)
        binding.yearDiffLabel.text = formatPercentageDiff(state.scoreYearDiff)
        postInvalidate()
    }
}
