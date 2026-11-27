package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.ColorPalettePreset
import com.example.data.model.DefaultCatalog
import com.example.data.model.GlyphKind
import com.example.data.model.GlyphMode
import com.example.data.model.IconPackUiModel
import com.example.data.model.IconPresetStyle
import com.example.data.model.IconShape
import com.example.rendering.NiagaraStyleIcon
import kotlin.math.roundToInt

private val customColorChoices = listOf(
    0xFF1A2B3CL, 0xFF0F172AL, 0xFF1C2B24L, 0xFF33201AL,
    0xFF282036L, 0xFF18181BL, 0xFFE2ECE9L, 0xFFF5F3FFL,
    0xFF5EEAD4L, 0xFFA5B4FCL, 0xFF86EFACL, 0xFFFDBA74L,
    0xFFD8B4FEL, 0xFFFB7185L, 0xFF38BDF8L, 0xFFF0FDFAL
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    packs: List<IconPackUiModel>,
    activePack: IconPackUiModel,
    onSelectPack: (Long) -> Unit,
    onCreatePack: (String, IconPresetStyle) -> Unit,
    onRenamePack: (String) -> Unit,
    onDuplicatePack: (String) -> Unit,
    onDeletePack: () -> Unit,
    onApplyPreset: (IconPresetStyle) -> Unit,
    onUpdateShape: (IconShape) -> Unit,
    onUpdateGlyphMode: (GlyphMode) -> Unit,
    onApplyPalette: (ColorPalettePreset) -> Unit,
    onUpdateBgColor: (Long) -> Unit,
    onUpdateAccentColor: (Long) -> Unit,
    onUpdateGlyphColor: (Long) -> Unit,
    onUpdateBgAlpha: (Float) -> Unit,
    onUpdateIconScale: (Float) -> Unit,
    onUpdateStrokeWidth: (Float) -> Unit,
    onToggleDotAccent: (Boolean) -> Unit,
    onOpenFullPreview: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPackSelector by remember { mutableStateOf(false) }
    var showPackActions by remember { mutableStateOf(false) }
    var showNewPackDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var activeColorTarget by remember { mutableStateOf(0) } // 0=Bg, 1=Accent, 2=Glyph

    if (showNewPackDialog) {
        NewPackDialog(
            onDismiss = { showNewPackDialog = false },
            onCreate = { name, preset ->
                showNewPackDialog = false
                onCreatePack(name, preset)
            }
        )
    }

    if (showRenameDialog) {
        RenamePackDialog(
            initialName = activePack.name,
            title = "Rename Icon Pack",
            confirmLabel = "Rename",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName ->
                showRenameDialog = false
                onRenamePack(newName)
            }
        )
    }

    if (showDuplicateDialog) {
        RenamePackDialog(
            initialName = "${activePack.name} Copy",
            title = "Duplicate Icon Pack",
            confirmLabel = "Duplicate",
            onDismiss = { showDuplicateDialog = false },
            onConfirm = { newName ->
                showDuplicateDialog = false
                onDuplicatePack(newName)
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header: Pack Switcher + Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showPackSelector = true }
                                .testTag("pack_selector_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ACTIVE PACK",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = activePack.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Rounded.ExpandMore, contentDescription = "Switch icon pack")
                            }
                        }

                        DropdownMenu(
                            expanded = showPackSelector,
                            onDismissRequest = { showPackSelector = false }
                        ) {
                            packs.forEach { pack ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            NiagaraStyleIcon(
                                                glyphKind = GlyphKind.SPARKLE,
                                                style = pack.style,
                                                size = 24.dp
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Column {
                                                Text(pack.name, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    "${pack.style.presetStyle.title} • ${pack.icons.size} icons",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        showPackSelector = false
                                        onSelectPack(pack.id)
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Create New Pack") },
                                leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                                onClick = {
                                    showPackSelector = false
                                    showNewPackDialog = true
                                }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FilledTonalButton(
                            onClick = onOpenExport,
                            modifier = Modifier.testTag("studio_quick_export_button")
                        ) {
                            Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export")
                        }

                        Box {
                            IconButton(
                                onClick = { showPackActions = true },
                                modifier = Modifier.testTag("pack_more_actions_button")
                            ) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "Pack options")
                            }
                            DropdownMenu(
                                expanded = showPackActions,
                                onDismissRequest = { showPackActions = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("New Pack") },
                                    leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                                    onClick = {
                                        showPackActions = false
                                        showNewPackDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rename Pack") },
                                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                                    onClick = {
                                        showPackActions = false
                                        showRenameDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate Pack") },
                                    leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showPackActions = false
                                        showDuplicateDialog = true
                                    }
                                )
                                if (packs.size > 1) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Pack", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Rounded.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showPackActions = false
                                            onDeletePack()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hero: Live Niagara Home Screen Preview Card
            item {
                val previewIcons = remember(activePack.icons) {
                    val favs = activePack.icons.filter { it.isFavoriteInPreview }
                    (if (favs.size >= 4) favs else activePack.icons).take(5)
                }

                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studio_live_preview_card")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        StudioWallpaperBackdrop(wallpaperId = activePack.wallpaperId)

                        // Subtle dark scrim for crisp legibility
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.56f),
                                            Color.Black.copy(alpha = 0.18f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "17:58",
                                        style = MaterialTheme.typography.displaySmall,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Thu, Nov 26  •  ${activePack.style.presetStyle.title}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White.copy(alpha = 0.78f)
                                    )
                                }

                                AssistChip(
                                    onClick = onOpenFullPreview,
                                    label = { Text("Full Preview") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("open_full_preview_chip")
                                )
                            }

                            // Niagara vertical list snippet
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                previewIcons.forEach { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        NiagaraStyleIcon(
                                            glyphKind = item.glyphKind,
                                            style = activePack.style,
                                            size = 34.dp,
                                            customTintArgb = item.customTintArgb,
                                            customDotArgb = item.customDotColorArgb,
                                            contentDescription = "${item.appName} icon"
                                        )
                                        Text(
                                            text = item.appName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Style Presets Carousel
            item {
                SectionHeader(title = "Niagara Aesthetics", subtitle = "Tap a preset to transform all icons")
                Spacer(Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(IconPresetStyle.entries) { preset ->
                        val selected = preset == activePack.style.presetStyle
                        val previewStyle = activePack.style.copy(
                            presetStyle = preset,
                            shape = preset.defaultShape,
                            glyphMode = preset.defaultGlyphMode,
                            backgroundAlpha = preset.defaultBgAlpha,
                            dotAccentEnabled = preset.defaultDotAccent
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .width(152.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { onApplyPreset(preset) }
                                .testTag("preset_${preset.name.lowercase()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    NiagaraStyleIcon(
                                        glyphKind = GlyphKind.MESSAGES,
                                        style = previewStyle,
                                        size = 36.dp
                                    )
                                    NiagaraStyleIcon(
                                        glyphKind = GlyphKind.MUSIC,
                                        style = previewStyle,
                                        size = 36.dp
                                    )
                                }
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                                Text(
                                    text = preset.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Container Shape & Glyph Mode
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SectionHeader(title = "Container & Glyph", subtitle = "Shape geometry and symbol rendering")

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconShape.entries.forEach { shape ->
                                FilterChip(
                                    selected = activePack.style.shape == shape,
                                    onClick = { onUpdateShape(shape) },
                                    label = { Text(shape.title) },
                                    modifier = Modifier.testTag("shape_${shape.name.lowercase()}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlyphMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = activePack.style.glyphMode == mode,
                                    onClick = { onUpdateGlyphMode(mode) },
                                    label = { Text(mode.title) },
                                    modifier = Modifier.testTag("glyph_mode_${mode.name.lowercase()}")
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Niagara Dot Accent", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Signature luminous corner indicator orb",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = activePack.style.dotAccentEnabled,
                                onCheckedChange = onToggleDotAccent,
                                modifier = Modifier.testTag("dot_accent_switch")
                            )
                        }
                    }
                }
            }

            // Curated Color Palettes & Custom Swatches
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            SectionHeader(title = "Color Moods", subtitle = "Curated palettes or custom channel colors")
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(DefaultCatalog.palettes) { palette ->
                                val selected = activePack.style.paletteId == palette.id
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (selected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { onApplyPalette(palette) }
                                        .testTag("palette_${palette.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                            ColorDot(Color(palette.backgroundArgb.toInt()))
                                            ColorDot(Color(palette.accentArgb.toInt()))
                                            ColorDot(Color(palette.glyphArgb.toInt()))
                                        }
                                        Text(palette.name, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        // Channel selector tabs: Background / Accent / Glyph
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Backdrop", "Accent Dot", "Glyph").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = activeColorTarget == idx,
                                    onClick = { activeColorTarget = idx },
                                    label = { Text(label) }
                                )
                            }
                        }

                        val currentTargetColor = when (activeColorTarget) {
                            0 -> activePack.style.backgroundArgb
                            1 -> activePack.style.accentArgb
                            else -> activePack.style.glyphArgb
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            customColorChoices.forEach { argb ->
                                val isSel = currentTargetColor == argb
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(argb.toInt()))
                                        .border(
                                            width = if (isSel) 3.dp else 1.dp,
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            when (activeColorTarget) {
                                                0 -> onUpdateBgColor(argb)
                                                1 -> onUpdateAccentColor(argb)
                                                else -> onUpdateGlyphColor(argb)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }
            }

            // Fine-tuning Geometry Sliders
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            SectionHeader(title = "Precision Tuning", subtitle = "Scale, translucency & line weight")
                        }

                        SliderRow(
                            label = "Glyph Scale",
                            valueText = "${(activePack.style.iconScale * 100).roundToInt()}%",
                            value = activePack.style.iconScale,
                            range = 0.34f..0.78f,
                            onValueChange = onUpdateIconScale
                        )

                        SliderRow(
                            label = "Backdrop Opacity",
                            valueText = "${(activePack.style.backgroundAlpha * 100).roundToInt()}%",
                            value = activePack.style.backgroundAlpha,
                            range = 0.0f..1.0f,
                            onValueChange = onUpdateBgAlpha
                        )

                        SliderRow(
                            label = "Line Weight",
                            valueText = "${String.format("%.1f", activePack.style.strokeWidthDp)} dp",
                            value = activePack.style.strokeWidthDp,
                            range = 1.4f..4.4f,
                            onValueChange = onUpdateStrokeWidth
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
    )
}

@Composable
private fun SliderRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                valueText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StudioWallpaperBackdrop(wallpaperId: String, modifier: Modifier = Modifier) {
    when {
        wallpaperId == "alpine" -> {
            Image(
                painter = painterResource(id = R.drawable.img_wallpaper_alpine),
                contentDescription = "Nordic alpine wallpaper",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }
        wallpaperId == "dusk" -> {
            Image(
                painter = painterResource(id = R.drawable.img_wallpaper_dusk),
                contentDescription = "Warm dusk wallpaper",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }
        wallpaperId == "obsidian" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF090D14), Color(0xFF131C2A), Color(0xFF0B1018))
                        )
                    )
            )
        }
        wallpaperId == "sand" -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF3EFEA), Color(0xFFE5DED4), Color(0xFFD9D1C7))
                        )
                    )
            )
        }
        else -> {
            AsyncImage(
                model = wallpaperId,
                contentDescription = "Custom wallpaper",
                contentScale = ContentScale.Crop,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}
