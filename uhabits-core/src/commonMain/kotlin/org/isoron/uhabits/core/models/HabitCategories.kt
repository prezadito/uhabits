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

/** Helpers for the free-text [Habit.category]: names are trimmed and compared ignoring case. */
object HabitCategories {
    /** The trimmed name, or null if [raw] is null or blank. */
    fun normalize(raw: String?): String? = raw?.trim()?.ifEmpty { null }

    fun isSame(a: String?, b: String?): Boolean {
        val x = normalize(a)
        val y = normalize(b)
        return if (x == null || y == null) x == y else x.equals(y, ignoreCase = true)
    }

    /**
     * The distinct non-empty categories of [habits], sorted alphabetically. When spellings differ
     * only by case, the first one seen is used.
     */
    fun distinct(habits: Iterable<Habit>): List<String> {
        val byKey = LinkedHashMap<String, String>()
        for (habit in habits) {
            val name = normalize(habit.category) ?: continue
            byKey.getOrPut(name.lowercase()) { name }
        }
        return byKey.values.sortedBy { it.lowercase() }
    }

    /** [selected] if it is one of [available], otherwise null, which means "All". */
    fun resolve(selected: String?, available: List<String>): String? =
        available.firstOrNull { isSame(it, selected) }
}
