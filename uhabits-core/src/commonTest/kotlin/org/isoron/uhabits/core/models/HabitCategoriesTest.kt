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

import org.isoron.uhabits.core.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HabitCategoriesTest : BaseUnitTest() {
    private fun buildHabit(name: String, category: String?, archived: Boolean = false): Habit {
        val habit = modelFactory.buildHabit()
        habit.name = name
        habit.category = category
        habit.isArchived = archived
        return habit
    }

    @Test
    fun testNormalize() {
        assertEquals("Health", HabitCategories.normalize("  Health "))
        assertNull(HabitCategories.normalize("   "))
        assertNull(HabitCategories.normalize(""))
        assertNull(HabitCategories.normalize(null))
    }

    @Test
    fun testIsSame() {
        assertTrue(HabitCategories.isSame("Health", " health "))
        assertTrue(HabitCategories.isSame(null, "  "))
        assertFalse(HabitCategories.isSame("Health", "Work"))
        assertFalse(HabitCategories.isSame("Health", null))
    }

    @Test
    fun testDistinct() {
        val habits = listOf(
            buildHabit("a", "work"),
            buildHabit("b", "Health"),
            buildHabit("c", "Work "),
            buildHabit("d", null),
            buildHabit("e", "  "),
            buildHabit("f", "health")
        )
        assertEquals(listOf("Health", "work"), HabitCategories.distinct(habits))
        assertEquals(emptyList(), HabitCategories.distinct(emptyList()))
    }

    @Test
    fun testResolve() {
        val available = listOf("Health", "Work")
        assertEquals("Work", HabitCategories.resolve("work", available))
        assertNull(HabitCategories.resolve("Chores", available))
        assertNull(HabitCategories.resolve(null, available))
    }

    @Test
    fun testMatcherByCategory() {
        val health = buildHabit("a", "Health")
        val work = buildHabit("b", "Work")
        val none = buildHabit("c", null)
        val habits = listOf(health, work, none)

        assertEquals(habits, habits.filter { HabitMatcher().matches(it) })
        assertEquals(habits, habits.filter { HabitMatcher(category = "  ").matches(it) })
        assertEquals(listOf(health), habits.filter { HabitMatcher(category = "health").matches(it) })
        assertEquals(listOf(work), habits.filter { HabitMatcher(category = "Work").matches(it) })
        assertEquals(emptyList(), habits.filter { HabitMatcher(category = "Chores").matches(it) })
    }

    @Test
    fun testMatcherCombinesCategoryWithSearchAndArchive() {
        val a = buildHabit("Run", "Health")
        val b = buildHabit("Yoga", "Health")
        val c = buildHabit("Old", "Health", archived = true)
        val habits = listOf(a, b, c)
        val matcher = HabitMatcher(category = "Health", searchQuery = "yo")
        assertEquals(listOf(b), habits.filter { matcher.matches(it) })
        assertEquals(listOf(a, b), habits.filter { HabitMatcher(category = "Health").matches(it) })
    }
}
