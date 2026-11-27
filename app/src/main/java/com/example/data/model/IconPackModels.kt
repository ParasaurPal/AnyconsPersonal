package com.example.data.model

enum class IconPresetStyle(
    val title: String,
    val subtitle: String,
    val defaultShape: IconShape,
    val defaultGlyphMode: GlyphMode,
    val defaultBgAlpha: Float,
    val defaultDotAccent: Boolean
) {
    NIAGARA_DOTS(
        title = "Niagara Dots",
        subtitle = "Minimalist tinted dot with crisp symbol",
        defaultShape = IconShape.CIRCLE,
        defaultGlyphMode = GlyphMode.DOT_ACCENT,
        defaultBgAlpha = 0.92f,
        defaultDotAccent = true
    ),
    ANYCON_DUOTONE(
        title = "Anycon Duotone",
        subtitle = "Soft squircle & layered dual-tone glyph",
        defaultShape = IconShape.SQUIRCLE,
        defaultGlyphMode = GlyphMode.DUOTONE,
        defaultBgAlpha = 0.88f,
        defaultDotAccent = false
    ),
    NORDIC_LINE(
        title = "Nordic Line",
        subtitle = "Precision hairline wireframe, minimal fill",
        defaultShape = IconShape.ROUNDED_SQUARE,
        defaultGlyphMode = GlyphMode.OUTLINED,
        defaultBgAlpha = 0.14f,
        defaultDotAccent = false
    ),
    PEBBLE_GLASS(
        title = "Pebble Glass",
        subtitle = "Frosted translucent pebble & specular rim",
        defaultShape = IconShape.PEBBLE,
        defaultGlyphMode = GlyphMode.FILLED,
        defaultBgAlpha = 0.42f,
        defaultDotAccent = true
    ),
    TACTILE_CLAY(
        title = "Tactile Clay",
        subtitle = "Matte elevated pill with soft depth",
        defaultShape = IconShape.PILL,
        defaultGlyphMode = GlyphMode.FILLED,
        defaultBgAlpha = 1.0f,
        defaultDotAccent = false
    )
}

enum class IconShape(val title: String) {
    CIRCLE("Dot"),
    SQUIRCLE("Squircle"),
    ROUNDED_SQUARE("Soft Box"),
    PEBBLE("Pebble"),
    PILL("Pill"),
    FRAMELESS("Glyph")
}

enum class GlyphMode(val title: String) {
    FILLED("Solid"),
    OUTLINED("Hairline"),
    DUOTONE("Duotone"),
    DOT_ACCENT("Dot + Glyph")
}

enum class GlyphCategory(val title: String) {
    COMMUNICATION("Connect"),
    MEDIA("Media"),
    PRODUCTIVITY("Work"),
    UTILITIES("Tools"),
    SOCIAL("Social"),
    TRAVEL("Travel"),
    SYSTEM("System")
}

enum class GlyphKind(val label: String, val category: GlyphCategory) {
    PHONE("Phone", GlyphCategory.COMMUNICATION),
    MESSAGES("Messages", GlyphCategory.COMMUNICATION),
    MAIL("Mail", GlyphCategory.COMMUNICATION),
    CHAT_BUBBLE("Chat", GlyphCategory.COMMUNICATION),
    VIDEO_CALL("Video", GlyphCategory.COMMUNICATION),
    SIGNAL_WAVE("Signal", GlyphCategory.COMMUNICATION),

    CAMERA("Camera", GlyphCategory.MEDIA),
    PHOTOS("Photos", GlyphCategory.MEDIA),
    MUSIC("Music", GlyphCategory.MEDIA),
    PLAY("Play", GlyphCategory.MEDIA),
    HEADPHONES("Audio", GlyphCategory.MEDIA),
    PODCAST("Podcast", GlyphCategory.MEDIA),
    TV("Stream", GlyphCategory.MEDIA),

    CALENDAR("Calendar", GlyphCategory.PRODUCTIVITY),
    CLOCK("Clock", GlyphCategory.PRODUCTIVITY),
    NOTES("Notes", GlyphCategory.PRODUCTIVITY),
    CHECKLIST("Tasks", GlyphCategory.PRODUCTIVITY),
    FOLDER("Files", GlyphCategory.PRODUCTIVITY),
    DOCUMENT("Docs", GlyphCategory.PRODUCTIVITY),
    CALCULATOR("Calc", GlyphCategory.PRODUCTIVITY),
    TERMINAL("Terminal", GlyphCategory.PRODUCTIVITY),

    GLOBE("Browser", GlyphCategory.SOCIAL),
    COMPASS("Explore", GlyphCategory.SOCIAL),
    SHARE("Network", GlyphCategory.SOCIAL),
    HEART("Heart", GlyphCategory.SOCIAL),
    STAR("Star", GlyphCategory.SOCIAL),
    SPARKLE("Spark", GlyphCategory.SOCIAL),
    BOOKMARK("Save", GlyphCategory.SOCIAL),

    PIN("Maps", GlyphCategory.TRAVEL),
    NAVIGATION("Transit", GlyphCategory.TRAVEL),
    CAR("Drive", GlyphCategory.TRAVEL),
    FLIGHT("Flight", GlyphCategory.TRAVEL),
    SUN_WEATHER("Weather", GlyphCategory.TRAVEL),

    SETTINGS("Settings", GlyphCategory.SYSTEM),
    LOCK("Vault", GlyphCategory.SYSTEM),
    WALLET("Wallet", GlyphCategory.SYSTEM),
    FLASHLIGHT("Torch", GlyphCategory.SYSTEM),
    DOWNLOAD("Store", GlyphCategory.SYSTEM),
    SEARCH("Search", GlyphCategory.UTILITIES),
    CODE("Code", GlyphCategory.UTILITIES),
    FITNESS("Health", GlyphCategory.UTILITIES),
    BOOK("Reader", GlyphCategory.UTILITIES)
}

data class ColorPalettePreset(
    val id: String,
    val name: String,
    val backgroundArgb: Long,
    val accentArgb: Long,
    val glyphArgb: Long
)

data class WallpaperPreset(
    val id: String,
    val name: String,
    val isDark: Boolean
)

data class IconPackStyle(
    val presetStyle: IconPresetStyle,
    val shape: IconShape,
    val glyphMode: GlyphMode,
    val paletteId: String,
    val backgroundArgb: Long,
    val accentArgb: Long,
    val glyphArgb: Long,
    val backgroundAlpha: Float,
    val iconScale: Float,
    val strokeWidthDp: Float,
    val dotAccentEnabled: Boolean
)

data class PackIconSpec(
    val id: Long,
    val packId: Long,
    val appName: String,
    val packageName: String,
    val activityName: String,
    val glyphKind: GlyphKind,
    val category: GlyphCategory,
    val customTintArgb: Long?,
    val customDotColorArgb: Long?,
    val isFavoriteInPreview: Boolean,
    val isIncluded: Boolean,
    val sortOrder: Int
) {
    val drawableName: String
        get() = "ic_" + appName.lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifEmpty { "app_${id}" }

    val componentInfo: String
        get() {
            val act = activityName.ifBlank { "$packageName.MainActivity" }
            return "ComponentInfo{$packageName/$act}"
        }
}

data class IconPackUiModel(
    val id: Long,
    val name: String,
    val author: String,
    val style: IconPackStyle,
    val wallpaperId: String,
    val updatedAt: Long,
    val icons: List<PackIconSpec>
)
