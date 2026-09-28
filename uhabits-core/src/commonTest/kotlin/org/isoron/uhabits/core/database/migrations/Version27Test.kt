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
package org.isoron.uhabits.core.database.migrations

import kotlinx.coroutines.test.runTest
import org.isoron.platform.io.Database
import org.isoron.platform.io.format
import org.isoron.platform.io.migrateTo
import org.isoron.platform.io.query
import org.isoron.platform.io.run
import org.isoron.platform.time.LocalDate
import org.isoron.uhabits.core.BaseUnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Version27Test : BaseUnitTest() {
    private lateinit var db: Database
    private val day = LocalDate(2015, 1, 20).unixTime
    private val endOfDay = 23 * 3_600_000L + 59 * 60_000L

    private suspend fun migrateTo(version: Int) {
        db.migrateTo(version) { v ->
            val path = "migrations/${format("%02d.sql", v)}"
            fileOpener.openResourceFile(path).lines().joinToString("\n")
        }
    }

    @Test
    fun testMigrateTo27MovesSlipsOfQuitHabits() = runTest {
        db = openDatabaseResource("/databases/022.db")
        migrateTo(26)
        db.run("delete from Repetitions")
        db.run("delete from Habits")
        db.run(
            """
            insert into Habits(id, name, freq_num, freq_den, color, position, archived, type)
            values (100, 'Quit', 1, 1, 0, 0, 0, 2), (101, 'Run', 1, 1, 0, 1, 0, 0)
            """
        )
        db.run(
            """
            insert into Repetitions(habit, timestamp, value, notes) values
            (100, $day, 0, ''),
            (100, ${day + 86_400_000L}, 0, 'Party'),
            (100, ${day + 2 * 86_400_000L}, -1, 'Tough day'),
            (101, $day, 0, '')
            """
        )

        migrateTo(27)

        val slips = mutableListOf<List<Long>>()
        db.query("select habit, timestamp from QuitSlips order by timestamp") { stmt ->
            slips.add(listOf(stmt.getLong(0), stmt.getLong(1)))
        }
        assertEquals(
            listOf(
                listOf(100L, day + endOfDay),
                listOf(100L, day + 86_400_000L + endOfDay)
            ),
            slips
        )

        val repetitions = mutableListOf<String>()
        db.query("select habit, timestamp, value, notes from Repetitions order by habit, timestamp") { stmt ->
            repetitions.add("${stmt.getLong(0)} ${stmt.getLong(1)} ${stmt.getInt(2)} ${stmt.getText(3)}")
        }
        assertEquals(
            listOf(
                "100 ${day + 86_400_000L} -1 Party",
                "100 ${day + 2 * 86_400_000L} -1 Tough day",
                "101 $day 0 "
            ),
            repetitions
        )
    }
}
