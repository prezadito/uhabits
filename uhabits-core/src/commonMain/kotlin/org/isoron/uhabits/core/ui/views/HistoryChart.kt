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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Canvas
import org.isoron.platform.gui.Color
import org.isoron.platform.gui.DataView
import org.isoron.platform.gui.TextAlign
import org.isoron.platform.time.DayOfWeek
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.LocalDateFormatter
import org.isoron.uhabits.core.models.PaletteColor
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

interface OnDateClickedListener {
    fun onDateShortPress(date: LocalDate) {}
    fun onDateLongPress(date: LocalDate) {}
}

class HistoryChart(
    var dateFormatter: LocalDateFormatter,
    var firstWeekday: DayOfWeek,
    var paletteColor: PaletteColor,
    var series: List<Square>,
    var defaultSquare: Square,
    var notesIndicators: List<Boolean>,
    var theme: Theme,
    var today: LocalDate,
    var onDateClickedListener: OnDateClickedListener = object : OnDateClickedListener {},
    var padding: Double = 0.0
) : DataView {

    enum class Square {
        ON,
        OFF,
        GREY,
        DIMMED,
        HATCHED
    }

    var squareSpacing = 1.0
    override var dataOffset = 0

    private var squareSize = 0.0
    private var width = 0.0
    private var height = 0.0
    private var nColumns = 0
    private var topLeftOffset = 0
    private var topLeftDate = LocalDate(2020, 1, 1)
    private var lastPrintedMonth = ""
    private var lastPrintedYear = ""
    private var headerOverflow = 0.0

    override val dataColumnWidth: Double
        get() = squareSpacing + squareSize

    override fun onClick(x: Double, y: Double) {
        onDateClicked(x, y, false)
    }

    override fun onLongClick(x: Double, y: Double) {
        onDateClicked(x, y, true)
    }

    private fun onDateClicked(x: Double, y: Double, isLongClick: Boolean) {
        if (width <= 0.0) throw IllegalStateException("onClick must be called after draw(canvas)")
        val col = ((x - padding) / squareSize).toInt()
        val row = ((y - padding) / squareSize).toInt()
        val offset = col * 7 + (row - 1)
        if (x - padding < 0 || row == 0 || row > 7 || col == nColumns) return
        val clickedDate = topLeftDate.plus(offset)
        if (clickedDate.isNewerThan(today)) return
        if (isLongClick) {
            onDateClickedListener.onDateLongPress(clickedDate)
        } else {
            onDateClickedListener.onDateShortPress(clickedDate)
        }
    }

    override fun draw(canvas: Canvas) {
        width = canvas.getWidth()
        height = canvas.getHeight()

        canvas.setColor(theme.cardBackgroundColor)
        canvas.fill()

        squareSize = round((height - 2 * padding) / 8.0)
        squareSpacing = squareSize * 0.12
        canvas.setFontSize(min(LABEL_FONT_SIZE, height * 0.06))

        val weekdayColumnWidth = DayOfWeek.values().map { weekday ->
            canvas.measureText(dateFormatter.shortWeekdayName(weekday)) + squareSize * 0.15
        }.maxOrNull() ?: 0.0

        nColumns = floor((width - 2 * padding - weekdayColumnWidth) / squareSize).toInt()
        val firstWeekdayOffset = (
            today.dayOfWeek.daysSinceSunday -
                firstWeekday.daysSinceSunday + 7
            ) % 7
        topLeftOffset = (nColumns - 1 + dataOffset) * 7 + firstWeekdayOffset
        topLeftDate = today.minus(topLeftOffset)

        lastPrintedYear = ""
        lastPrintedMonth = ""
        headerOverflow = 0.0

        // Draw main columns
        repeat(nColumns) { column ->
            val topOffset = topLeftOffset - 7 * column
            val topDate = topLeftDate.plus(7 * column)
            drawColumn(canvas, column, topDate, topOffset)
        }

        // Draw week day names
        canvas.setColor(theme.inkMutedColor)
        repeat(7) { row ->
            val date = topLeftDate.plus(row)
            canvas.setTextAlign(TextAlign.LEFT)
            canvas.drawText(
                dateFormatter.shortWeekdayName(date),
                padding + nColumns * squareSize + squareSize * 0.15,
                padding + squareSize * (row + 1) + squareSize / 2
            )
        }
    }

    private fun drawColumn(
        canvas: Canvas,
        column: Int,
        topDate: LocalDate,
        topOffset: Int
    ) {
        drawHeader(canvas, column, topDate)
        repeat(7) { row ->
            val offset = topOffset - row
            val date = topDate.plus(row)
            if (offset < 0) return
            drawSquare(
                canvas,
                padding + column * squareSize,
                padding + (row + 1) * squareSize,
                squareSize - squareSpacing,
                squareSize - squareSpacing,
                date,
                offset
            )
        }
    }

    private fun drawHeader(canvas: Canvas, column: Int, date: LocalDate) {
        canvas.setColor(theme.inkMutedColor)
        val monthText = dateFormatter.shortMonthName(date)
        val yearText = date.year.toString()
        val headerText: String
        when {
            monthText != lastPrintedMonth -> {
                headerText = monthText
                lastPrintedMonth = monthText
            }
            yearText != lastPrintedYear -> {
                headerText = yearText
                lastPrintedYear = headerText
            }
            else -> {
                headerText = ""
            }
        }
        canvas.setTextAlign(TextAlign.LEFT)
        canvas.drawText(
            headerText,
            headerOverflow + padding + column * squareSize,
            padding + squareSize / 2
        )

        headerOverflow += canvas.measureText(headerText) + 0.1 * squareSize
        headerOverflow = max(0.0, headerOverflow - squareSize)
    }

    private fun drawSquare(
        canvas: Canvas,
        x: Double,
        y: Double,
        width: Double,
        height: Double,
        date: LocalDate,
        offset: Int
    ) {
        val value = if (offset >= series.size) defaultSquare else series[offset]
        val hasNotes = if (offset >= notesIndicators.size) false else notesIndicators[offset]
        val fill = theme.habitFill(paletteColor)
        val tint = theme.habitTint(paletteColor)
        val ink = theme.habitInk(paletteColor)
        val radius = width * CORNER_RADIUS
        val strokeWidth = STROKE_WIDTH

        fun roundRectWithBorder(border: Color, inner: Color) {
            canvas.setColor(border)
            canvas.fillRoundRect(x, y, width, height, radius)
            canvas.setColor(inner)
            canvas.fillRoundRect(
                x + strokeWidth,
                y + strokeWidth,
                width - 2 * strokeWidth,
                height - 2 * strokeWidth,
                max(0.0, radius - strokeWidth)
            )
        }

        val squareColor = when (value) {
            Square.ON -> fill
            Square.OFF -> theme.lineColor
            Square.GREY, Square.DIMMED, Square.HATCHED -> tint
        }
        val isToday = date == today
        when {
            isToday -> roundRectWithBorder(theme.inkColor, squareColor)
            value == Square.DIMMED -> roundRectWithBorder(fill, tint)
            else -> {
                canvas.setColor(squareColor)
                canvas.fillRoundRect(x, y, width, height, radius)
            }
        }
        if (isToday && value == Square.DIMMED) {
            canvas.setColor(fill)
            canvas.fillRoundRect(
                x + 2 * strokeWidth,
                y + 2 * strokeWidth,
                width - 4 * strokeWidth,
                height - 4 * strokeWidth,
                max(0.0, radius - 2 * strokeWidth)
            )
            canvas.setColor(tint)
            canvas.fillRoundRect(
                x + 3 * strokeWidth,
                y + 3 * strokeWidth,
                width - 6 * strokeWidth,
                height - 6 * strokeWidth,
                max(0.0, radius - 3 * strokeWidth)
            )
        }

        if (value == Square.HATCHED) {
            canvas.setColor(ink)
            canvas.fillRect(
                x + width * 0.3,
                y + height * 0.78 - strokeWidth / 2,
                width * 0.4,
                strokeWidth
            )
        }

        val textColor = when (value) {
            Square.ON -> theme.onHabitTextColor
            Square.OFF -> theme.inkMutedColor
            else -> theme.inkColor
        }
        canvas.setColor(textColor)
        canvas.setTextAlign(TextAlign.CENTER)
        canvas.drawText(date.day.toString(), x + width / 2, y + width / 2)

        if (hasNotes) {
            canvas.setColor(
                when (value) {
                    Square.ON -> theme.onHabitColor
                    Square.OFF -> theme.inkMutedColor
                    else -> ink
                }
            )
            canvas.fillCircle(x + width - width / 5, y + width / 5, width / 12)
        }
    }

    companion object {
        private const val LABEL_FONT_SIZE = 12.0
        private const val CORNER_RADIUS = 0.22
        private const val STROKE_WIDTH = 1.5
    }
}
