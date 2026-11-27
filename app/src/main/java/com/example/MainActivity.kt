package com.example

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.PackIconSpec
import com.example.export.ExportBundleFormat
import com.example.export.IconPackExporter
import com.example.export.LauncherIntegrationManager
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.IconsScreen
import com.example.ui.screens.PreviewScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.FjordIconsTheme
import com.example.ui.viewmodel.IconStudioViewModel
import com.example.ui.viewmodel.StudioEvent
import com.example.ui.viewmodel.StudioUiState
import kotlinx.coroutines.launch

const val ROUTE_STUDIO = "studio"
const val ROUTE_ICONS = "icons"
const val ROUTE_PREVIEW = "preview"
const val ROUTE_EXPORT = "export"

private data class StudioNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val studioTabs = listOf(
    StudioNavTab(ROUTE_STUDIO, "Studio", Icons.Rounded.Palette, Icons.Outlined.Palette, "nav_tab_studio"),
    StudioNavTab(ROUTE_ICONS, "Icons", Icons.Rounded.Apps, Icons.Outlined.Apps, "nav_tab_icons"),
    StudioNavTab(ROUTE_PREVIEW, "Preview", Icons.Rounded.Smartphone, Icons.Outlined.Smartphone, "nav_tab_preview"),
    StudioNavTab(ROUTE_EXPORT, "Export", Icons.Rounded.FileUpload, Icons.Outlined.FileUpload, "nav_tab_export")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FjordIconsTheme {
                FjordIconsApp()
            }
        }
    }
}

@Composable
fun FjordIconsApp(
    viewModel: IconStudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_STUDIO

    var pendingZipFormat by remember { mutableStateOf<ExportBundleFormat?>(null) }
    var pendingSinglePngIcon by remember { mutableStateOf<PackIconSpec?>(null) }

    val saveZipLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        val fmt = pendingZipFormat
        pendingZipFormat = null
        if (uri != null && fmt != null) {
            viewModel.saveZipToUserUri(fmt, uri)
        }
    }

    val savePngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        val icon = pendingSinglePngIcon
        pendingSinglePngIcon = null
        if (uri != null && icon != null) {
            viewModel.saveSingleIconPngToUri(icon, uri)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StudioEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.text)
                }
                is StudioEvent.ShareFile -> {
                    LauncherIntegrationManager.shareFile(
                        context = context,
                        uri = event.uri,
                        mimeType = event.mimeType,
                        title = event.title
                    )
                }
            }
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        studioTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        Spacer(Modifier.height(12.dp))
                        studioTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                when (val state = uiState) {
                    StudioUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Preparing Fjord Studio...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    is StudioUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is StudioUiState.Ready -> {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_STUDIO,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(ROUTE_STUDIO) {
                                StudioScreen(
                                    packs = state.packs,
                                    activePack = state.activePack,
                                    onSelectPack = viewModel::selectPack,
                                    onCreatePack = viewModel::createPack,
                                    onRenamePack = viewModel::renameActivePack,
                                    onDuplicatePack = viewModel::duplicateActivePack,
                                    onDeletePack = viewModel::deleteActivePack,
                                    onApplyPreset = viewModel::applyPresetStyle,
                                    onUpdateShape = viewModel::updateShape,
                                    onUpdateGlyphMode = viewModel::updateGlyphMode,
                                    onApplyPalette = viewModel::applyPalette,
                                    onUpdateBgColor = viewModel::updateBackgroundColor,
                                    onUpdateAccentColor = viewModel::updateAccentColor,
                                    onUpdateGlyphColor = viewModel::updateGlyphColor,
                                    onUpdateBgAlpha = viewModel::updateBackgroundAlpha,
                                    onUpdateIconScale = viewModel::updateIconScale,
                                    onUpdateStrokeWidth = viewModel::updateStrokeWidth,
                                    onToggleDotAccent = viewModel::toggleDotAccent,
                                    onOpenFullPreview = { navigateToTab(ROUTE_PREVIEW) },
                                    onOpenExport = { navigateToTab(ROUTE_EXPORT) }
                                )
                            }

                            composable(ROUTE_ICONS) {
                                IconsScreen(
                                    activePack = state.activePack,
                                    onAddCustomApp = viewModel::addCustomApp,
                                    onUpdateIcon = viewModel::updateIcon,
                                    onDeleteIcon = viewModel::deleteIcon,
                                    onShareSinglePng = viewModel::shareSingleIconPng,
                                    onSaveSinglePng = { icon ->
                                        pendingSinglePngIcon = icon
                                        savePngLauncher.launch("${icon.drawableName}.png")
                                    }
                                )
                            }

                            composable(ROUTE_PREVIEW) {
                                PreviewScreen(
                                    activePack = state.activePack,
                                    onUpdateWallpaper = viewModel::updateWallpaper
                                )
                            }

                            composable(ROUTE_EXPORT) {
                                ExportScreen(
                                    activePack = state.activePack,
                                    launchers = state.launchers,
                                    isExporting = state.isExporting,
                                    onRefreshLaunchers = viewModel::refreshLaunchers,
                                    onSaveZipToFiles = { format ->
                                        pendingZipFormat = format
                                        val suggested = IconPackExporter.suggestedZipFileName(state.activePack, format)
                                        saveZipLauncher.launch(suggested)
                                    },
                                    onShareZip = viewModel::shareZipExport,
                                    onShowMessage = { msg ->
                                        scope.launch { snackbarHostState.showSnackbar(msg) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
