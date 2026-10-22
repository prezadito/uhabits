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

package org.isoron.uhabits.activities.habits.list

import android.content.Context
import android.text.format.DateFormat
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import me.tatarka.inject.annotations.Inject
import nl.dionsegijn.konfetti.xml.KonfettiView
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.common.views.ScrollableChart
import org.isoron.uhabits.activities.common.views.TaskProgressBar
import org.isoron.uhabits.activities.habits.list.views.DaySelectorView
import org.isoron.uhabits.activities.habits.list.views.EmptyListView
import org.isoron.uhabits.activities.habits.list.views.HabitCardListAdapter
import org.isoron.uhabits.activities.habits.list.views.HabitCardListView
import org.isoron.uhabits.activities.habits.list.views.HabitCardListViewFactory
import org.isoron.uhabits.activities.habits.list.views.HeaderView
import org.isoron.uhabits.activities.habits.list.views.HintView
import org.isoron.uhabits.core.models.ModelObservable
import org.isoron.uhabits.core.preferences.HomeViewMode
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.tasks.TaskRunner
import org.isoron.uhabits.core.ui.screens.habits.list.HintListFactory
import org.isoron.uhabits.core.ui.screens.habits.list.WeekPaging
import org.isoron.uhabits.core.utils.MidnightTimer
import org.isoron.uhabits.inject.ActivityContext
import org.isoron.uhabits.inject.ActivityScope
import org.isoron.uhabits.utils.addAtBottom
import org.isoron.uhabits.utils.addAtTop
import org.isoron.uhabits.utils.addBelow
import org.isoron.uhabits.utils.buildToolbar
import org.isoron.uhabits.utils.dim
import org.isoron.uhabits.utils.dp
import org.isoron.uhabits.utils.setScrolled
import org.isoron.uhabits.utils.setupToolbar
import org.isoron.uhabits.utils.sres
import java.time.LocalDate
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

const val MAX_CHECKMARK_COUNT = 60

