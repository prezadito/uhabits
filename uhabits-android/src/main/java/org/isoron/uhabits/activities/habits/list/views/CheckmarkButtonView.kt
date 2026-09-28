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
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextPaint
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.drawNotesIndicator
import org.isoron.uhabits.utils.getFontAwesome
import org.isoron.uhabits.utils.sp
import org.isoron.uhabits.utils.sres
import org.isoron.uhabits.utils.toMeasureSpec

@Inject
class CheckmarkButtonViewFactory(
    @ActivityContext val context: Context,
    val preferences: Preferences
) {
    fun create() = CheckmarkButtonView(context, preferences)
}

class CheckmarkButtonView(
    context: Context,
    val preferences: Preferences
) : View(context),
    View.OnClickListener,
    View.OnLongClickListener {

    var color: Int = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var value: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    var notes = ""
        set(value) {
            field = value
            invalidate()
        }

    /**
     * For quit habits, the button toggles between clean days and slips.
     */
    var isQuit = false
        set(value) {
            field = value
            invalidate()
        }

    var onToggle: (Int, String) -> Unit = { _, _ -> }

    var onEdit: () -> Unit = { }

    private var drawer = Drawer()

    init {
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    fun performToggle() {
        // For quit habits, the value is not toggled here: tapping a day either records a slip
        // or shows the slips of that day, and the button is refreshed once that is done.
        if (!isQuit) {
            value = Entry.nextToggleValue(
                value = value,
                isSkipEnabled = preferences.isSkipEnabled,
                areQuestionMarksEnabled = preferences.areQuestionMarksEnabled
            )
        }
        onToggle(value, notes)
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        invalidate()
    }

    override fun onClick(v: View) {
        // Tapping a day of a quit habit always adds or lists slips, regardless of the short
        // toggle preference, while long presses edit notes.
        if (isQuit || preferences.isShortToggleEnabled) {
            performToggle()
        } else {
            onEdit()
        }
    }

    override fun onLongClick(v: View): Boolean {
        if (isQuit || preferences.isShortToggleEnabled) {
            onEdit()
        } else {
            performToggle()
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = resources.getDimensionPixelSize(R.dimen.checkmarkHeight)
        val width = resources.getDimensionPixelSize(R.dimen.checkmarkWidth)
        super.onMeasure(
            width.toMeasureSpec(EXACTLY),
            height.toMeasureSpec(EXACTLY)
        )
    }

    private inner class Drawer {
        /**
         * Quit habits show clean days as checkmarks and slips as crosses. Days before the
         * quit date are left blank.
         */
        private fun drawQuit(canvas: Canvas) {
            paint.strokeWidth = 0f
            paint.style = Paint.Style.FILL
            paint.textSize = sp(14.0f)
            val em = paint.measureText("m")
            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            val id = when (value) {
                YES_MANUAL -> R.string.fa_check
                NO -> R.string.fa_times
                else -> null
            }
            if (id != null) {
                paint.color = if (value == NO) mediumContrastColor else color
                val textRect = RectF(rect).apply { offset(0f, 0.4f * em) }
                canvas.drawText(resources.getString(id), textRect.centerX(), textRect.centerY(), paint)
            }
            drawNotesIndicator(
                pNotesIndicator = pNotesIndicator,
                canvas = canvas,
                color = color,
                size = em,
                notes = notes
            )
        }

        private val rect = RectF()
        private val bgColor = sres.getColor(R.attr.cardBgColor)
        private val lowContrastColor = sres.getColor(R.attr.contrast40)
        private val mediumContrastColor = sres.getColor(R.attr.contrast60)
        private val pNotesIndicator = Paint()

        private val paint = TextPaint().apply {
            typeface = getFontAwesome()
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }

        fun draw(canvas: Canvas) {
            if (isQuit) {
                drawQuit(canvas)
                return
            }
            paint.color = when (value) {
                YES_MANUAL, YES_AUTO, SKIP -> color
                NO -> {
                    if (preferences.areQuestionMarksEnabled) {
                        mediumContrastColor
                    } else {
                        lowContrastColor
                    }
                }
                else -> lowContrastColor
            }
            val id = when (value) {
                SKIP -> R.string.fa_skipped
                NO -> R.string.fa_times
                UNKNOWN -> {
                    if (preferences.areQuestionMarksEnabled) {
                        R.string.fa_question
                    } else {
                        R.string.fa_times
                    }
                }
                else -> R.string.fa_check
            }
            paint.textSize = when {
                id == R.string.fa_question -> sp(12.0f)
                value == YES_AUTO -> sp(13.0f)
                else -> sp(14.0f)
            }
            if (value == YES_AUTO) {
                paint.strokeWidth = 5f
                paint.style = Paint.Style.STROKE
            } else {
                paint.strokeWidth = 0f
                paint.style = Paint.Style.FILL
            }

            val label = resources.getString(id)
            val em = paint.measureText("m")

            rect.set(0f, 0f, width.toFloat(), height.toFloat())
            rect.offset(0f, 0.4f * em)
            canvas.drawText(label, rect.centerX(), rect.centerY(), paint)

            if (value == YES_AUTO) {
                paint.color = bgColor
                paint.style = Paint.Style.FILL
                canvas.drawText(label, rect.centerX(), rect.centerY(), paint)
            }

            drawNotesIndicator(
                pNotesIndicator = pNotesIndicator,
                canvas = canvas,
                color = color,
                size = em,
                notes = notes
            )
        }
    }
}
