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

import kotlin.test.Test
import kotlin.test.assertEquals

class IconCategoryTest {
    private val entries = listOf(
        IconEntry("fitness_center", "gym", IconCategory.FITNESS_SPORTS),
        IconEntry("bedtime", "sleep", IconCategory.MIND_SLEEP),
        IconEntry("tooth", "dental gym", IconCategory.HEALTH_CARE),
        IconEntry("mystery", "", IconCategory.SYMBOLS_MORE)
    )

    @Test
    fun testThirteenCategoriesInDisplayOrder() {
        assertEquals(13, IconCategory.entries.size)
        assertEquals(IconCategory.FITNESS_SPORTS, IconCategory.entries.first())
        assertEquals(IconCategory.SYMBOLS_MORE, IconCategory.entries.last())
    }

    @Test
    fun testFromIdFallsBackToSymbols() {
        assertEquals(IconCategory.MONEY, IconCategory.fromId("MONEY"))
        assertEquals(IconCategory.SYMBOLS_MORE, IconCategory.fromId("nope"))
        assertEquals(IconCategory.SYMBOLS_MORE, IconCategory.fromId(null))
    }

    @Test
    fun testBrowseWithoutQueryFiltersByCategory() {
        val names = IconSearch.browse(entries, "", IconCategory.MIND_SLEEP).map { it.name }
        assertEquals(listOf("bedtime"), names)
    }

    @Test
    fun testBrowseWithoutCategoryReturnsAll() {
        assertEquals(entries, IconSearch.browse(entries, "", null))
    }

    @Test
    fun testSearchIgnoresCategory() {
        val names = IconSearch.browse(entries, "gym", IconCategory.MIND_SLEEP).map { it.name }
        assertEquals(listOf("fitness_center", "tooth"), names)
    }
}
