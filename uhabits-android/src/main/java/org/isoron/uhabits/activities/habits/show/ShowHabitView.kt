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

package org.isoron.uhabits.activities.habits.show

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnAttach
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import android.content.res.ColorStateList
import org.isoron.platform.time.LocalDate
import org.isoron.platform.time.getToday
import org.isoron.platform.gui.toInt
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitPresenter
import org.isoron.uhabits.core.ui.screens.habits.show.ShowHabitState
import org.isoron.uhabits.R
import org.isoron.uhabits.activities.habits.show.views.JournalAdapter
import org.isoron.uhabits.databinding.ShowHabitBinding
import org.isoron.uhabits.databinding.ShowHabitTabJournalBinding
import org.isoron.uhabits.databinding.ShowHabitTabOverviewBinding
import org.isoron.uhabits.utils.applyBottomInset
import org.isoron.uhabits.utils.applyToolbarInsets
import org.isoron.uhabits.utils.setScrolled
import org.isoron.uhabits.utils.setupToolbar

class ShowHabitView(context: Context) : FrameLayout(context) {
    private val binding = ShowHabitBinding.inflate(LayoutInflater.from(context))
    private val overview = ShowHabitTabOverviewBinding.inflate(LayoutInflater.from(context))
    private val journal = ShowHabitTabJournalBinding.inflate(LayoutInflater.from(context))
    private val journalAdapter = JournalAdapter()

    init {
        binding.toolbar.applyToolbarInsets()
        addView(binding.root)

        journal.journalList.layoutManager = LinearLayoutManager(context)
        journal.journalList.adapter = journalAdapter
        ViewCompat.setOnApplyWindowInsetsListener(journal.journalList) { list, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val clearance = resources.getDimensionPixelSize(R.dimen.journal_list_bottom_clearance)
            list.setPadding(list.paddingLeft, list.paddingTop, list.paddingRight, systemBars.bottom + clearance)
            insets
        }
        val fabMargin = resources.getDimensionPixelSize(R.dimen.ds_space_16)
        ViewCompat.setOnApplyWindowInsetsListener(journal.journalAdd) { fab, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            (fab.layoutParams as MarginLayoutParams).bottomMargin = fabMargin + systemBars.bottom
            fab.requestLayout()
            insets
        }
        // The journal page joins the window after the first insets dispatch, so ask again.
        journal.journalAdd.doOnAttach { ViewCompat.requestApplyInsets(it) }
        journal.journalList.doOnAttach { ViewCompat.requestApplyInsets(it) }

        overview.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.toolbar.setScrolled(scrollY > 0)
        }
        journal.journalList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                binding.toolbar.setScrolled(recyclerView.canScrollVertically(-1))
            }
        })

        val pages = listOf<View>(overview.root, journal.root)
        binding.pager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun getItemCount() = pages.size
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val container = FrameLayout(parent.context)
                container.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                return object : RecyclerView.ViewHolder(container) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val container = holder.itemView as FrameLayout
                val page = pages[position]
                if (page.parent === container) return
                (page.parent as? ViewGroup)?.removeView(page)
                container.removeAllViews()
                container.addView(
                    page,
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
            }
        }
        binding.pager.offscreenPageLimit = pages.size
        TabLayoutMediator(binding.tabs, binding.pager) { tab, position ->
            tab.setText(if (position == 0) R.string.overview else R.string.journal)
        }.attach()
        binding.pager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.toolbar.setScrolled(
                    if (position == 0) {
                        overview.scrollView.scrollY > 0
                    } else {
                        journal.journalList.canScrollVertically(-1)
                    }
                )
            }
        })
    }

    fun setState(data: ShowHabitState) {
        setupToolbar(
            binding.toolbar,
            title = data.title,
            titleColor = data.theme.habitInk(data.color).toInt()
        )
        overview.subtitleCard.setState(data.subtitle)
        overview.overviewCard.setState(data.overview)
        overview.quitCard.setState(data.quit)
        overview.notesCard.setState(data.notes)
        overview.targetCard.setState(data.target)
        overview.streakCard.setState(data.streaks)
        overview.scoreCard.setState(data.scores)
        overview.frequencyCard.setState(data.frequency)
        overview.historyCard.setState(data.history)
        overview.barCard.setState(data.bar)
        if (data.isNumerical) {
            overview.overviewCard.visibility = GONE
        } else {
            overview.targetCard.visibility = GONE
        }
        if (!data.isQuit) overview.quitCard.visibility = GONE
        overview.linearLayout.applyBottomInset()

        journal.journalAdd.backgroundTintList =
            ColorStateList.valueOf(data.theme.color(data.color).toInt())
        journalAdapter.setState(data.journal)
        val isEmpty = data.journal.entries.isEmpty()
        journal.journalEmpty.visibility = if (isEmpty) VISIBLE else GONE
        journal.journalList.visibility = if (isEmpty) GONE else VISIBLE
    }

    fun setListener(presenter: ShowHabitPresenter) {
        overview.scoreCard.setListener(presenter.scoreCardPresenter)
        overview.historyCard.setListener(presenter.historyCardPresenter)
        overview.barCard.setListener(presenter.barCardPresenter)
        journal.journalAdd.setOnClickListener { presenter.historyCardPresenter.onEditNotes(getToday()) }
        journalAdapter.onEntryClicked = { date: LocalDate -> presenter.historyCardPresenter.onEditNotes(date) }
    }
}
