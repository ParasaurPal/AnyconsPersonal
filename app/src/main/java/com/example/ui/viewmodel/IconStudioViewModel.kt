package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ColorPalettePreset
import com.example.data.model.GlyphCategory
import com.example.data.model.GlyphKind
import com.example.data.model.GlyphMode
import com.example.data.model.IconPackUiModel
import com.example.data.model.IconPresetStyle
import com.example.data.model.IconShape
import com.example.data.model.PackIconSpec
import com.example.data.repository.IconPackRepository
import com.example.export.ExportBundleFormat
import com.example.export.IconPackExporter
import com.example.export.LauncherIntegrationManager
import com.example.export.LauncherTarget
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface StudioUiState {
    data object Loading : StudioUiState
    data class Ready(
        val packs: List<IconPackUiModel>,
        val activePack: IconPackUiModel,
        val launchers: List<LauncherTarget>,
        val isExporting: Boolean = false
    ) : StudioUiState
    data class Error(val message: String) : StudioUiState
}

sealed interface StudioEvent {
    data class ShowMessage(val text: String) : StudioEvent
    data class ShareFile(val uri: Uri, val mimeType: String, val title: String) : StudioEvent
}

class IconStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IconPackRepository by lazy {
        val db = AppDatabase.getInstance(application)
        IconPackRepository(db.iconPackDao())
    }

    private val selectedPackId = MutableStateFlow<Long?>(null)
    private val launchersFlow = MutableStateFlow(LauncherIntegrationManager.detectLaunchers(application))
    private val isExportingFlow = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<StudioUiState>(StudioUiState.Loading)
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StudioEvent>()
    val events: SharedFlow<StudioEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            try {
                repository.ensureSeeded()
            } catch (e: Exception) {
                _uiState.value = StudioUiState.Error(e.message ?: "Failed to initialize icon studio")
                return@launch
            }

            combine(
                repository.allPacks,
                selectedPackId,
                launchersFlow,
                isExportingFlow
            ) { packs, selectedId, launchers, isExporting ->
                if (packs.isEmpty()) {
                    StudioUiState.Loading
                } else {
                    val active = packs.firstOrNull { it.id == selectedId } ?: packs.first()
                    StudioUiState.Ready(
                        packs = packs,
                        activePack = active,
                        launchers = launchers,
                        isExporting = isExporting
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectPack(packId: Long) {
        selectedPackId.value = packId
    }

    fun refreshLaunchers() {
        launchersFlow.value = LauncherIntegrationManager.detectLaunchers(getApplication())
    }

    fun createPack(name: String, preset: IconPresetStyle) {
        viewModelScope.launch {
            val newId = repository.createPack(name, preset)
            selectedPackId.value = newId
            _events.emit(StudioEvent.ShowMessage("Created \"$name\""))
        }
    }

    fun duplicateActivePack(newName: String) {
        val current = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            val newId = repository.duplicatePack(current, newName)
            selectedPackId.value = newId
            _events.emit(StudioEvent.ShowMessage("Duplicated pack as \"$newName\""))
        }
    }

    fun renameActivePack(newName: String) {
        val current = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updatePack(current.copy(name = trimmed))
            _events.emit(StudioEvent.ShowMessage("Renamed pack to \"$trimmed\""))
        }
    }

    fun deleteActivePack() {
        val ready = uiState.value as? StudioUiState.Ready ?: return
        if (ready.packs.size <= 1) {
            viewModelScope.launch {
                _events.emit(StudioEvent.ShowMessage("At least one icon pack must remain in the studio."))
            }
            return
        }
        val toDelete = ready.activePack
        val fallback = ready.packs.first { it.id != toDelete.id }
        viewModelScope.launch {
            selectedPackId.value = fallback.id
            repository.deletePack(toDelete.id)
            _events.emit(StudioEvent.ShowMessage("Deleted \"${toDelete.name}\""))
        }
    }

    fun applyPresetStyle(preset: IconPresetStyle) {
        updateActiveStyle { current ->
            current.copy(
                presetStyle = preset,
                shape = preset.defaultShape,
                glyphMode = preset.defaultGlyphMode,
                backgroundAlpha = preset.defaultBgAlpha,
                dotAccentEnabled = preset.defaultDotAccent
            )
        }
    }

    fun updateShape(shape: IconShape) {
        updateActiveStyle { it.copy(shape = shape) }
    }

    fun updateGlyphMode(mode: GlyphMode) {
        updateActiveStyle { it.copy(glyphMode = mode) }
    }

    fun applyPalette(palette: ColorPalettePreset) {
        updateActiveStyle {
            it.copy(
                paletteId = palette.id,
                backgroundArgb = palette.backgroundArgb,
                accentArgb = palette.accentArgb,
                glyphArgb = palette.glyphArgb
            )
        }
    }

    fun updateBackgroundColor(argb: Long) {
        updateActiveStyle { it.copy(paletteId = "custom", backgroundArgb = argb) }
    }

    fun updateAccentColor(argb: Long) {
        updateActiveStyle { it.copy(paletteId = "custom", accentArgb = argb) }
    }

    fun updateGlyphColor(argb: Long) {
        updateActiveStyle { it.copy(paletteId = "custom", glyphArgb = argb) }
    }

    fun updateBackgroundAlpha(alpha: Float) {
        updateActiveStyle { it.copy(backgroundAlpha = alpha.coerceIn(0f, 1f)) }
    }

    fun updateIconScale(scale: Float) {
        updateActiveStyle { it.copy(iconScale = scale.coerceIn(0.34f, 0.80f)) }
    }

    fun updateStrokeWidth(widthDp: Float) {
        updateActiveStyle { it.copy(strokeWidthDp = widthDp.coerceIn(1.4f, 4.6f)) }
    }

    fun toggleDotAccent(enabled: Boolean) {
        updateActiveStyle { it.copy(dotAccentEnabled = enabled) }
    }

    fun updateWallpaper(wallpaperIdOrUri: String) {
        val current = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            repository.updatePack(current.copy(wallpaperId = wallpaperIdOrUri))
        }
    }

    fun addCustomApp(
        appName: String,
        packageName: String,
        activityName: String,
        glyphKind: GlyphKind,
        isFavorite: Boolean
    ) {
        val current = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        if (appName.isBlank()) return
        viewModelScope.launch {
            repository.addCustomIcon(
                packId = current.id,
                appName = appName,
                packageName = packageName,
                activityName = activityName,
                glyphKind = glyphKind,
                isFavorite = isFavorite
            )
            _events.emit(StudioEvent.ShowMessage("Added \"${appName.trim()}\" to ${current.name}"))
        }
    }

    fun updateIcon(icon: PackIconSpec) {
        viewModelScope.launch {
            repository.updateIcon(icon)
        }
    }

    fun deleteIcon(iconId: Long) {
        viewModelScope.launch {
            repository.deleteIcon(iconId)
            _events.emit(StudioEvent.ShowMessage("Removed icon from pack"))
        }
    }

    fun shareZipExport(format: ExportBundleFormat) {
        val pack = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            isExportingFlow.value = true
            try {
                val result = IconPackExporter.createShareableZip(getApplication(), pack, format)
                result.shareUri?.let { uri ->
                    _events.emit(
                        StudioEvent.ShareFile(
                            uri = uri,
                            mimeType = "application/zip",
                            title = "Export ${pack.name} (${result.iconCount} icons)"
                        )
                    )
                }
            } catch (e: Exception) {
                _events.emit(StudioEvent.ShowMessage("Export failed: ${e.message ?: "Unknown error"}"))
            } finally {
                isExportingFlow.value = false
            }
        }
    }

    fun saveZipToUserUri(format: ExportBundleFormat, destinationUri: Uri) {
        val pack = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            isExportingFlow.value = true
            try {
                val result = IconPackExporter.writeZipToUri(getApplication(), pack, format, destinationUri)
                _events.emit(
                    StudioEvent.ShowMessage("Saved ${result.fileName} (${result.iconCount} icons @ 192px)")
                )
            } catch (e: Exception) {
                _events.emit(StudioEvent.ShowMessage("Save failed: ${e.message ?: "Unknown error"}"))
            } finally {
                isExportingFlow.value = false
            }
        }
    }

    fun shareSingleIconPng(icon: PackIconSpec) {
        val pack = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            try {
                val uri = IconPackExporter.createShareableSinglePng(getApplication(), pack, icon, 512)
                _events.emit(
                    StudioEvent.ShareFile(
                        uri = uri,
                        mimeType = "image/png",
                        title = "${icon.appName} Icon (512px PNG)"
                    )
                )
            } catch (e: Exception) {
                _events.emit(StudioEvent.ShowMessage("Could not share PNG: ${e.message}"))
            }
        }
    }

    fun saveSingleIconPngToUri(icon: PackIconSpec, destinationUri: Uri) {
        val pack = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            try {
                IconPackExporter.writeSinglePngToUri(getApplication(), pack, icon, destinationUri, 512)
                _events.emit(StudioEvent.ShowMessage("Saved ${icon.drawableName}.png (512×512px)"))
            } catch (e: Exception) {
                _events.emit(StudioEvent.ShowMessage("Could not save PNG: ${e.message}"))
            }
        }
    }

    private fun updateActiveStyle(transform: (com.example.data.model.IconPackStyle) -> com.example.data.model.IconPackStyle) {
        val current = (uiState.value as? StudioUiState.Ready)?.activePack ?: return
        viewModelScope.launch {
            repository.updatePack(current.copy(style = transform(current.style)))
        }
    }
}
