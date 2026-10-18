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
package org.isoron.uhabits.widgets.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.view.View
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.list.views.DayTilePainter
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.utils.dim

/**
 * Today's state as a 36dp tile, drawn with the same rules as the day tiles in the habit list.
 * Glyphs and text are white, and "not done" uses the hue tint at 30% alpha.
 */
class WidgetTileView(context: Context) : View(context) {
    var fill = 0
    var tint = 0
    var state = NO
    var questionMarks = false

    /** Replaces the glyph with text, for numerical and quit habits. */
    var text: String? = null

    private val painter = DayTilePainter(this)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        color = Color.WHITE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = dim(R.dimen.ds_tile).toInt()
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        painter.layout()
        when (state) {
            YES_MANUAL -> painter.fill(canvas, fill)
            YES_AUTO -> {
                painter.fill(canvas, tint)
                painter.innerStroke(canvas, fill, 2f)
            }
            SKIP -> painter.hatch(canvas, tint)
            NO, UNKNOWN -> painter.fill(canvas, tint)
            else -> painter.fill(canvas, tint)
        }
        val label = text
        if (label != null) {
            textPaint.textSize = dim(R.dimen.ds_text_tile_number)
            painter.text(canvas, label, textPaint, painter.rect.centerY())
            return
        }
        val glyph = when (state) {
            YES_MANUAL, YES_AUTO -> R.string.fa_check
            SKIP -> R.string.fa_skipped
            UNKNOWN -> if (questionMarks) R.string.fa_question else R.string.fa_times
            else -> R.string.fa_times
        }
        val size = if (glyph == R.string.fa_check || glyph == R.string.fa_skipped) 15f else 12f
        painter.glyph(canvas, glyph, size, Color.WHITE)
    }
}
