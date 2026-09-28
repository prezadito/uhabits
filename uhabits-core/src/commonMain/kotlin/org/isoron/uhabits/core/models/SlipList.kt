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
package org.isoron.uhabits.core.models

import org.isoron.platform.Synchronized
import org.isoron.platform.time.LocalDate

/**
 * The slips of a quit habit. Each slip is the moment the user slipped, as local wall-clock
 * time in milliseconds (same convention as [Habit.quitSince]). A day may contain any number of
 * slips, but two slips never share the exact same timestamp.
 */
open class SlipList {
    private val timestamps = mutableListOf<Long>()

    /**
     * Returns all slips, from the most recent to the oldest.
     */
    @Synchronized
    open fun getAll(): List<Long> = timestamps.toList()

    /**
     * Adds a slip. Does nothing if a slip with the same timestamp already exists.
     */
    @Synchronized
    open fun add(timestamp: Long) {
        if (timestamp in timestamps) return
        val index = timestamps.indexOfFirst { it < timestamp }
        if (index < 0) timestamps.add(timestamp) else timestamps.add(index, timestamp)
    }

    @Synchronized
    open fun remove(timestamp: Long) {
        timestamps.remove(timestamp)
    }

    @Synchronized
    open fun clear() {
        timestamps.clear()
    }

    /**
     * Returns the most recent slip, or null if there are none.
     */
    fun getLatest(): Long? = getAll().firstOrNull()

    /**
     * Returns the slips that happened on the given day, from the earliest to the latest.
     */
    fun getByDate(date: LocalDate): List<Long> {
        val start = date.unixTime
        val end = date.plus(1).unixTime
        return getAll().filter { it in start until end }.reversed()
    }
}
