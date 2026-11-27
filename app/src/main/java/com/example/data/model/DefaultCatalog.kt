package com.example.data.model

object DefaultCatalog {

    val palettes: List<ColorPalettePreset> = listOf(
        ColorPalettePreset(
            id = "nordic_frost",
            name = "Nordic Frost",
            backgroundArgb = 0xFF1A2B3CL,
            accentArgb = 0xFF5EEAD4L,
            glyphArgb = 0xFFF0FDFAL
        ),
        ColorPalettePreset(
            id = "midnight_ink",
            name = "Midnight Ink",
            backgroundArgb = 0xFF141824L,
            accentArgb = 0xFFA5B4FCL,
            glyphArgb = 0xFFEEF2FFL
        ),
        ColorPalettePreset(
            id = "sage_botanical",
            name = "Sage Leaf",
            backgroundArgb = 0xFF1C2B24L,
            accentArgb = 0xFF86EFACL,
            glyphArgb = 0xFFF0FDF4L
        ),
        ColorPalettePreset(
            id = "warm_terracotta",
            name = "Terracotta",
            backgroundArgb = 0xFF33201AL,
            accentArgb = 0xFFFDBA74L,
            glyphArgb = 0xFFFFF7EDL
        ),
        ColorPalettePreset(
            id = "lavender_mist",
            name = "Lavender Mist",
            backgroundArgb = 0xFF282036L,
            accentArgb = 0xFFD8B4FEL,
            glyphArgb = 0xFFFAF5FFL
        ),
        ColorPalettePreset(
            id = "pastel_pebble",
            name = "Pastel Pebble",
            backgroundArgb = 0xFFE2ECE9L,
            accentArgb = 0xFF0F766EL,
            glyphArgb = 0xFF132A2EL
        ),
        ColorPalettePreset(
            id = "monochrome_onyx",
            name = "Onyx Mono",
            backgroundArgb = 0xFF18181BL,
            accentArgb = 0xFFA1A1AAL,
            glyphArgb = 0xFFFAFAFAL
        )
    )

    val wallpapers: List<WallpaperPreset> = listOf(
        WallpaperPreset(id = "alpine", name = "Nordic Alpine", isDark = true),
        WallpaperPreset(id = "dusk", name = "Warm Dusk", isDark = true),
        WallpaperPreset(id = "obsidian", name = "OLED Ink", isDark = true),
        WallpaperPreset(id = "sand", name = "Alabaster Mist", isDark = false)
    )

    data class AppTemplate(
        val appName: String,
        val packageName: String,
        val activityName: String,
        val glyphKind: GlyphKind,
        val category: GlyphCategory,
        val isFavoriteDefault: Boolean,
        val tintArgb: Long? = null
    )

