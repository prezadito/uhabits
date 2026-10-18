<h1 align="center">Habits+</h1>
<p align="center">
  <a href="https://github.com/prezadito/uhabits/releases">
    <img alt="release" src="https://img.shields.io/github/v/release/prezadito/uhabits?include_prereleases" />
  </a>
  <img alt="License: GPL v3" src="https://img.shields.io/badge/license-GPLv3-blue" />
  <img alt="Android 9+" src="https://img.shields.io/badge/Android-9%2B-3ddc84" />
</p>

Habits+ is a mobile app that helps you create and maintain good habits, and
break bad ones. Detailed graphs and statistics show you how your habits improved
over time. It is completely ad-free and open source.

Habits+ is a fork of [Loop Habit Tracker](https://github.com/iSoron/uhabits) by
Álinson Santos Xavier. It keeps everything that makes Loop good, and adds quit
habits, a redesigned interface and a round of accessibility work.

## What is different from Loop

* **Quit a habit.** A third habit type, next to Yes or No and Measurable, for
  things you want to stop doing. The habit shows a live clean-time counter.
  Record a slip with the time it happened, as many times a day as needed, and
  edit it later. A day only counts as clean if it was clean from start to end.
  The slips chart can be grouped by day.

* **A redesigned interface.**
  * Twelve tuned habit colors, each with a fill, a text color and a tint, and
    the same colors in light, dark and pure black themes. Habits you already
    have keep their color.
  * A clean app bar, rounded habit cards with a score ring and a short summary
    line, and a header with the weekday and date of each column.
  * Day tiles that show done, skipped and missed days by shape and symbol as
    well as by color, so they stay readable without relying on color alone.
  * A habit screen with an overview ring, a rounded history calendar and
    recolored charts.
  * Forms with labels above the fields, a bottom sheet for choosing the habit
    type, and refreshed dialogs and color picker.
  * Updated home screen widgets.

* **Accessibility.** Text, borders and chart labels meet WCAG AA contrast in
  every theme. Day tiles, the list header, the score ring, charts and color
  swatches have descriptions for screen readers such as TalkBack, and layouts
  were adjusted for large font sizes.

* **Installs next to Loop.** Habits+ has its own application ID
  (`org.isoron.uhabits.qol`), so you can keep Loop installed and try Habits+
  without touching your existing data.

For the complete list of changes, see the [changelog](CHANGELOG.md).

## Features

* **Habit score.** An advanced formula calculates the strength of your habits.
  Every repetition makes your habit stronger and every missed day makes it
  weaker. A few missed days after a long streak, however, will not completely
  destroy your progress, unlike many other don't-break-the-chain apps.

* **Flexible schedules.** In addition to daily habits, supports habits with
  more complex schedules, such as 3 times per week or every other day.

* **Yes or No, Measurable and Quit habits.** Track whether you did something,
  how much of it you did against a target, or how long you have stayed away
  from it. Add notes to any day.

* **Search and archive.** Find habits by name, question or notes, and archive
  the ones you are not tracking right now.

* **Reminders.** Schedule notifications to remind you of your habits. Each habit
  can have its own reminder, at a chosen time of the day. Easily check or
  dismiss your habit directly from the notification.

* **Widgets.** Track your habits directly from your home screen, without even
  opening the app.

* **Take control of your data.** Export your data to spreadsheets (CSV) or to a
  full backup, and import it again. Backups can be saved automatically to a
  folder of your choice. Habits+ does not change the database format, so it
  stays compatible with Loop. For power users, checkmarks can be added through
  other apps, such as Tasker.

* **No limitations.** Track as many habits as you wish. All features are
  available to everyone. There are no in-app purchases.

* **Completely ad-free and open source.** There are no advertisements, annoying
  notifications or intrusive permissions in this app. The app is open source
  (GPLv3).

* **Works offline and respects your privacy.** No Internet connection or
  account is needed. Your data is never sent to anyone.

## Installing

Habits+ requires Android 9 or newer.

Download the APK from the [releases page][releases] and open it on your phone.
You may need to allow installing apps from your browser or file manager when
Android asks. Habits+ is not on Google Play or F-Droid, so it does not update
itself: to update, download the newer APK and install it over the old one. Your
data is kept.

All Habits+ releases are marked as pre-releases on GitHub, so tick "Show
pre-releases" if you cannot see one.

To move your habits from Loop, export a full backup from Loop, then import it in
the Habits+ settings.

## Building

To build the app from the source code, see the [build instructions][build].
Maintainers can find the release steps in [docs/RELEASING.md][releasing].

## Contributing

Pull requests are welcome. Please read the [guidelines][guidelines] first, and
target the `dev` branch.

* **Changes to features that come from Loop.** For bugs and ideas about
  behavior that Habits+ shares with Loop, such as the habit score or
  reminders, please also check [Loop's issue tracker](https://github.com/iSoron/uhabits/issues),
  since fixes there can often be brought over.

* **Translations.** New text is written in English only. Translations are
  inherited from Loop, which maintains them through its
  [open translation project][poedit].

## Credits and license

Habits+ is built on Loop Habit Tracker, created and maintained by Álinson Santos
Xavier and its contributors. Thank you for the project this one stands on.

<img align="right" alt="GPL v3" src="https://www.gnu.org/graphics/gplv3-88x31.png">

  Copyright (C) 2016-2025 Álinson Santos Xavier <isoron@gmail.com>

  Habits+ and Loop Habit Tracker are free software: you can redistribute them
  and/or modify them under the terms of the GNU General Public License as
  published by the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  They are distributed in the hope that they will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along
  with this program. If not, see <https://www.gnu.org/licenses/>.

[releases]: https://github.com/prezadito/uhabits/releases
[build]: docs/BUILD.md
[releasing]: docs/RELEASING.md
[guidelines]: docs/GUIDELINES.md
[poedit]: https://translate.loophabits.org
