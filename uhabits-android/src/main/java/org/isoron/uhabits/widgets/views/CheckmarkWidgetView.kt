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
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.HabitsApplication
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.RingView
import org.isoron.uhabits.activities.habits.list.views.toShortString
import org.isoron.uhabits.core.models.Entry.Companion.YES_MANUAL
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.views.WidgetTheme
import org.isoron.uhabits.inject.HabitsApplicationComponent
import org.isoron.uhabits.utils.InterfaceUtils.getDimension
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CheckmarkWidgetView : HabitWidgetView {
    var habitPaletteColor = PaletteColor(11)

    var percentage = 0f
    var name: String? = null
    private lateinit var ring: RingView
    private lateinit var label: TextView
    private lateinit var tile: WidgetTileView
    var entryValue = 0
    var entryState = 0
    var isNumerical = false

    /**
     * For quit habits, the text shown inside the ring (the number of clean days).
     */
    var quitText: String? = null
    private var preferences: Preferences? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun refresh() {
        if (backgroundPaint == null || frame == null) return
        val theme = WidgetTheme()
        val habitColor = habitPaletteColor
        tile.fill = theme.habitFill(habitColor).toInt()
        tile.tint = theme.habitTint(habitColor).toInt()
        tile.state = entryState
        tile.questionMarks = preferences!!.areQuestionMarksEnabled
        tile.text = if (isNumerical || quitText != null) numberText else null
        tile.invalidate()
        ring.setPercentage(percentage)
        ring.setColor(tile.fill)
        ring.setText("")
        label.text = name
        label.setTextColor(Color.WHITE)
        requestLayout()
        postInvalidate()
    }

    private val numberText: String
        get() = quitText ?: (max(0, entryValue) / 1000.0).toShortString()

    override val innerLayoutId: Int
        get() = R.layout.widget_checkmark

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (height >= width) {
            height = min(height, (width * 1.5).roundToInt())
        } else {
            width = min(width, height)
        }
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimension(context, R.dimen.ds_text_caption))
        ring.setThickness(getDimension(context, R.dimen.ds_ring_stroke))
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    private fun init() {
        val appComponent: HabitsApplicationComponent =
            (context.applicationContext as HabitsApplication).component
        preferences = appComponent.preferences
        ring = findViewById<View>(R.id.scoreRing) as RingView
        label = findViewById<View>(R.id.label) as TextView
        ring.setIsTransparencyEnabled(true)
        tile = WidgetTileView(context)
        (findViewById<View>(R.id.tileContainer) as ViewGroup).addView(tile)
        if (isInEditMode) {
            percentage = 0.75f
            name = "Wake up early"
            habitPaletteColor = PaletteColor(6)
            entryValue = YES_MANUAL
            refresh()
        }
    }
}
