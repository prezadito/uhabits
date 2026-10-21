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
import android.widget.HorizontalScrollView
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.core.preferences.HomeViewMode
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec
import java.util.Locale
import kotlin.math.abs

/**
 * Scrolling row with the last [DAY_COUNT] days, ending today, used by the compact, grid and
 * monthly views of the habit list. It starts scrolled to today; swiping goes back up to a month.
 * Tapping a day selects it; the habit cards then show and edit that day.
 */
class DaySelectorView(context: Context) : HorizontalScrollView(context) {

    /** Days before today of the selected day, from 0 (today) to [DAY_COUNT] - 1. */
    var selectedOffset: Int
        get() = strip.selectedOffset
        set(value) {
            strip.selectedOffset = value
        }

    var onSelect: (Int) -> Unit
        get() = strip.onSelect
        set(value) {
            strip.onSelect = value
        }

    private val strip = Strip(context)
    private var scrolledToToday = false

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        layoutDirection = LAYOUT_DIRECTION_LTR
        setBackgroundColor(sres.getColor(R.attr.dsSurface))
        addView(strip, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    /** Redraws the dates, e.g. after midnight, and scrolls back to today. */
    fun refresh() {
        strip.invalidate()
        scrolledToToday = false
        requestLayout()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!scrolledToToday && width > 0) {
            scrolledToToday = true
            scrollTo(strip.width, 0)
        }
    }

    private class Strip(context: Context) : View(context) {
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
        private var downX = 0f

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
            isClickable = true
        }

        private val columnWidth get() = dim(R.dimen.checkmarkWidth)
        private val margin get() = dp(MARGIN_DP)

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val width = 2 * margin + DAY_COUNT * columnWidth
            setMeasuredDimension(
                width.toInt().toMeasureSpec(EXACTLY),
                dim(R.dimen.checkmarkHeight).toMeasureSpec(EXACTLY)
            )
        }

        /** Day [index] counts from the oldest day shown (0) to today (DAY_COUNT - 1). */
        private fun columnRect(index: Int): RectF {
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
            when (event.action) {
                MotionEvent.ACTION_DOWN -> downX = event.x
                MotionEvent.ACTION_UP -> {
                    if (abs(event.x - downX) < dp(TAP_SLOP_DP)) {
                        val index = ((event.x - margin) / columnWidth).toInt()
                        if (index in 0 until DAY_COUNT) {
                            val offset = DAY_COUNT - 1 - index
                            selectedOffset = offset
                            onSelect(offset)
                            performClick()
                        }
                    }
                }
            }
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean = super.performClick()
    }

    companion object {
        const val DAY_COUNT = HomeViewMode.DAY_ROW_LENGTH
        private const val MARGIN_DP = 16f
        private const val TAP_SLOP_DP = 8f
    }
}
