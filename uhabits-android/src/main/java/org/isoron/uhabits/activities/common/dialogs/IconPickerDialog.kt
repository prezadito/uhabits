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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.HabitIcons
import org.isoron.uhabits.utils.StyledResources

/**
 * Lets the user pick one of the bundled Material icons, filtered by a search box, or no icon at
 * all. [onPicked] receives the icon name, or null for "no icon".
 */
class IconPickerDialog(
    private val context: Context,
    private val selected: String?,
    private val tint: Int,
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
                    empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                }
            })
        }
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

    private fun dp(value: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        value.toFloat(),
        context.resources.displayMetrics
    ).toInt()

    private inner class IconAdapter : RecyclerView.Adapter<IconHolder>() {
        private var names = HabitIcons.search(context, "")
        private var cellWidth = dp(CELL_DP)

        /** Cells are sized from the measured grid width, which is unknown until first layout. */
        fun setCellWidth(width: Int) {
            if (width <= 0 || width == cellWidth) return
            cellWidth = width
            notifyDataSetChanged()
        }

        fun setQuery(query: String) {
            names = HabitIcons.search(context, query)
            notifyDataSetChanged()
        }

        override fun getItemCount() = names.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconHolder {
            val cell = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, ICON_SP)
                gravity = Gravity.CENTER
                layoutParams = RecyclerView.LayoutParams(cellWidth, dp(CELL_DP))
            }
            return IconHolder(cell)
        }

        override fun onBindViewHolder(holder: IconHolder, position: Int) {
            val name = names[position]
            holder.cell.layoutParams.width = cellWidth
            val isSelected = name == selected
            holder.cell.typeface = HabitIcons.typeface(context, name)
            holder.cell.text = HabitIcons.glyph(context, name)
            holder.cell.setTextColor(if (isSelected) tint else ink)
            holder.cell.contentDescription = HabitIcons.displayName(name)
            holder.cell.alpha = if (isSelected) 1f else 0.85f
            holder.cell.setOnClickListener {
                onPicked(name)
                dialog.dismiss()
            }
        }
    }

    private class IconHolder(val cell: TextView) : RecyclerView.ViewHolder(cell)

    companion object {
        private const val COLUMNS = 5
        private const val CELL_DP = 56
        private const val GRID_HEIGHT_DP = 240
        private const val ICON_SP = 28f
    }
}
