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

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import org.isoron.platform.gui.toInt
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.views.Theme
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.drawNotesIndicator
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import java.time.format.TextStyle
import java.util.Locale

/** The three roles of a habit hue, as Android colors. */
data class TileColors(val fill: Int, val ink: Int, val tint: Int) {
    companion object {
        fun of(theme: Theme, color: PaletteColor) = TileColors(
            fill = theme.habitFill(color).toInt(),
            ink = theme.habitInk(color).toInt(),
            tint = theme.habitTint(color).toInt()
        )
    }
}

/** Weekday and day of month, e.g. "Thursday 29". */
fun View.describeDay(date: LocalDate): String {
    val weekday = date.dayOfWeek.name.let { java.time.DayOfWeek.valueOf(it) }
        .getDisplayName(TextStyle.FULL, Locale.getDefault())
    return resources.getString(R.string.cd_day, weekday, date.day)
}

fun View.describeWithNotes(description: String, notes: String): String {
    if (notes.isBlank()) return description
    return resources.getString(R.string.cd_has_notes, description)
}

/** Draws the 36dp tile and its decorations inside a 48dp day cell. */
class DayTilePainter(private val view: View) {
    val rect = RectF()
    private val radius = view.dim(R.dimen.ds_radius_sm)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glyphPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = view.getFontAwesome()
        textAlign = Paint.Align.CENTER
    }

    val surfaceRaised = view.sres.getColor(R.attr.dsSurfaceRaised)
    val inkMuted = view.sres.getColor(R.attr.dsInkMuted)
    val lineStrong = view.sres.getColor(R.attr.dsLineStrong)
    val onHabit = view.sres.getColor(R.attr.dsOnHabit)
    val onHabitText = view.sres.getColor(R.attr.dsOnHabitText)
    val ink = view.sres.getColor(R.attr.dsInk)

    fun layout() {
        val size = view.dim(R.dimen.ds_tile)
        val cx = view.width / 2f
        val cy = view.height / 2f
        rect.set(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2)
    }

    fun fill(canvas: Canvas, color: Int) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    /** 45 degree stripes of [tint] and the raised surface, 4dp wide. */
    fun hatch(canvas: Canvas, tint: Int) {
        val stripe = view.dp(4f)
        val path = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.save()
        canvas.clipPath(path)
        paint.style = Paint.Style.FILL
        paint.color = tint
        canvas.drawRect(rect, paint)
        paint.color = surfaceRaised
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = stripe
        val h = rect.height()
        var x = rect.left - h
        while (x < rect.right + h) {
            canvas.drawLine(x, rect.bottom, x + h, rect.top, paint)
            x += 2 * stripe
        }
        canvas.restore()
    }

    /** Inner stroke, [width]dp wide, following the tile edge. */
    fun innerStroke(canvas: Canvas, color: Int, width: Float) {
        val w = view.dp(width)
        val r = RectF(rect).apply { inset(w / 2, w / 2) }
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = w
        paint.color = color
        canvas.drawRoundRect(r, radius - w / 2, radius - w / 2, paint)
    }

    fun dashedBorder(canvas: Canvas, color: Int) {
        paint.pathEffect = DashPathEffect(floatArrayOf(view.dp(4f), view.dp(3f)), 0f)
        innerStroke(canvas, color, 1.5f)
        paint.pathEffect = null
    }

    /** Draws a FontAwesome glyph centered on the tile. */
    fun glyph(canvas: Canvas, id: Int, sizeSp: Float, color: Int) {
        glyphPaint.typeface = view.getFontAwesome()
        glyphPaint.textSize = view.sp(sizeSp)
        glyphPaint.color = color
        text(canvas, view.resources.getString(id), glyphPaint, rect.centerY())
    }

    fun text(canvas: Canvas, label: String, p: TextPaint, centerY: Float) {
        val fm = p.fontMetrics
        canvas.drawText(label, rect.centerX(), centerY - (fm.ascent + fm.descent) / 2, p)
    }

    fun notesDot(canvas: Canvas, color: Int, notes: String) {
        view.drawNotesIndicator(paint, canvas, rect, color, notes)
    }
}

internal val SANS_BOLD: Typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
