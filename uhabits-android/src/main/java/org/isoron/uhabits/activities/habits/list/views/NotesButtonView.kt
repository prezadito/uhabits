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
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.toMeasureSpec

/**
 * Opens the note editor for one day of a habit. It sits next to the day buttons, which only
 * handle the value of the day, and shows whether the day already has a note.
 */
class NotesButtonView(context: Context) : View(context) {
    var colors = TileColors(Color.BLACK, Color.BLACK, Color.LTGRAY)
        set(value) {
            field = value
            invalidate()
        }

    var date: LocalDate? = null
        set(value) {
            field = value
            updateDescription()
        }

    var notes = ""
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var onEdit: () -> Unit = {}

    private val tile = DayTilePainter(this)

    init {
        setOnClickListener { onEdit() }
    }

    private fun updateDescription() {
        val date = date ?: return
        val id = if (notes.isBlank()) R.string.add_note else R.string.edit_note
        contentDescription = resources.getString(id) + ", " + describeDay(date)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        tile.layout()
        if (notes.isBlank()) {
            tile.fill(canvas, colors.tint)
            tile.icon(canvas, R.drawable.ic_note_add, colors.ink)
        } else {
            tile.fill(canvas, colors.fill)
            tile.icon(canvas, R.drawable.ic_note, tile.onHabit)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.checkmarkHeight)
        val width = resources.getDimensionPixelSize(R.dimen.checkmarkWidth)
        super.onMeasure(width.toMeasureSpec(EXACTLY), height.toMeasureSpec(EXACTLY))
    }
}
