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
package org.isoron.uhabits.core.ui.screens.habits.list

import me.tatarka.inject.annotations.Inject
import org.isoron.uhabits.core.models.HabitCategories
import org.isoron.uhabits.core.models.HabitList
import org.isoron.uhabits.core.models.HabitMatcher
import org.isoron.uhabits.core.preferences.Preferences
import org.isoron.uhabits.core.ui.ThemeSwitcher

@Inject
class ListHabitsMenuBehavior(
    private val screen: Screen,
    private val adapter: Adapter,
    private val preferences: Preferences,
    private val themeSwitcher: ThemeSwitcher
) {
    private var showCompleted: Boolean
    private var showArchived: Boolean
    var searchQuery: String = ""
        private set

    /** The category chip selected on the home screen, or null for "All". Not persisted. */
    var selectedCategory: String? = null
        private set

    fun onCreateHabit() {
        screen.showSelectHabitTypeDialog()
    }

    fun onViewSettings() {
        screen.showSettingsScreen()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        updateAdapterFilter()
    }

    fun onCategorySelected(category: String?) {
        if (HabitCategories.isSame(category, selectedCategory)) return
        selectedCategory = HabitCategories.normalize(category)
        updateAdapterFilter()
    }

    fun onToggleShowArchived() {
        showArchived = !showArchived
        preferences.showArchived = showArchived
        updateAdapterFilter()
    }

    fun onToggleShowCompleted() {
        showCompleted = !showCompleted
        preferences.showCompleted = showCompleted
        updateAdapterFilter()
    }

    fun onSortByManually() {
        adapter.primaryOrder = HabitList.Order.BY_POSITION
    }

    fun onSortByColor() {
        onSortToggleBy(HabitList.Order.BY_COLOR_ASC, HabitList.Order.BY_COLOR_DESC)
    }

    fun onSortByScore() {
        onSortToggleBy(HabitList.Order.BY_SCORE_DESC, HabitList.Order.BY_SCORE_ASC)
    }

    fun onSortByName() {
        onSortToggleBy(HabitList.Order.BY_NAME_ASC, HabitList.Order.BY_NAME_DESC)
    }

    fun onSortByStatus() {
        onSortToggleBy(HabitList.Order.BY_STATUS_ASC, HabitList.Order.BY_STATUS_DESC)
    }

    private fun onSortToggleBy(defaultOrder: HabitList.Order, reversedOrder: HabitList.Order) {
        if (adapter.primaryOrder != defaultOrder) {
            if (adapter.primaryOrder != reversedOrder) {
                adapter.secondaryOrder = adapter.primaryOrder
            }
            adapter.primaryOrder = defaultOrder
        } else {
            adapter.primaryOrder = reversedOrder
        }
    }

    fun onToggleNightMode() {
        themeSwitcher.toggleNightMode()
        screen.applyTheme()
    }

    fun onPreferencesChanged() {
        updateAdapterFilter()
    }

    /**
     * Returns the categories to offer as chips: those of the habits shown by the archived setting.
     * Neither the search nor hiding completed habits removes a chip, so that checking off the last
     * habit of the selected category does not silently switch the filter back to "All".
     *
     * If the selected category is no longer one of them (for example its last habit was moved to
     * another category), the selection falls back to "All".
     */
    fun updateCategories(habits: HabitList): List<String> {
        val matcher = HabitMatcher(isArchivedAllowed = showArchived)
        val categories = HabitCategories.distinct(habits.filter { matcher.matches(it) })
        if (selectedCategory != null) {
            val resolved = HabitCategories.resolve(selectedCategory, categories)
            if (resolved == null) onCategorySelected(null) else selectedCategory = resolved
        }
        return categories
    }

    private fun buildMatcher(category: String?) =
        if (preferences.areQuestionMarksEnabled) {
            HabitMatcher(
                isArchivedAllowed = showArchived,
                isEnteredAllowed = showCompleted,
                searchQuery = searchQuery,
                category = category
            )
        } else {
            HabitMatcher(
                isArchivedAllowed = showArchived,
                isCompletedAllowed = showCompleted,
                searchQuery = searchQuery,
                category = category
            )
        }

    private fun updateAdapterFilter() {
        adapter.setFilter(buildMatcher(selectedCategory))
        adapter.refresh()
    }

    interface Adapter {
        fun refresh()
        fun setFilter(matcher: HabitMatcher)
        var primaryOrder: HabitList.Order
        var secondaryOrder: HabitList.Order
    }

    interface Screen {
        fun applyTheme()
        fun showSettingsScreen()
        fun showSelectHabitTypeDialog()
    }

    init {
        showCompleted = preferences.showCompleted
        showArchived = preferences.showArchived
        updateAdapterFilter()
    }
}
