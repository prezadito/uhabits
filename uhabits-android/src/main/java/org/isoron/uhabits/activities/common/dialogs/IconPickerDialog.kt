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

package org.isoron.uhabits.activities.common.dialogs

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import org.isoron.uhabits.R
import org.isoron.uhabits.core.ui.IconCategory
import org.isoron.uhabits.utils.HabitIcons
import org.isoron.uhabits.utils.StyledResources

/**
 * Lets the user pick one of the bundled icons, filtered by category chips and a search box, or no
 * icon at all. Icons in [usedIcons] (by other habits) are dimmed and badged but still pickable.
 * [onPicked] receives the icon name, or null for "no icon".
 */
class IconPickerDialog(
    private val context: Context,
    private val selected: String?,
    private val tint: Int,
    private val usedIcons: Set<String>,
    private val onPicked: (String?) -> Unit
) {
    private val ink = StyledResources(context).getColor(R.attr.dsInk)
    private val inkMuted = StyledResources(context).getColor(R.attr.dsInkMuted)
    private lateinit var dialog: AlertDialog
    private val adapter = IconAdapter()
    private val empty = TextView(context).apply {
        text = context.getString(R.string.no_icons_found)
        setTextColor(inkMuted)
        gravity = Gravity.CENTER
        visibility = View.GONE
    }

    fun show() {
        val padding = dp(16)
        val search = EditText(context).apply {
            hint = context.getString(R.string.search_icons)
            setSingleLine()
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    adapter.setQuery(s?.toString() ?: "")
                    updateEmptyView()
                }
            })
        }
        val chips = buildCategoryChips()
        val grid = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, COLUMNS)
            adapter = this@IconPickerDialog.adapter
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(GRID_HEIGHT_DP))
            addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
                this@IconPickerDialog.adapter.setCellWidth((right - left) / COLUMNS)
            }
        }
        val content = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding / 2, padding, 0)
            addView(search, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            addView(chips, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
            addView(grid)
            addView(empty, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }
        dialog = AlertDialog.Builder(context)
            .setTitle(R.string.icon)
            .setView(content)
            .setNeutralButton(R.string.no_icon) { _, _ -> onPicked(null) }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.show()
    }

    private fun updateEmptyView() {
        empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    /** "All" plus one chip per category; a search ignores the chip. */
    private fun buildCategoryChips(): HorizontalScrollView {
        val group = ChipGroup(context).apply {
            isSingleLine = true
            isSingleSelection = true
            isSelectionRequired = true
        }
        fun addChip(label: String, category: IconCategory?, checked: Boolean) {
            val chip = Chip(context, null, com.google.android.material.R.attr.chipStyle).apply {
                id = View.generateViewId()
                text = label
                isCheckable = true
                isCheckedIconVisible = false
                isChecked = checked
                setOnClickListener {
                    adapter.setCategory(category)
                    updateEmptyView()
                }
            }
            group.addView(chip)
        }
        addChip(context.getString(R.string.icon_category_all), null, true)
        for (category in IconCategory.entries) {
            addChip(context.getString(categoryLabel(category)), category, false)
        }
        return HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(group)
        }
    }

    private fun categoryLabel(category: IconCategory) = when (category) {
        IconCategory.FITNESS_SPORTS -> R.string.icon_category_fitness_sports
        IconCategory.HEALTH_CARE -> R.string.icon_category_health_care
        IconCategory.MIND_SLEEP -> R.string.icon_category_mind_sleep
        IconCategory.FOOD_DRINK -> R.string.icon_category_food_drink
        IconCategory.LEARNING_WORK -> R.string.icon_category_learning_work
        IconCategory.HOME_CHORES -> R.string.icon_category_home_chores
        IconCategory.MONEY -> R.string.icon_category_money
        IconCategory.FAMILY_FRIENDS -> R.string.icon_category_family_friends
        IconCategory.HOBBIES_CREATIVITY -> R.string.icon_category_hobbies_creativity
        IconCategory.NATURE_TRAVEL -> R.string.icon_category_nature_travel
        IconCategory.QUIT_LIMIT -> R.string.icon_category_quit_limit
        IconCategory.TIME_ROUTINE -> R.string.icon_category_time_routine
        IconCategory.SYMBOLS_MORE -> R.string.icon_category_symbols_more
    }

    private fun dp(value: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    private inner class IconAdapter : RecyclerView.Adapter<IconHolder>() {
        private var query = ""
        private var category: IconCategory? = null
        private var names = HabitIcons.browse(context, query, category)
        private var cellWidth = dp(CELL_DP)

        /** Cells are sized from the measured grid width, which is unknown until first layout. */
        fun setCellWidth(width: Int) {
            if (width <= 0 || width == cellWidth) return
            cellWidth = width
            notifyDataSetChanged()
        }

        fun setQuery(query: String) {
            this.query = query
            refresh()
        }

        fun setCategory(category: IconCategory?) {
            this.category = category
            refresh()
        }

        private fun refresh() {
            names = HabitIcons.browse(context, query, category)
            notifyDataSetChanged()
        }

        override fun getItemCount() = names.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconHolder {
            val glyph = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, ICON_SP)
                gravity = Gravity.CENTER
            }
            val badge = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, BADGE_SP)
                typeface = HabitIcons.typeface(context, null)
                text = HabitIcons.glyph(context, "check_circle")
                setTextColor(inkMuted)
                visibility = View.GONE
            }
            val cell = FrameLayout(context).apply {
                layoutParams = RecyclerView.LayoutParams(cellWidth, dp(CELL_DP))
                addView(glyph, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                addView(
                    badge,
                    FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.TOP or Gravity.END)
                        .apply { setMargins(0, dp(4), dp(6), 0) }
                )
            }
            return IconHolder(cell, glyph, badge)
        }

        override fun onBindViewHolder(holder: IconHolder, position: Int) {
            val name = names[position]
            holder.cell.layoutParams.width = cellWidth
            val isSelected = name == selected
            val inUse = name in usedIcons && !isSelected
            holder.glyph.typeface = HabitIcons.typeface(context, name)
            holder.glyph.text = HabitIcons.glyph(context, name)
            holder.glyph.setTextColor(if (isSelected) tint else ink)
            val label = HabitIcons.displayName(name)
            holder.cell.contentDescription =
                if (inUse) context.getString(R.string.icon_in_use, label) else label
            holder.glyph.alpha = if (isSelected) 1f else if (inUse) IN_USE_ALPHA else 0.85f
            holder.badge.visibility = if (inUse) View.VISIBLE else View.GONE
            holder.cell.setOnClickListener {
                onPicked(name)
                dialog.dismiss()
            }
        }
    }

    private class IconHolder(
        val cell: FrameLayout,
        val glyph: TextView,
        val badge: TextView
    ) : RecyclerView.ViewHolder(cell)

    companion object {
        private const val COLUMNS = 5
        private const val CELL_DP = 56
        private const val GRID_HEIGHT_DP = 240
        private const val ICON_SP = 28f
        private const val BADGE_SP = 14f
        private const val IN_USE_ALPHA = 0.35f
    }
}
