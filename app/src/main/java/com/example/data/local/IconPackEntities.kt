package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "icon_packs")
data class IconPackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val author: String,
    val presetStyle: String,
    val shape: String,
    val glyphMode: String,
    val paletteId: String,
    val backgroundArgb: Long,
    val accentArgb: Long,
    val glyphArgb: Long,
    val backgroundAlpha: Float,
    val iconScale: Float,
    val strokeWidthDp: Float,
    val dotAccentEnabled: Boolean,
    val wallpaperId: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "pack_icons",
    foreignKeys = [
        ForeignKey(
            entity = IconPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["packId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["packId"])]
)
data class PackIconItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packId: Long,
    val appName: String,
    val packageName: String,
    val activityName: String,
    val glyphKey: String,
    val category: String,
    val customTintArgb: Long? = null,
    val customDotColorArgb: Long? = null,
    val isFavoriteInPreview: Boolean = false,
    val isIncluded: Boolean = true,
    val sortOrder: Int = 0
)

data class IconPackWithIcons(
    @Embedded val pack: IconPackEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "packId"
    )
    val icons: List<PackIconItemEntity>
)
