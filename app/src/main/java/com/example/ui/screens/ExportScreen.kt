package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GlyphKind
import com.example.data.model.IconPackUiModel
import com.example.export.ExportBundleFormat
import com.example.export.IconPackExporter
import com.example.export.LauncherIntegrationManager
import com.example.export.LauncherTarget
import com.example.rendering.NiagaraStyleIcon

@Composable
fun ExportScreen(
    activePack: IconPackUiModel,
    launchers: List<LauncherTarget>,
    isExporting: Boolean,
    onRefreshLaunchers: () -> Unit,
    onSaveZipToFiles: (ExportBundleFormat) -> Unit,
    onShareZip: (ExportBundleFormat) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAppFilterPreview by remember { mutableStateOf(false) }
    val includedCount = remember(activePack.icons) { activePack.icons.count { it.isIncluded } }
    val appFilterSample = remember(activePack) { IconPackExporter.buildAppFilterXml(activePack) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Launcher Export", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Bake high-resolution 192×192 PNG icons and standard appfilter.xml bundles for your home screen.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isExporting) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // Pack Export Summary Hero Card
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
                            NiagaraStyleIcon(GlyphKind.PHONE, activePack.style, size = 44.dp)
                            NiagaraStyleIcon(GlyphKind.MUSIC, activePack.style, size = 44.dp)
                            NiagaraStyleIcon(GlyphKind.GLOBE, activePack.style, size = 44.dp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activePack.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "$includedCount PNG icons • 192×192px • ${activePack.style.presetStyle.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Option 1: Full Icon Pack Bundle (.zip)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Full Icon Pack Archive (.zip)", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Includes res/drawable-nodpi/*.png, appfilter.xml, drawable.xml & iconpack.xml",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { onSaveZipToFiles(ExportBundleFormat.FULL_ICON_PACK_ZIP) },
                                enabled = !isExporting && includedCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_full_pack_zip_button")
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save ZIP")
                            }
                            FilledTonalButton(
                                onClick = { onShareZip(ExportBundleFormat.FULL_ICON_PACK_ZIP) },
                                enabled = !isExporting && includedCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_full_pack_zip_button")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share ZIP")
                            }
                        }
                    }
                }
            }

            // Option 2: Clean PNG Set (.zip) for Niagara / Nova / Lawnchair Icon Picker
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Clean PNG Set (.zip)", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Transparent icons/*.png ready for Niagara, Nova, Lawnchair & Smart Launcher per-app picker",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onSaveZipToFiles(ExportBundleFormat.PNG_ICONS_ZIP) },
                                enabled = !isExporting && includedCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_png_zip_button")
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Save PNGs")
                            }
                            FilledTonalButton(
                                onClick = { onShareZip(ExportBundleFormat.PNG_ICONS_ZIP) },
                                enabled = !isExporting && includedCount > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_png_zip_button")
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share PNGs")
                            }
                        }
                    }
                }
            }

            // Expandable appfilter.xml Inspector
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAppFilterPreview = !showAppFilterPreview }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Inspect appfilter.xml", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "$includedCount ComponentInfo bindings generated",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (showAppFilterPreview) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = "Toggle appfilter preview"
                            )
                        }
                        AnimatedVisibility(visible = showAppFilterPreview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = appFilterSample.lines().take(24).joinToString("\n"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            // Supported Third-Party Launchers Directory
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Major Third-Party Launchers",
                        subtitle = "Detected launchers & apply instructions"
                    )
                    IconButton(
                        onClick = onRefreshLaunchers,
                        modifier = Modifier.testTag("refresh_launchers_button")
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh installed launchers")
                    }
                }
            }

            items(launchers, key = { it.id }) { launcher ->
                LauncherTargetCard(
                    launcher = launcher,
                    onOpenOrInstall = {
                        val pkg = launcher.installedPackage
                        if (pkg != null) {
                            val opened = LauncherIntegrationManager.openLauncher(context, pkg)
                            if (!opened) onShowMessage("Could not launch ${launcher.name}")
                        } else {
                            LauncherIntegrationManager.openPlayStoreListing(context, launcher.primaryStorePackage)
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LauncherTargetCard(
    launcher: LauncherTarget,
    onOpenOrInstall: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(launcher.name, style = MaterialTheme.typography.titleMedium)
                        if (launcher.isInstalled) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        "Installed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        launcher.supportTag,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                FilledTonalButton(
                    onClick = onOpenOrInstall,
                    modifier = Modifier.testTag("launcher_action_${launcher.id}")
                ) {
                    Text(if (launcher.isInstalled) "Open" else "Get App")
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(15.dp))
                }
            }

            Text(
                text = launcher.applySteps,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
