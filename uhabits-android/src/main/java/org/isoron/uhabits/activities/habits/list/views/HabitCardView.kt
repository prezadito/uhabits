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
import android.graphics.text.LineBreaker.BREAK_STRATEGY_BALANCED
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
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
import org.isoron.uhabits.core.models.getCurrentWeeklyStreak
import org.isoron.uhabits.core.models.isWeeklyTarget
import org.isoron.uhabits.core.preferences.HomeViewMode
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.screens.habits.list.ListHabitsBehavior
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.utils.HabitIcons
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
    private val behavior: ListHabitsBehavior,
    private val prefs: Preferences
) {
    fun create() = HabitCardView(context, checkmarkPanelFactory, numberPanelFactory, behavior, prefs)
}

class HabitCardView(
    @ActivityContext context: Context,
    checkmarkPanelFactory: CheckmarkPanelViewFactory,
    numberPanelFactory: NumberPanelViewFactory,
    private val behavior: ListHabitsBehavior,
    private val prefs: Preferences
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
            updateNotesButton()
        }

    var viewMode = HomeViewMode.WEEKLY
        set(value) {
            if (field == value) return
            field = value
            applyViewMode()
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
            history.invalidate()
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
            updateNotesButton()
        }

    var checkmarkPanel: CheckmarkPanelView
    private var numberPanel: NumberPanelView
    private var innerFrame: LinearLayout
    private var label: TextView
    private var metaLabel: TextView
    private var labelContainer: LinearLayout
    private var scoreRing: RingView
    private var topRow: LinearLayout
    private var tileRow: FrameLayout
    private var notesButton: NotesButtonView
    private var history: HabitHistoryView

    private var currentToggleTaskId = 0

    private val cleanTimeTicker = object : Runnable {
        override fun run() {
            habit?.let { if (it.isQuit) updateMeta(it) }
            postDelayed(this, CLEAN_TIME_REFRESH_INTERVAL)
        }
    }

    init {
        scoreRing = RingView(context).apply {
            val ringSize = dim(R.dimen.ds_ring_icon).toInt()
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

        notesButton = NotesButtonView(context).apply {
            onEdit = {
                habit?.let { behavior.onEditNotes(it, getToday().minus(dataOffset)) }
            }
        }

        topRow = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            addView(scoreRing)
            addView(labelContainer)
            addView(notesButton)
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

        history = HabitHistoryView(context, prefs).apply {
            visibility = GONE
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = dp(10f).toInt()
            }
        }

        tileRow = FrameLayout(context).apply {
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
            addView(history)

            setOnTouchListener { v, event ->
                v.background.setHotspot(event.x, event.y)
                false
            }
        }

        applyViewMode()
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

    /** The notes button always acts on the newest day shown in the panels. */
    private fun updateNotesButton() {
        notesButton.date = getToday().minus(dataOffset)
        notesButton.notes = notes.getOrNull(dataOffset) ?: ""
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
            setTextColor(tileColors.fill)
        }
        updateRingGlyph(h)
        history.habit = h
        history.colors = tileColors
        notesButton.colors = tileColors
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

    /** The habit's icon inside the ring, or the first letter of its name when it has none. */
    private fun updateRingGlyph(h: Habit) {
        val glyph = HabitIcons.glyph(context, h.icon)
        scoreRing.apply {
            if (glyph != null) {
                setTypeface(HabitIcons.typeface(context, h.icon))
                setBoldText(false)
                setText(glyph)
                setTextSize(dim(R.dimen.ds_ring_icon) * ICON_TEXT_RATIO)
            } else {
                setTypeface(null)
                setBoldText(true)
                setText(h.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "")
                setTextSize(dim(R.dimen.ds_ring_icon) * LETTER_TEXT_RATIO)
            }
        }
    }

    /**
     * Weekly: day tiles under the title. The other views: a single tile (the day selected in the
     * day row) at the end of the title row, plus, in monthly and grid, a read-only history under
     * the title.
     */
    private fun applyViewMode() {
        val showsHistory = viewMode == HomeViewMode.MONTHLY || viewMode == HomeViewMode.GRID
        (tileRow.parent as? ViewGroup)?.removeView(tileRow)
        if (viewMode.usesDayRow) {
            tileRow.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            topRow.addView(tileRow)
        } else {
            tileRow.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = dp(8f).toInt()
            }
            innerFrame.addView(tileRow, innerFrame.indexOfChild(history))
        }
        history.visibility = if (showsHistory) VISIBLE else GONE
        history.mode = viewMode
    }

    /** Streak for regular habits, clean time for quit habits. */
    private fun updateMeta(h: Habit) {
        metaLabel.text = when {
            h.isQuit -> h.formatCleanStatus(resources, includeSlipToday = false)
            else -> formatStreak(h)
        }
    }

    /** "Streak: 7 days", or "Streak: 4 weeks" (weeks meeting the target) for "N times per week". */
    private fun formatStreak(h: Habit): String {
        if (h.isWeeklyTarget) {
            val weeks = h.getCurrentWeeklyStreak(getToday(), prefs.firstWeekdayInt)
            return resources.getQuantityString(R.plurals.habit_card_streak_weeks, weeks, weeks)
        }
        val days = h.streaks.getCurrent(getToday())?.length ?: 0
        return resources.getQuantityString(R.plurals.habit_card_streak_days, days, days)
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
        private const val ICON_TEXT_RATIO = 0.5f
        private const val LETTER_TEXT_RATIO = 0.42f

        fun (() -> Unit).delay(delayInMillis: Long) {
            Handler(Looper.getMainLooper()).postDelayed(this, delayInMillis)
        }
    }
}
