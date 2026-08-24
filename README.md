OrionMD Launcher
Part personal organizer, part full launcher replacement — one app doing both jobs.
Organizer tabs
Planner
Day / Week / Month view toggle, switchable at any time
Week view starts on Sunday
Day/Week/Month header with prev/next navigation and a date label stays visible while scrolling
Tapping the date label jumps back to today
Month view highlights which days have entries; tapping a day selects it
Add via floating action button — entries have a title, time, and notes (no reminders/notifications)
Tap an entry to edit; long-press to delete (confirmation required)
Contacts
Fully manual list — not synced from the phone's contacts
Fields: name, phone, email, address, notes, photo
Add via floating action button
Tap to edit, long-press to delete (confirmed)
Passwords
Locked behind your app PIN — set at first launch (not just on first visit to this tab), protects this tab specifically
Entries encrypted at rest; encryption is PBKDF2-derived from your PIN rather than the device Keystore, so backups stay portable across devices and reinstalls
Fields: site/app name, username, password, URL, notes
Tap-to-copy password to clipboard, with a confirmation toast
Add via floating action button
Tap to edit, long-press to delete (confirmed)
Notes
Folders/categories via chips at the top (All, Unfiled, custom folders, plus a "+ New folder" action)
Checklist-capable formatting, not just flat plain text
Add via floating action button, scoped to the currently selected folder
Tap to open the full editor, long-press to delete (confirmed)
Backup & Restore
Import/export for all app data (Planner, Contacts, Passwords, Notes)
Password encryption is PIN-derived specifically so exported backups can be restored on a different device without losing access
App shell
Splash screen on launch, custom background image behind all four organizer tabs (now the same image as your chosen Home wallpaper, with a static fallback if none is set)
Roboto Bold typography throughout, bold tab labels
Minimum Android 8+, built and tested against Android 10
MIT-licensed, built via GitHub Actions CI
Home screen (when set as your device's Home app)
Guest profile loads by default: fixed 5×5 grid, 5-slot dock, apps hand-picked by Owner, zero editing rights
Owner profile: adjustable grid (3–6 columns × 3–8 rows) and dock (4–6 slots), full app-assignment control for both profiles via a checklist
Pinch-out toggles Guest ↔ Owner instantly, no PIN
Swipe up opens the drawer (slides up over Home); swipe down closes it
Double-tap-and-hold anywhere toggles touch-block (screen-touch lockout, with a notification to turn it back off)
Long-press opens a popup anchored exactly where you pressed — Guest sees "Wallpapers" only, Owner sees "Wallpapers" + "Home settings"
App labels render in fixed white with a drop shadow, so they stay legible over any wallpaper regardless of theme
Status bar and nav bar are fully transparent, dock background is transparent — wallpaper shows through everywhere
App drawer
Alphabetical, searchable, scrollbar on the side
Long-press an app for: Add to Home, Pin to Dock, App Info, Uninstall, Hide
Hidden apps live in a folder inside the grid, unhidden the same way
Adjustable icon opacity, brightness (stored, not yet visually applied), background transparency (fades to reveal the Home wallpaper), and column count
Gestures (Settings → Gestures)
Swipe up/down/left/right, double-tap, pinch-in — each assignable to: open drawer, expand notifications, expand quick settings, recent apps, lock screen, launch a specific app, switch default launcher, or nothing
Pinch-out is reserved (profile toggle, not reassignable)
Wallpaper picker
9 bundled photos (bubblegum, flora, canyon, escape, kepler, out-of-the-box, work, chroma, architecture) + "pick from gallery"
Full-screen live preview, thumbnail strip along the bottom, "Set wallpaper" to confirm
Same wallpaper also appears behind the four organizer tabs (falls back to the original static background if none picked)
Theme
Three modes: Light, Dark, Light-gray — teal accent throughout, squared corners app-wide
Fully decoupled from the device's own system dark/light setting
Status bar/nav bar icon color follows whichever mode is active
Apps management (Settings → Apps)
Full list of installed apps in one screen
Tap an icon to replace it with a custom image from your gallery
Tap a label to rename it (or reset to original)
Toggle to hide/unhide, same mechanism the drawer uses
Touch-block
Real Android Accessibility Service — overlays and blocks all screen touches
Trigger: double-tap-and-hold anywhere on Home
Persistent notification with a "Disable" action while active
Onboarding (first launch only)
After PIN setup: a checklist screen — storage access, notifications, default launcher, accessibility service, device admin — each with live status and a one-tap system-screen shortcut
"Continue" always available regardless of what's been granted; nothing is forced
About (Settings, top row)
App icon, name, version number, package name, footer
Settings screen itself
Searchable, stock-Android-style row list — colored square icon (real glyphs, not blank squares) + title + subtitle per row
Rows: About, Default launcher, Home screen, App drawer, Gestures, Wallpaper, Theme, Apps, Touch block, direct shortcuts into Planner/Contacts/Passwords/Notes, General (legacy PIN/storage/battery/device-admin controls, fully preserved from before the redesign)
Branding
Name: OrionMD Launcher; custom icon from a provided image; package id unchanged (com.mdmac.organizer)
Built and tested on a 600×1024 Android 10 tablet. Not distributed via Play Store — sideload the APK below.
