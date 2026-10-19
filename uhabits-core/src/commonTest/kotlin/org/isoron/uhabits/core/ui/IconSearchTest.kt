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

class IconSearchTest {
    private val entries = listOf(
        IconEntry("bluetooth", "wireless"),
        IconEntry("tooth", "dental teeth floss"),
        IconEntry("fitness_center", "gym dumbbell workout"),
        IconEntry("no_adult_content", "porn nsfw"),
        IconEntry("cannabis", "weed marijuana")
    )

    private fun names(query: String) = IconSearch.search(entries, query).map { it.name }

    @Test
    fun testBlankQueryReturnsEverythingInOrder() {
        assertEquals(entries.map { it.name }, names(""))
        assertEquals(entries.map { it.name }, names("  "))
    }

    @Test
    fun testMatchesKeywords() {
        assertEquals(listOf("fitness_center"), names("gym"))
        assertEquals(listOf("no_adult_content"), names("porn"))
        assertEquals(listOf("cannabis"), names("weed"))
        assertEquals(listOf("tooth"), names("floss"))
    }

    @Test
    fun testNameWordPrefixRanksBeforeSubstring() {
        // "tooth" starts the name "tooth" but is only inside "bluetooth".
        assertEquals(listOf("tooth", "bluetooth"), names("tooth"))
    }

    @Test
    fun testNameRanksBeforeKeywords() {
        val list = listOf(IconEntry("scale", "weight gym"), IconEntry("gym_bag", ""))
        assertEquals(listOf("gym_bag", "scale"), IconSearch.search(list, "gym").map { it.name })
    }

    @Test
    fun testAllWordsMustMatch() {
        assertEquals(listOf("fitness_center"), names("fitness gym"))
        assertEquals(emptyList(), names("fitness weed"))
    }

    @Test
    fun testSeparatorsAndCaseAreIgnored() {
        assertEquals(listOf("fitness_center"), names("Fitness_Center"))
        assertEquals(listOf("no_adult_content"), names("no-adult"))
    }
}
