package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.GlyphKind
import com.example.data.model.IconPackStyle
import com.example.data.model.PackIconSpec
import com.example.rendering.NiagaraStyleIcon

private val iconTintSwatches = listOf(
    null to "Pack Default",
    0xFF0F766EL to "Teal",
    0xFF2563EBL to "Cobalt",
    0xFF4F46E5L to "Indigo",
    0xFF7C3AEDL to "Violet",
    0xFFDB2777L to "Berry",
    0xFFDC2626L to "Crimson",
    0xFFD97706L to "Amber",
    0xFF16A34AL to "Emerald",
    0xFF334155L to "Slate",
    0xFF18181BL to "Obsidian"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun IconEditorSheet(
    icon: PackIconSpec,
    packStyle: IconPackStyle,
    onDismiss: () -> Unit,
    onSaveIcon: (PackIconSpec) -> Unit,
    onDeleteIcon: (Long) -> Unit,
    onSharePng: (PackIconSpec) -> Unit,
    onSavePngToFiles: (PackIconSpec) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var appName by remember(icon.id) { mutableStateOf(icon.appName) }
    var packageName by remember(icon.id) { mutableStateOf(icon.packageName) }
    var glyphKind by remember(icon.id) { mutableStateOf(icon.glyphKind) }
    var customTint by remember(icon.id) { mutableStateOf(icon.customTintArgb) }
    var customDot by remember(icon.id) { mutableStateOf(icon.customDotColorArgb) }
    var isFavorite by remember(icon.id) { mutableStateOf(icon.isFavoriteInPreview) }
    var isIncluded by remember(icon.id) { mutableStateOf(icon.isIncluded) }

    val draftIcon = icon.copy(
        appName = appName.ifBlank { icon.appName },
        packageName = packageName.ifBlank { icon.packageName },
        glyphKind = glyphKind,
        customTintArgb = customTint,
        customDotColorArgb = customDot,
        isFavoriteInPreview = isFavorite,
        isIncluded = isIncluded
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Large Preview Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NiagaraStyleIcon(
                    glyphKind = draftIcon.glyphKind,
                    style = packStyle,
                    size = 84.dp,
                    customTintArgb = draftIcon.customTintArgb,
                    customDotArgb = draftIcon.customDotColorArgb,
                    contentDescription = "Large icon preview for ${draftIcon.appName}"
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = draftIcon.appName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = draftIcon.componentInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = {
                                onSaveIcon(draftIcon)
                                onSharePng(draftIcon)
                            },
                            modifier = Modifier.testTag("sheet_share_png_button")
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share PNG")
                        }
                        OutlinedButton(
                            onClick = {
                                onSaveIcon(draftIcon)
                                onSavePngToFiles(draftIcon)
                            },
                            modifier = Modifier.testTag("sheet_save_png_button")
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save")
                        }
                    }
                }
            }

            // App metadata fields
            OutlinedTextField(
                value = appName,
                onValueChange = { appName = it },
                label = { Text("App Label") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = packageName,
                onValueChange = { packageName = it },
                label = { Text("Target Package Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Niagara Home Favorite", style = MaterialTheme.typography.bodyLarge)
                }
                Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Include in Launcher Export", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isIncluded, onCheckedChange = { isIncluded = it })
            }

            // Per-icon container color override
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom Background Tint", style = MaterialTheme.typography.titleSmall)
                if (customTint != null || customDot != null) {
                    TextButton(onClick = {
                        customTint = null
                        customDot = null
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                iconTintSwatches.forEach { (argb, label) ->
                    val isSelected = customTint == argb
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                argb?.let { Color(it.toInt()) }
                                    ?: Color(packStyle.backgroundArgb.toInt())
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clickable { customTint = argb },
                        contentAlignment = Alignment.Center
                    ) {
                        if (argb == null) {
                            Text(
                                text = "Auto",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(packStyle.glyphArgb.toInt())
                            )
                        }
                    }
                }
            }

            // Glyph Picker
            Text("Symbol Glyph", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlyphKind.entries.forEach { candidate ->
                    val selected = candidate == glyphKind
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                            .border(
                                width = if (selected) 2.dp else 0.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { glyphKind = candidate },
                        contentAlignment = Alignment.Center
                    ) {
                        NiagaraStyleIcon(
                            glyphKind = candidate,
                            style = packStyle,
                            size = 34.dp,
                            customTintArgb = customTint,
                            customDotArgb = customDot,
                            contentDescription = candidate.label
                        )
                    }
                }
            }

            // Save & Delete Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        onDeleteIcon(icon.id)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("delete_icon_button")
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = "Delete icon",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        onSaveIcon(draftIcon)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("save_icon_changes_button")
                ) {
                    Text("Apply Changes")
                }
            }
        }
    }
}
