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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import me.tatarka.inject.annotations.Inject
import nl.dionsegijn.konfetti.xml.KonfettiView
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
import kotlin.math.max
import kotlin.math.min

const val MAX_CHECKMARK_COUNT = 60

@Inject
@ActivityScope
class ListHabitsRootView(
    @ActivityContext context: Context,
    hintListFactory: HintListFactory,
    private val preferences: Preferences,
    midnightTimer: MidnightTimer,
    runner: TaskRunner,
    private val listAdapter: HabitCardListAdapter,
    habitCardListViewFactory: HabitCardListViewFactory
) : FrameLayout(context), ModelObservable.Listener, Preferences.Listener {

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
            title = context.applicationInfo.loadLabel(context.packageManager).toString(),
            displayHomeAsUpEnabled = false
        )
        listView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                tbar.setScrolled(recyclerView.canScrollVertically(-1))
            }
        })
        daySelector.onSelect = { offset -> listView.selectedDayOffset = offset }
        addView(rootView, MATCH_PARENT, MATCH_PARENT)
        listAdapter.setListView(listView)
        applyViewMode()
    }

    override fun onHomeViewModeChanged() {
        post { applyViewMode() }
    }

    /**
     * Weekly shows the scrollable day header, compact a day row to pick the day the cards act on,
     * and the history-based views (monthly, grid) need neither.
     */
    private fun applyViewMode() {
        val mode = preferences.homeViewMode
        header.visibility = if (mode == HomeViewMode.WEEKLY) VISIBLE else GONE
        daySelector.visibility = if (mode == HomeViewMode.COMPACT) VISIBLE else GONE
        listView.viewMode = mode
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
        applyViewMode()
        listAdapter.observable.addListener(this)
    }

    override fun onDetachedFromWindow() {
        listAdapter.observable.removeListener(this)
        preferences.removeListener(this)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val count = getCheckmarkCount()
        header.buttonCount = count
        header.setMaxDataOffset(max(MAX_CHECKMARK_COUNT - count, 0))
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
