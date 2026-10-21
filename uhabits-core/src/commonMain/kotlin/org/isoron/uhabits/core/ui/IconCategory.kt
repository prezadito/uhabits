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

package org.isoron.uhabits.core.ui

/** The groups the icon picker can be filtered by. Declared in the order they are shown. */
enum class IconCategory {
    FITNESS_SPORTS,
    HEALTH_CARE,
    MIND_SLEEP,
    FOOD_DRINK,
    LEARNING_WORK,
    HOME_CHORES,
    MONEY,
    FAMILY_FRIENDS,
    HOBBIES_CREATIVITY,
    NATURE_TRAVEL,
    QUIT_LIMIT,
    TIME_ROUTINE,
    SYMBOLS_MORE;

    companion object {
        /** The category stored under [id] in the icon data files; unknown ids are "Symbols & More". */
        fun fromId(id: String?): IconCategory =
            entries.firstOrNull { it.name == id } ?: SYMBOLS_MORE
    }
}
