package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.DefaultCatalog
import com.example.data.model.IconPackUiModel
import com.example.rendering.NiagaraStyleIcon
import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    activePack: IconPackUiModel,
    onUpdateWallpaper: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isNiagaraListMode by remember { mutableStateOf(true) }
    var activeLetter by remember { mutableStateOf<Char?>(null) }
    var scrubberHeightPx by remember { mutableStateOf(1) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onUpdateWallpaper(uri.toString())
        }
    }

    val includedIcons = remember(activePack.icons) {
        activePack.icons.filter { it.isIncluded }.sortedBy { it.appName.lowercase() }
    }

    val letters = remember(includedIcons) {
        includedIcons.mapNotNull { it.appName.firstOrNull()?.uppercaseChar() }.distinct().sorted()
    }

    val isLightBackdrop = activePack.wallpaperId == "sand"
    val primaryTextColor = if (isLightBackdrop) Color(0xFF131922) else Color.White
    val secondaryTextColor = if (isLightBackdrop) Color(0xFF475569) else Color.White.copy(alpha = 0.72f)

    Box(modifier = modifier.fillMaxSize()) {
        // Wallpaper Layer
        StudioWallpaperBackdrop(wallpaperId = activePack.wallpaperId)

        // Atmospheric readability scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isLightBackdrop) {
                        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.35f)))
                    } else {
                        Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Black.copy(alpha = 0.55f)))
                    }
                )
        )

        Column(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
        ) {
            // Top Controls: Wallpaper + Layout Mode Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = isNiagaraListMode,
                    onClick = { isNiagaraListMode = true },
                    label = { Text("Niagara List") },
                    leadingIcon = { Icon(Icons.Rounded.FormatListBulleted, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("preview_niagara_mode_chip")
                )
                FilterChip(
                    selected = !isNiagaraListMode,
                    onClick = { isNiagaraListMode = false },
                    label = { Text("Launcher Grid") },
                    leadingIcon = { Icon(Icons.Rounded.Apps, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("preview_grid_mode_chip")
                )

                DefaultCatalog.wallpapers.forEach { wp ->
                    FilterChip(
                        selected = activePack.wallpaperId == wp.id,
                        onClick = { onUpdateWallpaper(wp.id) },
                        label = { Text(wp.name) }
                    )
                }

                FilterChip(
                    selected = DefaultCatalog.wallpapers.none { it.id == activePack.wallpaperId },
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    label = { Text("My Photo") },
                    leadingIcon = { Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.testTag("pick_custom_wallpaper_chip")
                )
            }

            if (isNiagaraListMode) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = 24.dp, end = 52.dp, top = 12.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Niagara Minimalist Clock & Date Widget
                        item {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = "17:58",
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Light),
                                    color = primaryTextColor
                                )
                                Text(
                                    text = "Thu, Nov 26  •  ${activePack.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        // Pinned Favorites Section
                        val favorites = includedIcons.filter { it.isFavoriteInPreview }
                        if (favorites.isNotEmpty()) {
                            item {
                                Text(
                                    text = "FAVORITES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryTextColor
                                )
                            }
                            items(favorites, key = { "fav_${it.id}" }) { icon ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    NiagaraStyleIcon(
                                        glyphKind = icon.glyphKind,
                                        style = activePack.style,
                                        size = 42.dp,
                                        customTintArgb = icon.customTintArgb,
                                        customDotArgb = icon.customDotColorArgb,
                                        contentDescription = "${icon.appName} icon"
                                    )
                                    Text(
                                        text = icon.appName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = primaryTextColor
                                    )
                                }
                            }
                            item {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "ALL APPS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = secondaryTextColor
                                )
                            }
                        }

                        items(includedIcons, key = { "all_${it.id}" }) { icon ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                NiagaraStyleIcon(
                                    glyphKind = icon.glyphKind,
                                    style = activePack.style,
                                    size = 38.dp,
                                    customTintArgb = icon.customTintArgb,
                                    customDotArgb = icon.customDotColorArgb,
                                    contentDescription = "${icon.appName} icon"
                                )
                                Text(
                                    text = icon.appName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = primaryTextColor
                                )
                            }
                        }
                    }

                    // Niagara A-Z Alphabet Scrubber on the right edge
                    if (letters.isNotEmpty()) {
                        val headerOffset = if (includedIcons.any { it.isFavoriteInPreview }) {
                            3 + includedIcons.count { it.isFavoriteInPreview }
                        } else {
                            1
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp, top = 16.dp, bottom = 16.dp)
                                .fillMaxHeight(0.82f)
                                .width(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black.copy(alpha = 0.22f))
                                .onSizeChanged { scrubberHeightPx = it.height.coerceAtLeast(1) }
                                .pointerInput(letters, includedIcons) {
                                    detectVerticalDragGestures(
                                        onDragEnd = { activeLetter = null },
                                        onDragCancel = { activeLetter = null }
                                    ) { change, _ ->
                                        val fraction = (change.position.y / scrubberHeightPx.toFloat()).coerceIn(0f, 0.999f)
                                        val idx = (fraction * letters.size).toInt().coerceIn(0, letters.lastIndex)
                                        val letter = letters[idx]
                                        activeLetter = letter
                                        val targetIdx = includedIcons.indexOfFirst {
                                            it.appName.firstOrNull()?.uppercaseChar() == letter
                                        }
                                        if (targetIdx >= 0) {
                                            scope.launch {
                                                listState.scrollToItem(headerOffset + targetIdx)
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            letters.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (activeLetter == letter) MaterialTheme.colorScheme.primary else secondaryTextColor,
                                    modifier = Modifier
                                        .clickable {
                                            activeLetter = letter
                                            val targetIdx = includedIcons.indexOfFirst {
                                                it.appName.firstOrNull()?.uppercaseChar() == letter
                                            }
                                            if (targetIdx >= 0) {
                                                scope.launch {
                                                    listState.animateScrollToItem(headerOffset + targetIdx)
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    }

                    // Floating Niagara letter indicator bubble
                    activeLetter?.let { letter ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = letter.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            } else {
                // Classic Nova / Lawnchair 4-column Grid Preview
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(includedIcons, key = { "grid_${it.id}" }) { icon ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NiagaraStyleIcon(
                                glyphKind = icon.glyphKind,
                                style = activePack.style,
                                size = 54.dp,
                                customTintArgb = icon.customTintArgb,
                                customDotArgb = icon.customDotColorArgb,
                                contentDescription = "${icon.appName} icon"
                            )
                            Text(
                                text = icon.appName,
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
