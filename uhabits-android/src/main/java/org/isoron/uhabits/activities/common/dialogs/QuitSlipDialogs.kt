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
package org.isoron.uhabits.activities.common.dialogs

import android.text.format.DateFormat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.datetimepicker.time.RadialPickerLayout
import com.android.datetimepicker.time.TimePickerDialog
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.R
import org.isoron.uhabits.utils.dismissCurrentAndShow
import org.isoron.uhabits.utils.formatTime
import org.isoron.uhabits.utils.toSimpleDataFormat
import java.util.Date

/**
 * Dialogs used to record and remove the slips of quit habits.
 */
object QuitSlipDialogs {
    private const val MILLIS_PER_MINUTE = 60_000L

    /**
     * Asks the user at which time they slipped, starting at the given time of day.
     */
    fun showTimePicker(
        activity: AppCompatActivity,
        color: Int,
        initialHour: Int,
        initialMinute: Int,
        callback: (hour: Int, minute: Int) -> Unit
    ) {
        val dialog = TimePickerDialog.newInstance(
            object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
                    callback(hourOfDay, minute)
                }

                override fun onTimeCleared(view: RadialPickerLayout?) {
                }
            },
            initialHour,
            initialMinute,
            DateFormat.is24HourFormat(activity),
            color
        )
        dialog.dismissCurrentAndShow(activity.supportFragmentManager, "slipTimePicker")
    }

    /**
     * Shows the slips of a day. Tapping a slip offers to change its time or delete it (after
     * confirmation), and the positive button records another slip.
     */
    fun showSlipList(
        activity: AppCompatActivity,
        date: LocalDate,
        slips: List<Long>,
        isToday: Boolean,
        onAdd: () -> Unit,
        onEdit: (timestamp: Long) -> Unit,
        onDelete: (timestamp: Long) -> Unit
    ) {
        val times = slips.map { formatSlipTime(activity, it) }.toTypedArray()
        val title = activity.getString(
            R.string.slips_on_date,
            "yMMMd".toSimpleDataFormat().format(Date(date.unixTime))
        )
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(times) { _, which ->
                showSlipActions(activity, times[which], { onEdit(slips[which]) }) { onDelete(slips[which]) }
            }
            .setPositiveButton(R.string.add_slip) { _, _ ->
                onAdd()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .dismissCurrentAndShow()
    }

    private fun showSlipActions(
        activity: AppCompatActivity,
        time: String,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        val actions = arrayOf(activity.getString(R.string.change_slip_time), activity.getString(R.string.delete))
        AlertDialog.Builder(activity)
            .setTitle(time)
            .setItems(actions) { _, which ->
                if (which == 0) onEdit() else confirmDelete(activity, time, onDelete)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .dismissCurrentAndShow()
    }

    private fun confirmDelete(activity: AppCompatActivity, time: String, onConfirmed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setMessage(activity.getString(R.string.delete_slip_message, time))
            .setPositiveButton(R.string.delete) { _, _ -> onConfirmed() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
            .dismissCurrentAndShow()
    }

    private fun formatSlipTime(activity: AppCompatActivity, timestamp: Long): String {
        val minuteOfDay = ((timestamp - LocalDate.fromUnixTime(timestamp).unixTime) / MILLIS_PER_MINUTE).toInt()
        return formatTime(activity, minuteOfDay / 60, minuteOfDay % 60) ?: ""
    }
}
