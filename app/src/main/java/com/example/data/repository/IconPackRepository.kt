package com.example.data.repository

import com.example.data.local.IconPackDao
import com.example.data.local.IconPackEntity
import com.example.data.local.IconPackWithIcons
import com.example.data.local.PackIconItemEntity
import com.example.data.model.DefaultCatalog
import com.example.data.model.GlyphCategory
import com.example.data.model.GlyphKind
import com.example.data.model.GlyphMode
import com.example.data.model.IconPackStyle
import com.example.data.model.IconPackUiModel
import com.example.data.model.IconPresetStyle
import com.example.data.model.IconShape
import com.example.data.model.PackIconSpec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IconPackRepository(private val dao: IconPackDao) {

    val allPacks: Flow<List<IconPackUiModel>> = dao.observeAllPacks().map { list ->
        list.map { it.toUiModel() }
    }

    suspend fun ensureSeeded() {
        if (dao.countPacks() > 0) return

        val nordicPackId = dao.insertPack(
            IconPackEntity(
                name = "Fjord Dots",
                author = "Studio Preset",
                presetStyle = IconPresetStyle.NIAGARA_DOTS.name,
                shape = IconShape.CIRCLE.name,
                glyphMode = GlyphMode.DOT_ACCENT.name,
                paletteId = "nordic_frost",
                backgroundArgb = 0xFF1A2B3CL,
                accentArgb = 0xFF5EEAD4L,
                glyphArgb = 0xFFF0FDFAL,
                backgroundAlpha = 0.92f,
                iconScale = 0.56f,
                strokeWidthDp = 2.4f,
                dotAccentEnabled = true,
                wallpaperId = "alpine",
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertIcons(buildStarterIcons(nordicPackId, useAppTints = false))

        val duotonePackId = dao.insertPack(
            IconPackEntity(
                name = "Dusk Anycons",
                author = "Studio Preset",
                presetStyle = IconPresetStyle.ANYCON_DUOTONE.name,
                shape = IconShape.SQUIRCLE.name,
                glyphMode = GlyphMode.DUOTONE.name,
                paletteId = "warm_terracotta",
                backgroundArgb = 0xFF33201AL,
                accentArgb = 0xFFFDBA74L,
                glyphArgb = 0xFFFFF7EDL,
                backgroundAlpha = 0.88f,
                iconScale = 0.58f,
                strokeWidthDp = 2.6f,
                dotAccentEnabled = false,
                wallpaperId = "dusk",
                updatedAt = System.currentTimeMillis() - 10_000L
            )
        )
        dao.insertIcons(buildStarterIcons(duotonePackId, useAppTints = true))
    }

    suspend fun createPack(name: String, preset: IconPresetStyle): Long {
        val palette = DefaultCatalog.palettes.first()
        val packId = dao.insertPack(
            IconPackEntity(
                name = name.trim().ifEmpty { "Custom Pack" },
                author = "Custom",
                presetStyle = preset.name,
                shape = preset.defaultShape.name,
                glyphMode = preset.defaultGlyphMode.name,
                paletteId = palette.id,
                backgroundArgb = palette.backgroundArgb,
                accentArgb = palette.accentArgb,
                glyphArgb = palette.glyphArgb,
                backgroundAlpha = preset.defaultBgAlpha,
                iconScale = 0.56f,
                strokeWidthDp = 2.4f,
                dotAccentEnabled = preset.defaultDotAccent,
                wallpaperId = "alpine",
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertIcons(buildStarterIcons(packId, useAppTints = false))
        return packId
    }

    suspend fun duplicatePack(source: IconPackUiModel, newName: String): Long {
        val newPackId = dao.insertPack(
            IconPackEntity(
                name = newName.trim().ifEmpty { "${source.name} Copy" },
                author = source.author,
                presetStyle = source.style.presetStyle.name,
                shape = source.style.shape.name,
                glyphMode = source.style.glyphMode.name,
                paletteId = source.style.paletteId,
                backgroundArgb = source.style.backgroundArgb,
                accentArgb = source.style.accentArgb,
                glyphArgb = source.style.glyphArgb,
                backgroundAlpha = source.style.backgroundAlpha,
                iconScale = source.style.iconScale,
                strokeWidthDp = source.style.strokeWidthDp,
                dotAccentEnabled = source.style.dotAccentEnabled,
                wallpaperId = source.wallpaperId,
                updatedAt = System.currentTimeMillis()
            )
        )
        dao.insertIcons(
            source.icons.map { icon ->
                PackIconItemEntity(
                    packId = newPackId,
                    appName = icon.appName,
                    packageName = icon.packageName,
                    activityName = icon.activityName,
                    glyphKey = icon.glyphKind.name,
                    category = icon.category.name,
                    customTintArgb = icon.customTintArgb,
                    customDotColorArgb = icon.customDotColorArgb,
                    isFavoriteInPreview = icon.isFavoriteInPreview,
                    isIncluded = icon.isIncluded,
                    sortOrder = icon.sortOrder
                )
            }
        )
        return newPackId
    }

    suspend fun updatePack(pack: IconPackUiModel) {
        dao.updatePack(
            IconPackEntity(
                id = pack.id,
                name = pack.name,
                author = pack.author,
                presetStyle = pack.style.presetStyle.name,
                shape = pack.style.shape.name,
                glyphMode = pack.style.glyphMode.name,
                paletteId = pack.style.paletteId,
                backgroundArgb = pack.style.backgroundArgb,
                accentArgb = pack.style.accentArgb,
                glyphArgb = pack.style.glyphArgb,
                backgroundAlpha = pack.style.backgroundAlpha,
                iconScale = pack.style.iconScale,
                strokeWidthDp = pack.style.strokeWidthDp,
                dotAccentEnabled = pack.style.dotAccentEnabled,
                wallpaperId = pack.wallpaperId,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deletePack(packId: Long) {
        dao.deletePackById(packId)
    }

    suspend fun addCustomIcon(
        packId: Long,
        appName: String,
        packageName: String,
        activityName: String,
        glyphKind: GlyphKind,
        isFavorite: Boolean
    ) {
        val cleanPkg = packageName.trim().ifEmpty {
            "com.custom.${appName.lowercase().replace(Regex("[^a-z0-9]"), "")}"
        }
        val cleanAct = activityName.trim().ifEmpty { "$cleanPkg.MainActivity" }
        dao.insertIcon(
            PackIconItemEntity(
                packId = packId,
                appName = appName.trim(),
                packageName = cleanPkg,
                activityName = cleanAct,
                glyphKey = glyphKind.name,
                category = glyphKind.category.name,
                customTintArgb = null,
                customDotColorArgb = null,
                isFavoriteInPreview = isFavorite,
                isIncluded = true,
                sortOrder = 999
            )
        )
    }

    suspend fun updateIcon(icon: PackIconSpec) {
        dao.updateIcon(
            PackIconItemEntity(
                id = icon.id,
                packId = icon.packId,
                appName = icon.appName,
                packageName = icon.packageName,
                activityName = icon.activityName,
                glyphKey = icon.glyphKind.name,
                category = icon.category.name,
                customTintArgb = icon.customTintArgb,
                customDotColorArgb = icon.customDotColorArgb,
                isFavoriteInPreview = icon.isFavoriteInPreview,
                isIncluded = icon.isIncluded,
                sortOrder = icon.sortOrder
            )
        )
    }

    suspend fun deleteIcon(iconId: Long) {
        dao.deleteIconById(iconId)
    }

    private fun buildStarterIcons(packId: Long, useAppTints: Boolean): List<PackIconItemEntity> {
        return DefaultCatalog.starterApps.mapIndexed { index, tpl ->
            PackIconItemEntity(
                packId = packId,
                appName = tpl.appName,
                packageName = tpl.packageName,
                activityName = tpl.activityName,
                glyphKey = tpl.glyphKind.name,
                category = tpl.category.name,
                customTintArgb = if (useAppTints) tpl.tintArgb else null,
                customDotColorArgb = null,
                isFavoriteInPreview = tpl.isFavoriteDefault,
                isIncluded = true,
                sortOrder = index
            )
        }
    }

    private fun IconPackWithIcons.toUiModel(): IconPackUiModel {
        val style = IconPackStyle(
            presetStyle = enumValueOrDefault(pack.presetStyle, IconPresetStyle.NIAGARA_DOTS),
            shape = enumValueOrDefault(pack.shape, IconShape.CIRCLE),
            glyphMode = enumValueOrDefault(pack.glyphMode, GlyphMode.DOT_ACCENT),
            paletteId = pack.paletteId,
            backgroundArgb = pack.backgroundArgb,
            accentArgb = pack.accentArgb,
            glyphArgb = pack.glyphArgb,
            backgroundAlpha = pack.backgroundAlpha,
            iconScale = pack.iconScale,
            strokeWidthDp = pack.strokeWidthDp,
            dotAccentEnabled = pack.dotAccentEnabled
        )
        val sortedIcons = icons
            .map { item ->
                PackIconSpec(
                    id = item.id,
                    packId = item.packId,
                    appName = item.appName,
                    packageName = item.packageName,
                    activityName = item.activityName,
                    glyphKind = enumValueOrDefault(item.glyphKey, GlyphKind.SPARKLE),
                    category = enumValueOrDefault(item.category, GlyphCategory.UTILITIES),
                    customTintArgb = item.customTintArgb,
                    customDotColorArgb = item.customDotColorArgb,
                    isFavoriteInPreview = item.isFavoriteInPreview,
                    isIncluded = item.isIncluded,
                    sortOrder = item.sortOrder
                )
            }
            .sortedBy { it.appName.lowercase() }

        return IconPackUiModel(
            id = pack.id,
            name = pack.name,
            author = pack.author,
            style = style,
            wallpaperId = pack.wallpaperId,
            updatedAt = pack.updatedAt,
            icons = sortedIcons
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T {
        return try {
            enumValueOf<T>(name)
        } catch (_: IllegalArgumentException) {
            default
        }
    }
}
