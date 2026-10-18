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
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.View.MeasureSpec.EXACTLY
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.models.Entry.Companion.NO
import org.isoron.uhabits.core.models.Entry.Companion.SKIP
import org.isoron.uhabits.core.models.Entry.Companion.UNKNOWN
import org.isoron.uhabits.core.models.Entry.Companion.YES_AUTO
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.inject.ActivityContext
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

    var value: Int = 0
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

    /**
     * For quit habits, the button toggles between clean days and slips.
     */
    var isQuit = false
        set(value) {
            field = value
            updateDescription()
            invalidate()
        }

    var onToggle: (Int, String) -> Unit = { _, _ -> }

    var onEdit: () -> Unit = { }

    private var drawer = Drawer()

    init {
        setOnClickListener(this)
        setOnLongClickListener(this)
    }

    private fun updateDescription() {
        val date = date ?: return
        val day = describeDay(date)
        val id = when {
            isQuit && value == YES_MANUAL -> R.string.cd_entry_clean
            isQuit && value == NO -> R.string.cd_entry_slip
            isQuit -> R.string.cd_entry_unknown
            value == YES_MANUAL -> R.string.cd_entry_done
            value == YES_AUTO -> R.string.cd_entry_done_auto
            value == SKIP -> R.string.cd_entry_skipped
            value == NO -> R.string.cd_entry_not_done
            preferences.areQuestionMarksEnabled -> R.string.cd_entry_unknown
            else -> R.string.cd_entry_not_done
        }
        contentDescription = describeWithNotes(resources.getString(id, day), notes)
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
        private val tile = DayTilePainter(this@CheckmarkButtonView)
        private val pNotesIndicator = Paint()

        fun draw(canvas: Canvas) {
            tile.layout()
            val state = resolveState() ?: return drawBlank(canvas)
            when (state) {
                State.DONE -> {
                    tile.fill(canvas, colors.fill)
                    tile.glyph(canvas, R.string.fa_check, 15f, tile.onHabit)
                }
                State.AUTO -> {
                    tile.fill(canvas, colors.tint)
                    tile.innerStroke(canvas, colors.fill, 2f)
                    tile.glyph(canvas, R.string.fa_check, 15f, colors.ink)
                }
                State.SKIPPED -> {
                    tile.hatch(canvas, colors.tint)
                    tile.glyph(canvas, R.string.fa_skipped, 15f, colors.ink)
                }
                State.MISSED -> {
                    tile.fill(canvas, colors.tint)
                    tile.glyph(canvas, R.string.fa_times, 12f, tile.inkMuted)
                }
                State.UNKNOWN -> {
                    tile.dashedBorder(canvas, tile.lineStrong)
                    tile.glyph(canvas, R.string.fa_question, 13f, tile.inkMuted)
                }
            }
            tile.notesDot(canvas, glyphColor(state), notes)
        }

        private fun drawBlank(canvas: Canvas) {
            tile.notesDot(canvas, colors.ink, notes)
        }

        private fun glyphColor(state: State) = when (state) {
            State.DONE -> tile.onHabit
            State.AUTO, State.SKIPPED -> colors.ink
            else -> tile.inkMuted
        }

        /** Null when nothing is drawn (quit habits, days before the quit date). */
        private fun resolveState(): State? {
            if (isQuit) {
                return when (value) {
                    YES_MANUAL -> State.DONE
                    NO -> State.MISSED
                    else -> null
                }
            }
            return when (value) {
                YES_MANUAL -> State.DONE
                YES_AUTO -> State.AUTO
                SKIP -> State.SKIPPED
                UNKNOWN ->
                    if (preferences.areQuestionMarksEnabled) State.UNKNOWN else State.MISSED
                else -> State.MISSED
            }
        }
    }

    private enum class State { DONE, AUTO, SKIPPED, MISSED, UNKNOWN }
}