@Inject
@ActivityScope
class ListHabitsRootView(
    @ActivityContext context: Context,
    hintListFactory: HintListFactory,
    private val preferences: Preferences,
    private val midnightTimer: MidnightTimer,
    runner: TaskRunner,
    private val listAdapter: HabitCardListAdapter,
    private val menu: ListHabitsMenu,
    habitCardListViewFactory: HabitCardListViewFactory
) : FrameLayout(context),
    ModelObservable.Listener,
    Preferences.Listener,
    MidnightTimer.MidnightListener {

    val listView: HabitCardListView = habitCardListViewFactory.create()
    val llEmpty = EmptyListView(context)
    val tbar = buildToolbar()
    val konfettiView = KonfettiView(context).apply {
        translationZ = 10f
    }
    val progressBar = TaskProgressBar(context, runner)
    val hintView: HintView
    val header = HeaderView(context, preferences, midnightTimer)
    val daySelector = DaySelectorView(context)
    private val headerContainer = FrameLayout(context).apply {
        addView(header, MATCH_PARENT, WRAP_CONTENT)
        addView(daySelector, MATCH_PARENT, WRAP_CONTENT)
    }

    init {
        val hints = resources.getStringArray(R.array.hints)
        val hintList = hintListFactory.create(hints)
        hintView = HintView(context, hintList)

        val rootView = RelativeLayout(context).apply {
            background = sres.getDrawable(R.attr.windowBackgroundColor)
            addAtTop(konfettiView)
            addAtTop(tbar)
            addBelow(headerContainer, tbar)
            addBelow(listView, headerContainer, height = MATCH_PARENT)
            addBelow(llEmpty, headerContainer, height = MATCH_PARENT)
            addBelow(progressBar, headerContainer) {
                it.topMargin = dp(-6.0f).toInt()
            }
            addAtBottom(hintView)
            (hintView.layoutParams as MarginLayoutParams).setMargins(
                dp(16f).toInt(),
                0,
                dp(16f).toInt(),
                dp(8f).toInt()
            )
        }
        rootView.setupToolbar(
            toolbar = tbar,
            title = dateTitle(0),
            displayHomeAsUpEnabled = false
        )
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                tbar.setScrolled(recyclerView.canScrollVertically(-1))
                moveToolbar(dy)
            }
        })
        listView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> moveToolbar(0) }
        daySelector.onSelect = { offset ->
            listView.selectedDayOffset = offset
            updateTitle()
        }
        addView(rootView, MATCH_PARENT, MATCH_PARENT)
        listAdapter.setListView(listView)
        applyViewMode()
    }

    /**
     * Slides the toolbar (the date title and the menu) out of view while scrolling down and back in
     * while scrolling up, fading it as it goes. The day row is anchored below it, so it stays pinned to the top and the list takes over
     * the freed space. The toolbar is left alone when the
     * list is too short to scroll once the toolbar is gone, since it could not be brought back, and
     * while searching, since the search field lives in the toolbar.
     */
    private fun moveToolbar(dy: Int) {
        val params = tbar.layoutParams as? MarginLayoutParams ?: return
        val hideable = tbar.height - tbar.paddingTop
        val hidden = -params.topMargin
        val canHide = hideable > 0 &&
            !menu.isSearchActive &&
            listView.computeVerticalScrollRange() > listView.height - hidden + hideable
        val wanted = when {
            !canHide || !listView.canScrollVertically(-1) -> 0
            else -> (hidden + dy).coerceIn(0, hideable)
        }
        if (wanted != hidden) {
            params.topMargin = -wanted
            tbar.layoutParams = params
        }
        // The status bar area is drawn over the toolbar's last pixels, so fade it out too.
        tbar.alpha = if (hideable > 0) 1f - wanted.toFloat() / hideable else 1f
    }

    /** Switching views always goes back to today: the day row, the selection and Weekly's header. */
    override fun onHomeViewModeChanged() {
        post {
            selectDay(0)
            daySelector.refresh()
            header.reset()
            applyViewMode()
        }
    }

    override fun atMidnight() {
        post {
            selectDay(0)
            daySelector.refresh()
        }
    }

    /**
     * Weekly shows the scrollable day header; the other views show a day row to pick the day the
     * cards act on, and go back to today when Weekly is shown.
     */
    private fun applyViewMode() {
        val mode = preferences.homeViewMode
        header.visibility = if (mode == HomeViewMode.WEEKLY) VISIBLE else GONE
        daySelector.visibility = if (mode.usesDayRow) VISIBLE else GONE
        if (!mode.usesDayRow) selectDay(0)
        listView.viewMode = mode
        updateTitle()
    }

    private fun selectDay(offset: Int) {
        daySelector.selectedOffset = offset
        listView.selectedDayOffset = offset
        updateTitle()
    }

    /** Today's date, or in the views with a day row the selected day's. */
    private fun updateTitle() {
        val offset = if (preferences.homeViewMode.usesDayRow) daySelector.selectedOffset else 0
        tbar.title = dateTitle(offset)
    }

    /** "Mon, Oct 19" in the language of the device, for the day [offset] days before today. */
    private fun dateTitle(offset: Int): String {
        val day = getToday().minus(offset)
        val date = LocalDate.of(day.year, day.month, day.day)
        val locale = Locale.getDefault()
        val pattern = DateFormat.getBestDateTimePattern(locale, "EEEMMMd")
        return java.time.format.DateTimeFormatter.ofPattern(pattern, locale).format(date)
    }

    override fun onModelChange() {
        updateEmptyView()
    }

    private fun setupControllers() {
        header.setScrollController(
            object : ScrollableChart.ScrollController {
                override fun onDataOffsetChanged(newDataOffset: Int) {
                    listView.dataOffset = newDataOffset
                }
            }
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupControllers()
        preferences.addListener(this)
        midnightTimer.addListener(this)
        applyViewMode()
        listAdapter.observable.addListener(this)
    }

    override fun onDetachedFromWindow() {
        listAdapter.observable.removeListener(this)
        midnightTimer.removeListener(this)
        preferences.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val count = getCheckmarkCount()
        header.buttonCount = count
        header.setMaxDataOffset(WeekPaging.maxOffset(MAX_CHECKMARK_COUNT, count))
        listView.checkmarkCount = count
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private fun getCheckmarkCount(): Int {
        val buttonWidth = dim(R.dimen.checkmarkWidth)
        val cardInsets = 2 * dp(16f) + 2 * dp(12f)
        val buttonCount = ((measuredWidth - cardInsets) / buttonWidth).toInt()
        return min(MAX_CHECKMARK_COUNT, max(0, buttonCount))
    }

    private fun updateEmptyView() {
        if (listAdapter.itemCount == 0) {
            if (listAdapter.hasNoHabit()) {
                llEmpty.showEmpty()
            } else {
                llEmpty.showDone()
            }
        } else {
            llEmpty.hide()
        }
    }
}
