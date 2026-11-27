package com.example

import com.example.data.model.DefaultCatalog
import com.example.data.model.GlyphCategory
import com.example.data.model.GlyphKind
import com.example.data.model.GlyphMode
import com.example.data.model.IconPackStyle
import com.example.data.model.IconPackUiModel
import com.example.data.model.IconPresetStyle
import com.example.data.model.IconShape
import com.example.data.model.PackIconSpec
import com.example.export.ExportBundleFormat
import com.example.export.IconPackExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `appfilter and manifest generation include valid ComponentInfo bindings`() {
        val style = IconPackStyle(
            presetStyle = IconPresetStyle.NIAGARA_DOTS,
            shape = IconShape.CIRCLE,
            glyphMode = GlyphMode.DOT_ACCENT,
            paletteId = "nordic_frost",
            backgroundArgb = 0xFF1A2B3CL,
            accentArgb = 0xFF5EEAD4L,
            glyphArgb = 0xFFF0FDFAL,
            backgroundAlpha = 0.92f,
            iconScale = 0.56f,
            strokeWidthDp = 2.4f,
            dotAccentEnabled = true
        )
        val includedPhone = PackIconSpec(
            id = 1L,
            packId = 1L,
            appName = "Phone & Dialer",
            packageName = "com.google.android.dialer",
            activityName = "com.google.android.dialer.extensions.GoogleDialtactsActivity",
            glyphKind = GlyphKind.PHONE,
            category = GlyphCategory.COMMUNICATION,
            customTintArgb = null,
            customDotColorArgb = null,
            isFavoriteInPreview = true,
            isIncluded = true,
            sortOrder = 0
        )
        val excludedApp = includedPhone.copy(
            id = 2L,
            appName = "Excluded App",
            packageName = "com.example.excluded",
            isIncluded = false
        )
        val pack = IconPackUiModel(
            id = 1L,
            name = "Fjord Dots",
            author = "Studio",
            style = style,
            wallpaperId = "alpine",
            updatedAt = 0L,
            icons = listOf(includedPhone, excludedApp)
        )

        val appFilter = IconPackExporter.buildAppFilterXml(pack)
        assertTrue(appFilter.contains("ComponentInfo{com.google.android.dialer/com.google.android.dialer.extensions.GoogleDialtactsActivity}"))
        assertTrue(appFilter.contains("drawable=\"ic_phone_dialer\""))
        assertTrue(!appFilter.contains("com.example.excluded"))

        val drawableXml = IconPackExporter.buildDrawableXml(pack)
        assertTrue(drawableXml.contains("""<item drawable="ic_phone_dialer" />"""))

        val iconPackXml = IconPackExporter.buildIconPackXml(pack)
        assertTrue(iconPackXml.contains("<item>ic_phone_dialer</item>"))

        val manifestJson = IconPackExporter.buildPackManifestJson(pack)
        assertTrue(manifestJson.contains("\"iconCount\": 1"))
        assertTrue(manifestJson.contains("\"resolutionPx\": 192"))

        assertEquals("fjord_dots_icon_pack.zip", IconPackExporter.suggestedZipFileName(pack, ExportBundleFormat.FULL_ICON_PACK_ZIP))
        assertEquals("fjord_dots_png_icons.zip", IconPackExporter.suggestedZipFileName(pack, ExportBundleFormat.PNG_ICONS_ZIP))
    }

    @Test
    fun `default catalog has unique starter apps and valid palettes`() {
        assertTrue(DefaultCatalog.starterApps.size >= 30)
        assertTrue(DefaultCatalog.palettes.size >= 6)
        val distinctPackages = DefaultCatalog.starterApps.map { it.packageName }.distinct()
        assertEquals(DefaultCatalog.starterApps.size, distinctPackages.size)
    }
}