    val starterApps: List<AppTemplate> = listOf(
        AppTemplate("Browser", "com.android.chrome", "com.google.android.apps.chrome.Main", GlyphKind.GLOBE, GlyphCategory.SOCIAL, true, 0xFF2563EBL),
        AppTemplate("Calculator", "com.google.android.calculator", "com.android.calculator2.Calculator", GlyphKind.CALCULATOR, GlyphCategory.PRODUCTIVITY, false),
        AppTemplate("Calendar", "com.google.android.calendar", "com.android.calendar.AllInOneActivity", GlyphKind.CALENDAR, GlyphCategory.PRODUCTIVITY, true, 0xFF0D9488L),
        AppTemplate("Camera", "com.android.camera2", "com.android.camera.CameraLauncher", GlyphKind.CAMERA, GlyphCategory.MEDIA, true, 0xFF475569L),
        AppTemplate("Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock", GlyphKind.CLOCK, GlyphCategory.PRODUCTIVITY, false),
        AppTemplate("Contacts", "com.google.android.contacts", "com.android.contacts.activities.PeopleActivity", GlyphKind.PHONE, GlyphCategory.COMMUNICATION, false),
        AppTemplate("Discord", "com.discord", "com.discord.app.AppActivity\$Main", GlyphKind.CHAT_BUBBLE, GlyphCategory.COMMUNICATION, false, 0xFF4F46E5L),
        AppTemplate("Drive", "com.google.android.apps.docs", "com.google.android.apps.docs.app.NewMainProxyActivity", GlyphKind.FOLDER, GlyphCategory.PRODUCTIVITY, false),
        AppTemplate("Files", "com.google.android.documentsui", "com.android.documentsui.files.FilesActivity", GlyphKind.FOLDER, GlyphCategory.SYSTEM, false),
        AppTemplate("Fitness", "com.google.android.apps.fitness", "com.google.android.apps.fitness.welcome.WelcomeActivity", GlyphKind.FITNESS, GlyphCategory.UTILITIES, false, 0xFFE11D48L),
        AppTemplate("Flights", "com.google.android.apps.travel.onthego", "com.google.android.apps.travel.onthego.MainActivity", GlyphKind.FLIGHT, GlyphCategory.TRAVEL, false),
        AppTemplate("GitHub", "com.github.android", "com.github.android.activities.MainActivity", GlyphKind.CODE, GlyphCategory.UTILITIES, true, 0xFF1F2937L),
        AppTemplate("Gmail", "com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail", GlyphKind.MAIL, GlyphCategory.COMMUNICATION, true, 0xFFDC2626L),
        AppTemplate("Headphones", "com.spotify.music", "com.spotify.music.MainActivity", GlyphKind.MUSIC, GlyphCategory.MEDIA, true, 0xFF16A34AL),
        AppTemplate("Instagram", "com.instagram.android", "com.instagram.android.activity.MainTabActivity", GlyphKind.HEART, GlyphCategory.SOCIAL, false, 0xFFDB2777L),
        AppTemplate("Maps", "com.google.android.apps.maps", "com.google.android.maps.MapsActivity", GlyphKind.PIN, GlyphCategory.TRAVEL, true, 0xFF059669L),
        AppTemplate("Messages", "com.google.android.apps.messaging", "com.google.android.apps.messaging.ui.ConversationListActivity", GlyphKind.MESSAGES, GlyphCategory.COMMUNICATION, true, 0xFF0284C7L),
        AppTemplate("Notes", "com.google.android.keep", "com.google.android.keep.activities.BrowseActivity", GlyphKind.NOTES, GlyphCategory.PRODUCTIVITY, true, 0xFFD97706L),
        AppTemplate("Phone", "com.google.android.dialer", "com.google.android.dialer.extensions.GoogleDialtactsActivity", GlyphKind.PHONE, GlyphCategory.COMMUNICATION, true, 0xFF0F766EL),
        AppTemplate("Photos", "com.google.android.apps.photos", "com.google.android.apps.photos.home.HomeActivity", GlyphKind.PHOTOS, GlyphCategory.MEDIA, false),
        AppTemplate("Play Store", "com.android.vending", "com.android.vending.AssetBrowserActivity", GlyphKind.DOWNLOAD, GlyphCategory.SYSTEM, false),
        AppTemplate("Podcasts", "com.pocketcasts.android", "au.com.shiftyjelly.pocketcasts.ui.MainActivity", GlyphKind.PODCAST, GlyphCategory.MEDIA, false),
        AppTemplate("Reader", "com.amazon.kindle", "com.amazon.kindle.UpgradePage", GlyphKind.BOOK, GlyphCategory.UTILITIES, false),
        AppTemplate("Reddit", "com.reddit.frontpage", "com.reddit.frontpage.StartActivity", GlyphKind.COMPASS, GlyphCategory.SOCIAL, false, 0xFFEA580CL),
        AppTemplate("Search", "com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchActivity", GlyphKind.SEARCH, GlyphCategory.UTILITIES, false),
        AppTemplate("Settings", "com.android.settings", "com.android.settings.Settings", GlyphKind.SETTINGS, GlyphCategory.SYSTEM, false),
        AppTemplate("Signal", "org.thoughtcrime.securesms", "org.thoughtcrime.securesms.RoutingActivity", GlyphKind.SIGNAL_WAVE, GlyphCategory.COMMUNICATION, false, 0xFF2563EBL),
        AppTemplate("Tasks", "com.todoist", "com.todoist.activity.HomeActivity", GlyphKind.CHECKLIST, GlyphCategory.PRODUCTIVITY, false),
        AppTemplate("Terminal", "com.termux", "com.termux.app.TermuxActivity", GlyphKind.TERMINAL, GlyphCategory.UTILITIES, false),
        AppTemplate("Vault", "com.bitwarden.authenticator", "com.x8bit.bitwarden.MainActivity", GlyphKind.LOCK, GlyphCategory.SYSTEM, false),
        AppTemplate("Wallet", "com.google.android.apps.walletnfcrel", "com.google.commerce.tapandpay.android.cardlist.CardListActivity", GlyphKind.WALLET, GlyphCategory.SYSTEM, false),
        AppTemplate("Weather", "com.google.android.apps.weather", "com.google.android.apps.weather.WeatherActivity", GlyphKind.SUN_WEATHER, GlyphCategory.TRAVEL, false, 0xFF0EA5E9L),
        AppTemplate("YouTube", "com.google.android.youtube", "com.google.android.youtube.app.honeycomb.Shell\$HomeActivity", GlyphKind.PLAY, GlyphCategory.MEDIA, false, 0xFFE11D48L)
    )
}
