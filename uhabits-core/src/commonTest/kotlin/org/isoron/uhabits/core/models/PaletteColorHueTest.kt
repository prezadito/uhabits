package org.isoron.uhabits.core.models

import org.isoron.uhabits.core.ui.views.DarkTheme
import org.isoron.uhabits.core.ui.views.LightTheme
import org.isoron.uhabits.core.ui.views.PureBlackTheme
import org.isoron.uhabits.core.ui.views.WidgetTheme
import kotlin.test.Test
import kotlin.test.assertEquals

class PaletteColorHueTest {
    @Test
    fun classicIndicesMapToHues() {
        val expected = listOf(0, 1, 1, 2, 2, 3, 3, 4, 5, 6, 7, 7, 8, 9, 9, 10, 2, 11, 11, 11, 12, 13, 14, 15)
        assertEquals(expected, (0..23).map { PaletteColor(it).hue })
    }

    @Test
    fun canonicalIndexMapsBackToHue() {
        for (hue in HUE_CANONICAL_INDEX.indices) {
            assertEquals(hue, PaletteColor.fromHue(hue).hue)
        }
    }

    @Test
    fun rePickingSameHueKeepsStoredIndex() {
        for (index in 0..HUE_CANONICAL_INDEX.max()) {
            val original = PaletteColor(index)
            assertEquals(original, original.afterPickingHue(original.hue))
        }
    }

    @Test
    fun pickingOtherHueStoresCanonicalIndex() {
        for (index in 0..HUE_CANONICAL_INDEX.max()) {
            for (hue in HUE_CANONICAL_INDEX.indices) {
                if (hue == PaletteColor(index).hue) continue
                assertEquals(
                    PaletteColor(HUE_CANONICAL_INDEX[hue]),
                    PaletteColor(index).afterPickingHue(hue)
                )
            }
        }
    }

    @Test
    fun colorEqualsHabitFill() {
        for (theme in listOf(LightTheme(), DarkTheme(), PureBlackTheme(), WidgetTheme())) {
            for (i in 0..HUE_CANONICAL_INDEX.max()) {
                assertEquals(theme.habitFill(i), theme.color(i))
                assertEquals(theme.habitFill(i), theme.color(PaletteColor(i)))
            }
        }
    }

    @Test
    fun yellowShowsAmberFill() {
        assertEquals(LightTheme().habitFill(3), LightTheme().habitFill(4))
        assertEquals(DarkTheme().habitFill(3), DarkTheme().habitFill(4))
    }

    @Test
    fun unknownIndexFallsBackToGraphite() {
        for (index in listOf(-1, 24, 99)) {
            assertEquals(11, PaletteColor(index).hue)
            assertEquals("#757575", PaletteColor(index).toCsvColor())
            for (theme in listOf(LightTheme(), DarkTheme())) {
                assertEquals(theme.habitFill(HUE_CANONICAL_INDEX[11]), theme.habitFill(index))
                assertEquals(theme.habitInk(HUE_CANONICAL_INDEX[11]), theme.habitInk(index))
                assertEquals(theme.habitTint(HUE_CANONICAL_INDEX[11]), theme.habitTint(index))
            }
        }
    }

    @Test
    fun newHuesHaveDistinctFills() {
        for (theme in listOf(LightTheme(), DarkTheme())) {
            assertEquals(HUE_CANONICAL_INDEX.size, HUE_CANONICAL_INDEX.indices.map { theme.habitFill(HUE_CANONICAL_INDEX[it]) }.toSet().size)
        }
    }
}
