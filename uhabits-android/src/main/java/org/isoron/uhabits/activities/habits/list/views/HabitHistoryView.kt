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
import android.text.TextPaint
import android.view.View
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.preferences.HomeViewMode
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.HabitHistoryLayout
import org.isoron.uhabits.core.ui.screens.habits.list.HistoryCell
import org.isoron.uhabits.core.ui.screens.habits.list.HistoryCellStatus
import org.isoron.uhabits.utils.ColorUtils.setAlpha
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import kotlin.math.max

/**
 * Read-only history of a habit, used by the grid and monthly views of the habit list: a dense
 * heatmap of recent weeks, or the current month as a small calendar.
 */
class HabitHistoryView(
    context: Context,
    private val prefs: Preferences
) : View(context) {

    var habit: Habit? = null
        set(value) {
            field = value
            invalidate()
        }

    var colors = TileColors(0, 0, 0)
        set(value) {
            field = value
            invalidate()
        }

    var mode = HomeViewMode.GRID
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
            invalidate()
        }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1.5f)
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sp(10f)
        textAlign = Paint.Align.CENTER
    }
    private val rect = RectF()
    private val inkMuted = sres.getColor(R.attr.dsInkMuted)
    private val ink = sres.getColor(R.attr.dsInk)
    private val onHabitText = sres.getColor(R.attr.dsOnHabitText)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = when (mode) {
            HomeViewMode.MONTHLY -> {
                val rows = HabitHistoryLayout.monthRowCount(getToday(), prefs.firstWeekday)
                (rows * MONTH_CELL_DP + (rows - 1) * MONTH_GAP_DP)
            }
            else -> 7 * GRID_CELL_DP + 6 * GRID_GAP_DP
        }
        setMeasuredDimension(width, dp(height.toFloat()).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        val habit = habit ?: return
        when (mode) {
            HomeViewMode.MONTHLY -> drawMonth(canvas, habit)
            else -> drawGrid(canvas, habit)
        }
    }

    private fun drawGrid(canvas: Canvas, habit: Habit) {
        val gap = dp(GRID_GAP_DP.toFloat())
        val cellHeight = dp(GRID_CELL_DP.toFloat())
        val columns = max(1, ((width + gap) / (dp(GRID_CELL_DP.toFloat()) + gap)).toInt())
        val cellWidth = (width - (columns - 1) * gap) / columns
        val radius = dp(2f)
        val cells = HabitHistoryLayout.gridCells(getToday(), prefs.firstWeekday, columns)
        for (cell in cells) {
            placeCell(cell, cellWidth, cellHeight, gap)
            paint.color = fillFor(HabitHistoryLayout.status(habit, cell.date))
            canvas.drawRoundRect(rect, radius, radius, paint)
            if (cell.date == getToday()) drawTodayMark(canvas, radius)
        }
    }

    private fun drawMonth(canvas: Canvas, habit: Habit) {
        val gap = dp(MONTH_GAP_DP.toFloat())
        val cellHeight = dp(MONTH_CELL_DP.toFloat())
        val cellWidth = (width - 6 * gap) / 7
        val radius = dp(4f)
        val today = getToday()
        for (cell in HabitHistoryLayout.monthCells(today, prefs.firstWeekday)) {
            placeCell(cell, cellWidth, cellHeight, gap)
            val isFuture = cell.date.isNewerThan(today)
            val status = HabitHistoryLayout.status(habit, cell.date)
            paint.color = if (isFuture) setAlpha(colors.tint, 0.4f) else fillFor(status)
            canvas.drawRoundRect(rect, radius, radius, paint)
            textPaint.color = when {
                isFuture -> inkMuted
                status == HistoryCellStatus.DONE -> onHabitText
                else -> ink
            }
            val baseline = rect.centerY() - (textPaint.ascent() + textPaint.descent()) / 2
            canvas.drawText(cell.date.day.toString(), rect.centerX(), baseline, textPaint)
            if (cell.date == today) drawTodayMark(canvas, radius)
        }
    }

    private fun placeCell(cell: HistoryCell, w: Float, h: Float, gap: Float) {
        val left = cell.column * (w + gap)
        val top = cell.row * (h + gap)
        rect.set(left, top, left + w, top + h)
    }

    private fun drawTodayMark(canvas: Canvas, radius: Float) {
        strokePaint.color = ink
        val inset = strokePaint.strokeWidth / 2
        canvas.drawRoundRect(
            rect.left + inset,
            rect.top + inset,
            rect.right - inset,
            rect.bottom - inset,
            radius,
            radius,
            strokePaint
        )
    }

    private fun fillFor(status: HistoryCellStatus): Int = when (status) {
        HistoryCellStatus.DONE -> colors.fill
        HistoryCellStatus.AUTO -> setAlpha(colors.fill, 0.55f)
        HistoryCellStatus.PARTIAL -> setAlpha(colors.fill, 0.35f)
        HistoryCellStatus.SKIPPED -> setAlpha(colors.fill, 0.2f)
        HistoryCellStatus.MISSED -> colors.tint
        HistoryCellStatus.UNKNOWN -> setAlpha(colors.tint, 0.6f)
    }

    companion object {
        private const val GRID_CELL_DP = 10
        private const val GRID_GAP_DP = 2
        private const val MONTH_CELL_DP = 24
        private const val MONTH_GAP_DP = 3
    }
}
