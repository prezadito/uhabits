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

package org.isoron.uhabits.core.ui.views

import org.isoron.platform.gui.Color
import org.isoron.uhabits.core.models.PaletteColor

abstract class Theme {
    open val appBackgroundColor: Color get() = surfaceColor
    open val cardBackgroundColor: Color get() = surfaceRaisedColor
    open val headerBackgroundColor: Color get() = surfaceColor
    open val headerBorderColor = Color(0xcccccc)
    open val headerTextColor = Color(0x9E9E9E)
    open val highContrastTextColor: Color get() = inkColor
    open val itemBackgroundColor = Color(0xffffff)
    open val lowContrastTextColor: Color get() = lineColor
    open val mediumContrastTextColor: Color get() = inkMutedColor
    open val statusBarBackgroundColor = Color(0x333333)
    open val surfaceColor = Color(0xf5f5f5)
    open val surfaceRaisedColor = Color(0xffffff)
    open val inkColor = Color(0x1c1c1c)
    open val inkMutedColor = Color(0x666666)
    open val lineColor = Color(0xe6e6e6)
    open val lineStrongColor = Color(0x8a8a8a)
    open val accentColor = Color(0x1f6fca)
    open val onAccentColor = Color(0xffffff)
    open val accentTintColor = Color(0xe1eefe)
    open val onHabitColor = Color(0xffffff)
    open val onHabitTextColor = Color(0x1c1c1c)

    protected open val habitFills = listOf(Color(0xe9615c), Color(0xdd6f23), Color(0xbf8105), Color(0x809703), Color(0x3ca059), Color(0x189e8c), Color(0x0d9caf), Color(0x418eec), Color(0x7786eb), Color(0xa17adf), Color(0xd268b2), Color(0x8d8d8d))
    protected open val habitInks = listOf(Color(0xc43f3e), Color(0xb45302), Color(0x966503), Color(0x647703), Color(0x0e803b), Color(0x067c6c), Color(0x087988), Color(0x1f6fca), Color(0x5a66c7), Color(0x825abc), Color(0xaf4791), Color(0x6f6f6f))
    protected open val habitTints = listOf(Color(0xfee8e6), Color(0xffe8dc), Color(0xfdead0), Color(0xe8f0d3), Color(0xd9f3dd), Color(0xcff5ec), Color(0xcef3fa), Color(0xe1eefe), Color(0xe7ecff), Color(0xf0e9ff), Color(0xfee7f5), Color(0xededed))

    fun color(paletteColor: PaletteColor): Color = color(paletteColor.paletteIndex)

    open fun color(paletteIndex: Int): Color = habitFill(paletteIndex)

    fun habitFill(paletteColor: PaletteColor): Color = habitFill(paletteColor.paletteIndex)

    fun habitInk(paletteColor: PaletteColor): Color = habitInk(paletteColor.paletteIndex)

    fun habitTint(paletteColor: PaletteColor): Color = habitTint(paletteColor.paletteIndex)

    fun habitFill(paletteIndex: Int): Color = habitFills[PaletteColor(paletteIndex).hue]

    fun habitInk(paletteIndex: Int): Color = habitInks[PaletteColor(paletteIndex).hue]

    fun habitTint(paletteIndex: Int): Color = habitTints[PaletteColor(paletteIndex).hue]

    val checkmarkButtonSize = 48.0
    val smallTextSize = 10.0
    val regularTextSize = 17.0
}

open class LightTheme : Theme()

open class DarkTheme : Theme() {
    override val headerBorderColor = Color(0xcccccc)
    override val headerTextColor = Color(0x9E9E9E)
    override val itemBackgroundColor = Color(0xffffff)
    override val statusBarBackgroundColor = Color(0x333333)
    override val surfaceColor = Color(0x161616)
    override val surfaceRaisedColor = Color(0x212121)
    override val inkColor = Color(0xf2f2f2)
    override val inkMutedColor = Color(0xa6a6a6)
    override val lineColor = Color(0x2e2e2e)
    override val lineStrongColor = Color(0x7a7a7a)
    override val accentColor = Color(0x7baff2)
    override val onAccentColor = Color(0x161616)
    override val accentTintColor = Color(0x222f3f)
    override val onHabitColor = Color(0x161616)
    override val onHabitTextColor = Color(0x0f0f0f)

    override val habitFills = listOf(Color(0xcd5c57), Color(0xc2672d), Color(0xad7501), Color(0x74881f), Color(0x3f8f55), Color(0x298e7e), Color(0x208b9a), Color(0x4281cf), Color(0x6c7acd), Color(0x8f70c4), Color(0xb9619e), Color(0x7f7f7f))
    override val habitInks = listOf(Color(0xf29189), Color(0xe99a6d), Color(0xd3a35d), Color(0xa1b365), Color(0x7bbb88), Color(0x70baab), Color(0x6cb7c4), Color(0x7baff2), Color(0x9aa9f1), Color(0xb89fe7), Color(0xe195c7), Color(0xababab))
    override val habitTints = listOf(Color(0x3f2826), Color(0x3d2a1f), Color(0x392c1a), Color(0x2b301c), Color(0x203223), Color(0x17332d), Color(0x163236), Color(0x222f3f), Color(0x292d40), Color(0x312b3e), Color(0x3b2834), Color(0x2e2e2e))
}

class PureBlackTheme : DarkTheme() {
    override val surfaceColor = Color(0x000000)
    override val surfaceRaisedColor = Color(0x121212)
    override val inkColor = Color(0xf2f2f2)
    override val inkMutedColor = Color(0xa6a6a6)
    override val lineColor = Color(0x222222)
    override val lineStrongColor = Color(0x6e6e6e)
    override val accentColor = Color(0x7baff2)
    override val onAccentColor = Color(0x000000)
    override val accentTintColor = Color(0x1a2433)
    override val onHabitColor = Color(0x000000)
    override val onHabitTextColor = Color(0x000000)
}

class WidgetTheme : LightTheme() {
    override val appBackgroundColor = Color(0xf4f4f4)
    override val headerBackgroundColor = Color(0xeeeeee)
    override val cardBackgroundColor = Color.TRANSPARENT
    override val highContrastTextColor = Color.WHITE
    override val mediumContrastTextColor = Color.WHITE.withAlpha(0.50)
    override val lowContrastTextColor = Color.WHITE.withAlpha(0.10)
}
