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
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.NumericalHabitType.AT_LEAST
import org.isoron.uhabits.core.models.NumericalHabitType.AT_MOST
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import java.text.DecimalFormat
import kotlin.math.max

fun Double.toShortString(): String = when {
    this >= 1e9 -> String.format("%.1fG", this / 1e9)
    this >= 1e8 -> String.format("%.0fM", this / 1e6)
    this >= 1e7 -> String.format("%.1fM", this / 1e6)
    this >= 1e6 -> String.format("%.1fM", this / 1e6)
    this >= 1e5 -> String.format("%.0fk", this / 1e3)
    this >= 1e4 -> String.format("%.1fk", this / 1e3)
    this >= 1e3 -> String.format("%.1fk", this / 1e3)
    this >= 1e2 -> DecimalFormat("#").format(this)
    this >= 1e1 -> DecimalFormat("#.#").format(this)
    else -> DecimalFormat("#.##").format(this)
}

@Inject
class NumberButtonViewFactory(
    @ActivityContext val context: Context,
    val preferences: Preferences
) {
    fun create() = NumberButtonView(context, preferences)
}

class NumberButtonView(
    @ActivityContext context: Context,
    val preferences: Preferences
) : View(context),
    OnClickListener,
    OnLongClickListener {

    var colors = TileColors(0, 0, 0)
        set(value) {
            field = value
            invalidate()
        }

    var date: LocalDate? = null
        set(value) {
            field = value
            updateDescription()
        }

    var value = 0.0
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var threshold = 0.0
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var targetType = AT_LEAST
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var units = ""
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }
    var notes = ""
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var onEdit: () -> Unit = { }

    private var drawer: Drawer = Drawer(context)

    init {
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    private fun isTargetMet() = value >= 0 && when (targetType) {
        AT_LEAST -> value >= threshold
        AT_MOST -> value <= threshold
    }

    private fun updateDescription() {
        val date = date ?: return
        val day = describeDay(date)
        val description = when {
            value == Entry.SKIP.toDouble() / 1000 ->
                resources.getString(R.string.cd_entry_skipped, day)
            value < 0 -> resources.getString(R.string.cd_entry_unknown, day)
            else -> resources.getString(
                if (isTargetMet()) R.string.cd_number_met else R.string.cd_number_missed,
                day,
                if (units.isBlank()) value.toShortString() else "${value.toShortString()} $units"
            )
        }
        contentDescription = describeWithNotes(description, notes)
    }

    override fun onClick(v: View) {
        onEdit()
    }

    override fun onLongClick(v: View): Boolean {
        onEdit()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDimension(context, R.dimen.checkmarkWidth).toInt()
        val height = getDimension(context, R.dimen.checkmarkHeight).toInt()
        setMeasuredDimension(width, height)
    }

    private inner class Drawer(context: Context) {
        private val tile = DayTilePainter(this@NumberButtonView)
        private val pNotesIndicator = Paint()

        private val pUnit: TextPaint = TextPaint().apply {
            textSize = dim(R.dimen.ds_text_tile_unit)
            typeface = Typeface.DEFAULT
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        private val pNumber: TextPaint = TextPaint().apply {
            textSize = dim(R.dimen.ds_text_tile_number)
            typeface = SANS_BOLD
            fontFeatureSettings = "tnum"
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        fun draw(canvas: Canvas) {
            tile.layout()
            val skipped = value == Entry.SKIP.toDouble() / 1000
            val recorded = value >= 0 && !skipped
            val met = recorded && isTargetMet()
            val textColor: Int
            when {
                skipped -> {
                    tile.hatch(canvas, colors.tint)
                    tile.glyph(canvas, R.string.fa_skipped, 15f, colors.ink)
                    textColor = colors.ink
                }
                met -> {
                    tile.fill(canvas, colors.fill)
                    textColor = tile.onHabitText
                }
                else -> {
                    tile.fill(canvas, colors.tint)
                    textColor = if (recorded) tile.ink else tile.inkMuted
                }
            }
            if (!skipped) {
                if (!recorded && preferences.areQuestionMarksEnabled) {
                    tile.glyph(canvas, R.string.fa_question, 13f, textColor)
                } else {
                    drawNumber(canvas, if (recorded) value.toShortString() else "0", textColor)
                }
            }
            tile.notesDot(canvas, textColor, notes)
        }

        /** Shrinks the number to fit inside the tile, down to a 9sp minimum. */
        private fun fitNumber(label: String) {
            val maxWidth = dim(R.dimen.ds_tile) - dp(4f)
            val minSize = dim(R.dimen.ds_text_tile_unit)
            pNumber.textSize = dim(R.dimen.ds_text_tile_number)
            while (pNumber.textSize > minSize && pNumber.measureText(label) > maxWidth) {
                pNumber.textSize = max(minSize, pNumber.textSize - 1f)
            }
        }

        private fun drawNumber(canvas: Canvas, label: String, color: Int) {
            pNumber.color = color
            pUnit.color = color
            fitNumber(label)
            if (units.isBlank()) {
                tile.text(canvas, label, pNumber, tile.rect.centerY())
                return
            }
            tile.text(canvas, label, pNumber, tile.rect.centerY() - dp(6f))
            val maxUnitsWidth = dp(32f)
            var trimmedUnits = units
            while (trimmedUnits.length > 2 && pUnit.measureText(trimmedUnits) > maxUnitsWidth) {
                trimmedUnits = trimmedUnits.dropLast(2) + "…"
            }
            tile.text(canvas, trimmedUnits, pUnit, tile.rect.centerY() + dp(7f))
        }
    }
}
