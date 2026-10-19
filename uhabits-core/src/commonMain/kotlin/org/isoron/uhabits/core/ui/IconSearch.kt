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

/** An icon that can be searched by its [name] and by related [keywords], e.g. "gym". */
data class IconEntry(val name: String, val keywords: String)

object IconSearch {
    /**
     * Returns the entries that match every word of [query], best matches first. A word matches
     * the start of a word in the name best, then anywhere in the name, then the start of a
     * keyword, then anywhere in the keywords. Entries that rank equally keep their order. A
     * blank query matches everything.
     */
    fun search(entries: List<IconEntry>, query: String): List<IconEntry> {
        val queryWords = split(query)
        if (queryWords.isEmpty()) return entries
        val ranked = mutableListOf<Pair<Int, IconEntry>>()
        for (entry in entries) {
            val nameWords = split(entry.name)
            val keywordWords = entry.keywords.split(' ').filter { it.isNotEmpty() }
            var worst = 0
            for (word in queryWords) {
                val rank = when {
                    nameWords.any { it.startsWith(word) } -> 0
                    entry.name.contains(word) -> 1
                    keywordWords.any { it.startsWith(word) } -> 2
                    entry.keywords.contains(word) -> 3
                    else -> -1
                }
                if (rank < 0) {
                    worst = -1
                    break
                }
                if (rank > worst) worst = rank
            }
            if (worst >= 0) ranked.add(worst to entry)
        }
        return ranked.sortedBy { it.first }.map { it.second }
    }

    private fun split(text: String): List<String> =
        text.lowercase().split(' ', '_', '-').filter { it.isNotEmpty() }
}
