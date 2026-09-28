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
import org.isoron.platform.time.DateUtils
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
     * Asks the user at which time they slipped. The picker starts at the current time of day.
     */
    fun showTimePicker(
        activity: AppCompatActivity,
        color: Int,
        callback: (hour: Int, minute: Int) -> Unit
    ) {
        val minuteOfDay = (DateUtils.getLocalTime() % DateUtils.DAY_LENGTH) / MILLIS_PER_MINUTE
        val dialog = TimePickerDialog.newInstance(
            object : TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view: RadialPickerLayout?, hourOfDay: Int, minute: Int) {
                    callback(hourOfDay, minute)
                }

                override fun onTimeCleared(view: RadialPickerLayout?) {
                }
            },
            (minuteOfDay / 60).toInt(),
            (minuteOfDay % 60).toInt(),
            DateFormat.is24HourFormat(activity),
            color
        )
        dialog.dismissCurrentAndShow(activity.supportFragmentManager, "slipTimePicker")
    }

    /**
     * Shows the slips of a day. Tapping a slip asks for confirmation before deleting it, and the
     * positive button records another slip.
     */
    fun showSlipList(
        activity: AppCompatActivity,
        date: LocalDate,
        slips: List<Long>,
        isToday: Boolean,
        onAdd: () -> Unit,
        onDelete: (timestamp: Long) -> Unit
    ) {
        val times = slips.map { formatSlipTime(activity, it) }.toTypedArray()
        val title = activity.getString(
            R.string.slips_on_date,
            "yMMMd".toSimpleDataFormat().format(Date(date.unixTime))
        )
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setItems(times) { _, which -> confirmDelete(activity, times[which]) { onDelete(slips[which]) } }
            .setPositiveButton(if (isToday) R.string.slipped_again_now else R.string.add_slip) { _, _ ->
                onAdd()
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
