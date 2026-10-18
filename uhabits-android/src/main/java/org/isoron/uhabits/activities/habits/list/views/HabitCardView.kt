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
import android.graphics.PointF
import android.graphics.Typeface
import android.graphics.text.LineBreaker.BREAK_STRATEGY_BALANCED
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import me.tatarka.inject.annotations.Inject
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.RingView
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.models.PaletteColor
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.currentTheme
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.formatCleanStatus
import org.isoron.uhabits.utils.sres
import kotlin.math.roundToInt

@Inject
class HabitCardViewFactory(
    @ActivityContext val context: Context,
    private val checkmarkPanelFactory: CheckmarkPanelViewFactory,
    private val numberPanelFactory: NumberPanelViewFactory,
    private val behavior: ListHabitsBehavior
) {
    fun create() = HabitCardView(context, checkmarkPanelFactory, numberPanelFactory, behavior)
}

class HabitCardView(
    @ActivityContext context: Context,
    checkmarkPanelFactory: CheckmarkPanelViewFactory,
    numberPanelFactory: NumberPanelViewFactory,
    private val behavior: ListHabitsBehavior
) : FrameLayout(context),
    ModelObservable.Listener {

    var buttonCount
        get() = checkmarkPanel.buttonCount
        set(value) {
            checkmarkPanel.buttonCount = value
            numberPanel.buttonCount = value
        }

    var dataOffset = 0
        set(value) {
            field = value
            checkmarkPanel.dataOffset = value
            numberPanel.dataOffset = value
        }

    var habit: Habit? = null
        set(newHabit) {
            if (isAttachedToWindow) {
                field?.observable?.removeListener(this)
                newHabit?.observable?.addListener(this)
            }
            field = newHabit
            if (newHabit != null) copyAttributesFrom(newHabit)
        }

    var score
        get() = scoreRing.getPercentage().toDouble()
        set(value) {
            scoreRing.setPercentage(value.toFloat())
            scoreRing.setPrecision(1.0f / 16)
            scoreRing.contentDescription =
                resources.getString(R.string.cd_habit_strength, scorePercent())
            habit?.let { updateMeta(it) }
        }

    var unit
        get() = numberPanel.units
        set(value) {
            numberPanel.units = value
        }

    var values
        get() = checkmarkPanel.values
        set(values) {
            checkmarkPanel.values = values
            numberPanel.values = values.map { it / 1000.0 }.toDoubleArray()
        }

    var threshold: Double
        get() = numberPanel.threshold
        set(value) {
            numberPanel.threshold = value
        }

    var notes
        get() = checkmarkPanel.notes
        set(values) {
            checkmarkPanel.notes = values
            numberPanel.notes = values
        }

    var checkmarkPanel: CheckmarkPanelView
    private var numberPanel: NumberPanelView
    private var innerFrame: LinearLayout
    private var label: TextView
    private var metaLabel: TextView
    private var labelContainer: LinearLayout
    private var scoreRing: RingView

    private var currentToggleTaskId = 0
    private var metaInk = 0

    private val cleanTimeTicker = object : Runnable {
        override fun run() {
            habit?.let { if (it.isQuit) updateMeta(it) }
            postDelayed(this, CLEAN_TIME_REFRESH_INTERVAL)
        }
    }

    init {
        scoreRing = RingView(context).apply {
            val ringSize = dim(R.dimen.ds_ring).toInt()
            layoutParams = LinearLayout.LayoutParams(ringSize, ringSize).apply {
                marginEnd = dp(12f).toInt()
                gravity = Gravity.CENTER
            }
            setThickness(dim(R.dimen.ds_ring_stroke))
            setBackgroundColor(sres.getColor(R.attr.dsSurfaceRaised))
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        label = TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_Ds_HabitName)
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                breakStrategy = BREAK_STRATEGY_BALANCED
            }
        }

        metaLabel = TextView(context).apply {
            setTextAppearance(R.style.TextAppearance_Ds_Caption)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }

        labelContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            addView(label)
            addView(metaLabel)
        }

        val topRow = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            addView(scoreRing)
            addView(labelContainer)
        }

        checkmarkPanel = checkmarkPanelFactory.create().apply {
            onToggle = { date, value, notes ->
                triggerRipple(date)
                val location = getAbsoluteButtonLocation(date)
                habit?.let {
                    behavior.onToggle(
                        it,
                        date,
                        value,
                        notes,
                        location.x,
                        location.y
                    )
                }
            }
            onEdit = { date ->
                triggerRipple(date)
                val location = getAbsoluteButtonLocation(date)
                habit?.let { behavior.onEdit(it, date, location.x, location.y) }
            }
        }

        numberPanel = numberPanelFactory.create().apply {
            visibility = GONE
            onEdit = { date ->
                triggerRipple(date)
                val location = getAbsoluteButtonLocation(date)
                habit?.let { behavior.onEdit(it, date, location.x, location.y) }
            }
        }

        val tileRow = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = dp(8f).toInt()
            }
            val end = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.END)
            addView(checkmarkPanel, end)
            addView(numberPanel, FrameLayout.LayoutParams(end))
        }

        innerFrame = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            val padding = dp(12f).toInt()
            setPadding(padding, padding, padding, padding)
            elevation = sres.getDimension(R.attr.dsCardElevation).toFloat()
            outlineProvider = ViewOutlineProvider.BACKGROUND

            addView(topRow)
            addView(tileRow)

            setOnTouchListener { v, event ->
                v.background.setHotspot(event.x, event.y)
                false
            }
        }

        clipToPadding = false
        layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        setPadding(dp(16f).toInt(), 0, dp(16f).toInt(), dp(8f).toInt())
        addView(innerFrame)
    }

    override fun onModelChange() {
        Handler(Looper.getMainLooper()).post {
            habit?.let { copyAttributesFrom(it) }
        }
    }

    override fun setSelected(isSelected: Boolean) {
        super.setSelected(isSelected)
        updateBackground(isSelected)
    }

    fun triggerRipple(date: LocalDate) {
        val location = getRelativeButtonLocation(date)
        triggerRipple(location.x, location.y)
    }

    private fun getRelativeButtonLocation(date: LocalDate): PointF {
        val today = getToday()
        val offset = date.daysUntil(today) - dataOffset
        val panel = when (habit!!.isNumerical) {
            true -> numberPanel
            false -> checkmarkPanel
        }
        val button = panel.buttons[offset]
        val y = button.height / 2.0f
        val x = panel.x + button.x + (button.width / 2).toFloat()
        return PointF(x, y)
    }

    private fun getAbsoluteButtonLocation(date: LocalDate): PointF {
        val containerLocation = IntArray(2)
        this.getLocationInWindow(containerLocation)
        val relButtonLocation = getRelativeButtonLocation(date)
        val windowInsets = rootWindowInsets
        val xInset = windowInsets?.displayCutout?.safeInsetLeft ?: 0
        val yInset = if (SDK_INT <= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            windowInsets?.systemWindowInsetTop ?: 0
        } else {
            0
        }
        return PointF(
            containerLocation[0].toFloat() + relButtonLocation.x - xInset,
            containerLocation[1].toFloat() + relButtonLocation.y - yInset
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        habit?.observable?.addListener(this)
        postDelayed(cleanTimeTicker, CLEAN_TIME_REFRESH_INTERVAL)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(cleanTimeTicker)
        habit?.observable?.removeListener(this)
        super.onDetachedFromWindow()
    }

    private fun scorePercent() = (score * 100).roundToInt()

    private fun copyAttributesFrom(h: Habit) {
        val theme = currentTheme()
        val color = if (h.isArchived) PaletteColor.fromHue(GRAPHITE_HUE) else h.color
        val tileColors = TileColors.of(theme, color)
        label.apply {
            text = h.name
            setTextColor(tileColors.ink)
        }
        scoreRing.apply {
            setColor(tileColors.fill)
            setInactiveColor(tileColors.tint)
        }
        metaInk = tileColors.ink
        updateMeta(h)
        checkmarkPanel.apply {
            colors = tileColors
            isQuit = h.isQuit
            visibility = when (h.isNumerical) {
                true -> View.GONE
                false -> View.VISIBLE
            }
        }
        numberPanel.apply {
            colors = tileColors
            units = h.unit
            targetType = h.targetType
            threshold = h.targetValue
            visibility = when (h.isNumerical) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
    }

    /** "72% · Every day", with the percentage bold in the habit's ink color. */
    private fun updateMeta(h: Habit) {
        val percent = "${scorePercent()}%"
        val detail = when {
            h.isQuit -> h.formatCleanStatus(resources)
            else -> formatStreak(h)
        }
        val text = SpannableStringBuilder(percent).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(metaInk), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            append(" · ").append(detail)
        }
        metaLabel.text = text
    }

    /** "Streak: 7 days", or in weeks ("Streak: 4 weeks") for habits with a weekly or longer period. */
    private fun formatStreak(h: Habit): String {
        val days = h.streaks.getCurrent(getToday())?.length ?: 0
        return if (h.frequency.denominator >= 7 && days >= 7) {
            val weeks = days / 7
            resources.getQuantityString(R.plurals.habit_card_streak_weeks, weeks, weeks)
        } else {
            resources.getQuantityString(R.plurals.habit_card_streak_days, days, days)
        }
    }

    private fun triggerRipple(x: Float, y: Float) {
        val background = innerFrame.background
        background.setHotspot(x, y)
        background.state = intArrayOf(
            android.R.attr.state_pressed,
            android.R.attr.state_enabled
        )
        Handler().postDelayed({ background.state = intArrayOf() }, 25)
    }

    private fun updateBackground(isSelected: Boolean) {
        val background = when (isSelected) {
            true -> R.drawable.ds_card_selected
            false -> R.drawable.ds_card_ripple
        }
        innerFrame.setBackgroundResource(background)
    }

    companion object {
        private const val CLEAN_TIME_REFRESH_INTERVAL = 60_000L
        private const val GRAPHITE_HUE = 11

        fun (() -> Unit).delay(delayInMillis: Long) {
            Handler(Looper.getMainLooper()).postDelayed(this, delayInMillis)
        }
    }
}
