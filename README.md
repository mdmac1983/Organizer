**Organizer** — a native Android app (Kotlin, min SDK Android 8+) for personal organization, built with a gray Material Design aesthetic and Material You dynamic theming (Android 12+, so the UI adapts to the user's wallpaper-based color palette on supported devices).

**Navigation:** Four tabs across the top of the screen — Planner, Contacts, Passwords, Notes — swipeable via `ViewPager2` with a `TabLayout` for direct tab selection. A persistent footer reading "@OrionMD 2026" sits pinned beneath the tab content on every screen.

**Planner tab**
Calendar view with daily/weekly/monthly switching. Users can add entries with a title, time, and notes/description. No reminders or notifications — just a record-keeping calendar, not an alarm system.

**Contacts tab**
A fully separate, manually-maintained contact list — not synced from or pulled off the phone's native contacts. Each entry holds name, phone, email, address, notes, and a photo.

**Passwords tab**
A local password manager. Entries are encrypted at rest and the tab itself is locked behind an app PIN/password. Each entry stores site/app name, username, password, URL, and notes.

**Notes tab**
Supports checklists and formatting (not just plain flat text), organized into folders/categories rather than one long list.
