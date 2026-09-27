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
package org.isoron.uhabits.utils

import android.content.res.Resources
import org.isoron.platform.time.DateUtils
import org.isoron.platform.time.getToday
import org.isoron.uhabits.R
import org.isoron.uhabits.core.models.CleanTime
import org.isoron.uhabits.core.models.Habit
import org.isoron.uhabits.core.models.getCleanTime
import org.isoron.uhabits.core.models.getLastSlipDate

/**
 * Formats a clean time compactly, for example "12d 4h", "5h 30m" or "20m".
 */
fun formatCleanTime(res: Resources, time: CleanTime): String = when {
    time.days > 0 -> res.getString(R.string.clean_time_days_hours, time.days, time.hours)
    time.hours > 0 -> res.getString(R.string.clean_time_hours_minutes, time.hours, time.minutes)
    else -> res.getString(R.string.clean_time_minutes, time.minutes)
}

/**
 * Returns a short description of how long a quit habit has been kept clean, such as
 * "12d 4h clean", or "Slipped today" if the user slipped today.
 */
fun Habit.formatCleanStatus(res: Resources): String {
    if (getLastSlipDate() == getToday()) return res.getString(R.string.slipped_today)
    val time = getCleanTime(DateUtils.getLocalTime())
    return res.getString(R.string.clean_for, formatCleanTime(res, time))
}
