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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View.MeasureSpec.EXACTLY
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper
import org.isoron.platform.time.JavaLocalDateFormatter
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.ScrollableChart
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.utils.MidnightTimer
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.isRTL
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec
import java.util.Locale

class HeaderView(
    context: Context,
    val prefs: Preferences,
    val midnightTimer: MidnightTimer
) : ScrollableChart(context),
    Preferences.Listener,
    MidnightTimer.MidnightListener {

    private var drawer = Drawer()

    var buttonCount: Int = 0
        set(value) {
            field = value
            requestLayout()
        }

    private val columnHelper by lazy {
        object : ExploreByTouchHelper(this) {
            override fun getVirtualViewAt(x: Float, y: Float): Int {
                val index = (0 until buttonCount).firstOrNull { columnRect(it).contains(x, y) }
                return index ?: INVALID_ID
            }

            override fun getVisibleVirtualViews(ids: MutableList<Int>) {
                ids.addAll(0 until buttonCount)
            }

            override fun onPopulateNodeForVirtualView(id: Int, node: AccessibilityNodeInfoCompat) {
                val bounds = Rect()
                columnRect(id).roundOut(bounds)
                node.setBoundsInParent(bounds)
                node.contentDescription = describeColumn(id)
            }

            override fun onPerformActionForVirtualView(id: Int, action: Int, args: Bundle?) = false
        }
    }

    init {
        setScrollerBucketSize(dim(R.dimen.checkmarkWidth).toInt())
        setBackgroundColor(sres.getColor(R.attr.dsSurface))
        elevation = 0f
        ViewCompat.setAccessibilityDelegate(this, columnHelper)
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        return columnHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event)
    }

    private fun describeColumn(index: Int): String {
        val date = getToday().minus(index + dataOffset)
        val day = describeDay(date)
        return if (index + dataOffset == 0) resources.getString(R.string.cd_header_today, day) else day
    }

    /** Bounds of the column for day [index], aligned with the tiles of the habit cards. */
    private fun columnRect(index: Int): RectF {
        val width = dim(R.dimen.checkmarkWidth)
        val height = dim(R.dimen.checkmarkHeight)
        val rect = RectF(0f, 0f, width, height)
        rect.offset(this.width.toFloat() - dp(CARD_INSET_DP), 0f)
        if (prefs.isCheckmarkSequenceReversed) {
            rect.offset(-(index + 1) * width, 0f)
        } else {
            rect.offset((index - buttonCount) * width, 0f)
        }
        if (isRTL()) {
            rect.set(this.width - rect.right, rect.top, this.width - rect.left, rect.bottom)
        }
        return rect
    }

    override fun atMidnight() {
        post { invalidate() }
    }

    override fun onCheckmarkSequenceChanged() {
        updateScrollDirection()
        postInvalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateScrollDirection()
        prefs.addListener(this)
        midnightTimer.addListener(this)
    }

    override fun onDetachedFromWindow() {
        midnightTimer.removeListener(this)
        prefs.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = dim(R.dimen.checkmarkHeight)
        setMeasuredDimension(widthMeasureSpec, height.toMeasureSpec(EXACTLY))
    }

    private fun updateScrollDirection() {
        var direction = -1
        if (prefs.isCheckmarkSequenceReversed) direction *= -1
        if (isRTL()) direction *= -1
        setScrollDirection(direction)
    }

    private inner class Drawer {
        private val dateFormatter = JavaLocalDateFormatter(Locale.getDefault())
        private val pill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = sres.getColor(R.attr.dsAccent) }
        private val inkMuted = sres.getColor(R.attr.dsInkMuted)
        private val ink = sres.getColor(R.attr.dsInk)
        private val accent = sres.getColor(R.attr.dsAccent)

        private val weekdayPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = sp(12f)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT
            letterSpacing = 0.04f
        }

        private val dayPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = sp(14f)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        fun draw(canvas: Canvas) {
            val today = getToday()
            repeat(buttonCount) { index ->
                val rect = columnRect(index)
                val date = today.minus(index + dataOffset)
                val isToday = index + dataOffset == 0
                val dayOfWeek = dateFormatter.shortWeekdayName(date).uppercase()
                if (isToday) {
                    val bar = RectF(rect.left + dp(8f), rect.bottom - dp(3f), rect.right - dp(8f), rect.bottom)
                    canvas.drawRoundRect(bar, dp(1.5f), dp(1.5f), pill)
                }
                weekdayPaint.color = if (isToday) accent else inkMuted
                dayPaint.color = if (isToday) accent else ink
                drawCentered(canvas, dayOfWeek, weekdayPaint, rect.centerX(), rect.centerY() - dp(8f))
                drawCentered(canvas, date.day.toString(), dayPaint, rect.centerX(), rect.centerY() + dp(9f))
            }
        }

        private fun drawCentered(canvas: Canvas, text: String, paint: TextPaint, cx: Float, cy: Float) {
            val fm = paint.fontMetrics
            canvas.drawText(text, cx, cy - (fm.ascent + fm.descent) / 2, paint)
        }
    }

    companion object {
        /** Card margin (16dp) plus card padding (12dp). */
        private const val CARD_INSET_DP = 28f
    }
}
