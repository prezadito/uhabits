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
package org.isoron.uhabits.activities.habits.list.views

import android.content.Context
import android.view.View
import android.widget.HorizontalScrollView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.dp

/**
 * Row of chips under the date row: "All" followed by one chip per category. Selecting a chip
 * filters the habit list to that category. The row is hidden when there are no categories.
 */
class CategoryChipsView(context: Context) : HorizontalScrollView(context) {

    /** Called with the chosen category, or null for "All". */
    var onSelect: (String?) -> Unit = {}

    private var categories: List<String> = emptyList()
    private var selected: String? = null

    private val group = ChipGroup(context).apply {
        isSingleLine = true
        isSingleSelection = true
        isSelectionRequired = true
        setPadding(dp(16f).toInt(), 0, dp(16f).toInt(), 0)
    }

    init {
        isHorizontalScrollBarEnabled = false
        visibility = GONE
        addView(group)
    }

    /** Shows [categories] with [selected] checked, or hides the row if there are none. */
    fun update(categories: List<String>, selected: String?) {
        if (categories == this.categories && selected == this.selected) return
        this.categories = categories
        this.selected = selected
        visibility = if (categories.isEmpty()) GONE else VISIBLE
        group.removeAllViews()
        if (categories.isEmpty()) return
        addChip(context.getString(R.string.category_all), null, selected == null)
        for (category in categories) addChip(category, category, category == selected)
    }

    private fun addChip(label: String, category: String?, checked: Boolean) {
        val chip = Chip(context, null, com.google.android.material.R.attr.chipStyle).apply {
            id = View.generateViewId()
            text = label
            isCheckable = true
            isCheckedIconVisible = false
            isChecked = checked
            setOnClickListener { onSelect(category) }
        }
        group.addView(chip)
    }
}
