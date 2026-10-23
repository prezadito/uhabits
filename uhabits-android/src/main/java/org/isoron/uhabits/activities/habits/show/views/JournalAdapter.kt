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

package org.isoron.uhabits.activities.habits.show.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.Entry
import org.isoron.uhabits.core.ui.screens.habits.show.views.JournalCardState
import org.isoron.uhabits.core.ui.screens.habits.show.views.JournalEntryState
import org.isoron.uhabits.databinding.ShowHabitJournalRowBinding
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class JournalAdapter : RecyclerView.Adapter<JournalAdapter.ViewHolder>() {
    private var state = JournalCardState(
        entries = emptyList(),
        isNumerical = false,
        isQuit = false,
        unit = ""
    )
    var onEntryClicked: (LocalDate) -> Unit = {}

    fun setState(newState: JournalCardState) {
        state = newState
        notifyDataSetChanged()
    }

    override fun getItemCount() = state.entries.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ShowHabitJournalRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = state.entries[position]
        val context = holder.binding.root.context
        val date = java.time.LocalDate.of(entry.date.year, entry.date.month, entry.date.day)
        holder.binding.journalDate.text = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
        holder.binding.journalValue.text = formatValue(entry, context)
        holder.binding.journalNotes.text = entry.notes
        holder.binding.root.setOnClickListener { onEntryClicked(entry.date) }
    }

    private fun formatValue(entry: JournalEntryState, context: android.content.Context): String {
        if (state.isQuit) return ""
        if (entry.value == Entry.SKIP) return context.getString(R.string.skip_day)
        if (state.isNumerical) {
            if (entry.value < 0) return ""
            val number = DecimalFormat("#.###").format(entry.value / 1000.0)
            return "$number ${state.unit}".trim()
        }
        return when (entry.value) {
            Entry.YES_MANUAL -> context.getString(R.string.yes)
            Entry.NO -> context.getString(R.string.no)
            else -> ""
        }
    }

    class ViewHolder(val binding: ShowHabitJournalRowBinding) : RecyclerView.ViewHolder(binding.root)
}
