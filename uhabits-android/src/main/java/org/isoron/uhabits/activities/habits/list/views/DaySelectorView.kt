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
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec
import java.util.Locale

/**
 * Row with the last [DAY_COUNT] days, ending today, used by the compact view of the habit list.
 * Tapping a day selects it; the habit cards then show and edit that day.
 */
class DaySelectorView(context: Context) : View(context) {

    /** Days before today of the selected day, from 0 (today) to [DAY_COUNT] - 1. */
    var selectedOffset = 0
        set(value) {
            field = value.coerceIn(0, DAY_COUNT - 1)
            invalidate()
        }

    var onSelect: (Int) -> Unit = {}

    private val dateFormatter = JavaLocalDateFormatter(Locale.getDefault())
    private val pill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = sres.getColor(R.attr.dsAccent) }
    private val ink = sres.getColor(R.attr.dsInk)
    private val inkMuted = sres.getColor(R.attr.dsInkMuted)
    private val accent = sres.getColor(R.attr.dsAccent)
    private val onAccent = sres.getColor(R.attr.dsSurface)
    private val rect = RectF()

    private val weekdayPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(12f)
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.04f
    }

    private val dayPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(14f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    init {
        setBackgroundColor(sres.getColor(R.attr.dsSurface))
        isClickable = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = dim(R.dimen.checkmarkHeight)
        setMeasuredDimension(widthMeasureSpec, height.toMeasureSpec(EXACTLY))
    }

    /** Day [index] counts from the oldest day shown (0) to today (DAY_COUNT - 1). */
    private fun columnRect(index: Int): RectF {
        val margin = dp(MARGIN_DP)
        val columnWidth = (width - 2 * margin) / DAY_COUNT
        val left = margin + index * columnWidth
        rect.set(left, 0f, left + columnWidth, height.toFloat())
        return rect
    }

    override fun onDraw(canvas: Canvas) {
        val today = getToday()
        for (index in 0 until DAY_COUNT) {
            val offset = DAY_COUNT - 1 - index
            val date = today.minus(offset)
            val selected = offset == selectedOffset
            val r = columnRect(index)
            if (selected) {
                val pillRect = RectF(r.left + dp(2f), r.top + dp(4f), r.right - dp(2f), r.bottom - dp(4f))
                canvas.drawRoundRect(pillRect, dp(12f), dp(12f), pill)
            }
            weekdayPaint.color = if (selected) onAccent else if (offset == 0) accent else inkMuted
            dayPaint.color = if (selected) onAccent else if (offset == 0) accent else ink
            val weekday = dateFormatter.shortWeekdayName(date).uppercase()
            drawCentered(canvas, weekday, weekdayPaint, r.centerX(), r.centerY() - dp(8f))
            drawCentered(canvas, date.day.toString(), dayPaint, r.centerX(), r.centerY() + dp(9f))
        }
    }

    private fun drawCentered(canvas: Canvas, text: String, paint: TextPaint, cx: Float, cy: Float) {
        val fm = paint.fontMetrics
        canvas.drawText(text, cx, cy - (fm.ascent + fm.descent) / 2, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val margin = dp(MARGIN_DP)
            val columnWidth = (width - 2 * margin) / DAY_COUNT
            val index = ((event.x - margin) / columnWidth).toInt()
            if (index in 0 until DAY_COUNT) {
                val offset = DAY_COUNT - 1 - index
                selectedOffset = offset
                onSelect(offset)
                performClick()
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean = super.performClick()

    companion object {
        const val DAY_COUNT = 7
        private const val MARGIN_DP = 16f
    }
}
