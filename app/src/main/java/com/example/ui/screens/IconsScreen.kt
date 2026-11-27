package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.GlyphCategory
import com.example.data.model.GlyphKind
import com.example.data.model.IconPackUiModel
import com.example.data.model.PackIconSpec
import com.example.rendering.NiagaraStyleIcon

@Composable
fun IconsScreen(
    activePack: IconPackUiModel,
    onAddCustomApp: (appName: String, packageName: String, activityName: String, glyph: GlyphKind, isFavorite: Boolean) -> Unit,
    onUpdateIcon: (PackIconSpec) -> Unit,
    onDeleteIcon: (Long) -> Unit,
    onShareSinglePng: (PackIconSpec) -> Unit,
    onSaveSinglePng: (PackIconSpec) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "FAV", or GlyphCategory.name
    var editingIcon by remember { mutableStateOf<PackIconSpec?>(null) }
    var showAddAppDialog by remember { mutableStateOf(false) }

    val filteredIcons = remember(activePack.icons, searchQuery, selectedFilter) {
        activePack.icons.filter { icon ->
            val matchesQuery = searchQuery.isBlank() ||
                icon.appName.contains(searchQuery, ignoreCase = true) ||
                icon.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "FAV" -> icon.isFavoriteInPreview
                else -> icon.category.name == selectedFilter
            }
            matchesQuery && matchesFilter
        }
    }

    editingIcon?.let { target ->
        val freshIcon = activePack.icons.firstOrNull { it.id == target.id } ?: target
        IconEditorSheet(
            icon = freshIcon,
            packStyle = activePack.style,
            onDismiss = { editingIcon = null },
            onSaveIcon = onUpdateIcon,
            onDeleteIcon = onDeleteIcon,
            onSharePng = onShareSinglePng,
            onSavePngToFiles = onSaveSinglePng
        )
    }

    if (showAddAppDialog) {
        AddCustomAppDialog(
            previewStyle = activePack.style,
            onDismiss = { showAddAppDialog = false },
            onAdd = { name, pkg, act, glyph, fav ->
                showAddAppDialog = false
                onAddCustomApp(name, pkg, act, glyph, fav)
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Icon Library", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            "${activePack.icons.count { it.isIncluded }} of ${activePack.icons.size} icons enabled for export",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search apps or packages...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("icons_search_input")
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All (${activePack.icons.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "FAV",
                        onClick = { selectedFilter = "FAV" },
                        label = { Text("Favorites") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    GlyphCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedFilter == cat.name,
                            onClick = { selectedFilter = cat.name },
                            label = { Text(cat.title) }
                        )
                    }
                }
            }

            if (filteredIcons.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No matching apps", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Tap + to add a custom app mapping and assign a Niagara-style glyph.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 112.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredIcons, key = { it.id }) { icon ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (icon.isIncluded) 1f else 0.45f)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { editingIcon = icon }
                                .testTag("icon_card_${icon.drawableName}")
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                IconButton(
                                    onClick = {
                                        onUpdateIcon(icon.copy(isFavoriteInPreview = !icon.isFavoriteInPreview))
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (icon.isFavoriteInPreview) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                        contentDescription = if (icon.isFavoriteInPreview) "Remove favorite" else "Add favorite",
                                        tint = if (icon.isFavoriteInPreview) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 18.dp, bottom = 14.dp, start = 10.dp, end = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    NiagaraStyleIcon(
                                        glyphKind = icon.glyphKind,
                                        style = activePack.style,
                                        size = 56.dp,
                                        customTintArgb = icon.customTintArgb,
                                        customDotArgb = icon.customDotColorArgb,
                                        contentDescription = "${icon.appName} icon"
                                    )
                                    Text(
                                        text = icon.appName,
                                        style = MaterialTheme.typography.titleSmall,
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

        FloatingActionButton(
            onClick = { showAddAppDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_custom_app_fab")
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Add custom app icon")
        }
    }
}
