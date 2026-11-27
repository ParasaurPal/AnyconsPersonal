package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
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
import com.example.export.LauncherIntegrationManager
import com.example.rendering.NiagaraIconRenderer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.util.zip.ZipFile

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Fjord Icons`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Fjord Icons", appName)
    }

    @Test
    fun `renders crisp bitmap and exports valid icon pack zip`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
        val icon = PackIconSpec(
            id = 1L,
            packId = 1L,
            appName = "Phone",
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
        val pack = IconPackUiModel(
            id = 1L,
            name = "Fjord Dots",
            author = "Studio",
            style = style,
            wallpaperId = "alpine",
            updatedAt = 0L,
            icons = listOf(icon)
        )

        val bmp = NiagaraIconRenderer.renderToBitmap(icon, style, 192)
        assertEquals(192, bmp.width)
        assertEquals(192, bmp.height)
        bmp.recycle()

        val appFilter = IconPackExporter.buildAppFilterXml(pack)
        assertTrue(appFilter.contains("ComponentInfo{com.google.android.dialer/com.google.android.dialer.extensions.GoogleDialtactsActivity}"))
        assertTrue(appFilter.contains("drawable=\"ic_phone\""))

        val result = IconPackExporter.createShareableZip(context, pack, ExportBundleFormat.FULL_ICON_PACK_ZIP)
        assertEquals(1, result.iconCount)
        val zipFile = File(File(context.filesDir, "exports"), result.fileName)
        assertTrue(zipFile.exists())
        ZipFile(zipFile).use { zip ->
            assertTrue(zip.getEntry("res/drawable-nodpi/ic_phone.png") != null)
            assertTrue(zip.getEntry("res/xml/appfilter.xml") != null)
            assertTrue(zip.getEntry("res/xml/drawable.xml") != null)
            assertTrue(zip.getEntry("res/xml/iconpack.xml") != null)
        }
    }

    @Test
    fun `detects supported third-party launchers catalog`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val launchers = LauncherIntegrationManager.detectLaunchers(context)
        assertTrue(launchers.any { it.id == "niagara" })
        assertTrue(launchers.any { it.id == "nova" })
        assertTrue(launchers.any { it.id == "lawnchair" })
    }
}
