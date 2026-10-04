package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.model.Track
import com.example.util.ExternalPlaylistResolver
import com.example.util.FuzzyMatcher
import com.example.util.ParsedTrackItem
import com.example.util.PlaylistParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportState {
    data object Idle : ImportState()
    data class Matching(val current: Int, val total: Int, val currentTitle: String) : ImportState()
    data class Completed(
        val playlistName: String,
        val matchedTracks: List<Track>,
        val unmatchedItems: List<ParsedTrackItem>,
        val playlistId: Long
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}

class ExternalPlaylistImportViewModel(
    private val repository: MusicRepository,
    private val resolver: ExternalPlaylistResolver = ExternalPlaylistResolver()
) : ViewModel() {
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()
    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun onInputChanged(text: String) { _inputText.value = text }

    fun importCsv(csvText: String) {
        _inputText.value = csvText
        startImport()
    }

    fun startImport() {
        val input = _inputText.value.trim()
        if (input.isBlank()) {
            _importState.value = ImportState.Error("Paste a playlist URL or CSV content first")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (playlistName, parsedItems) = if (PlaylistParser.isPlaylistUrl(input)) {
                    val playlist = resolver.resolve(input)
                    playlist.name to playlist.tracks
                } else {
                    PlaylistParser.parseCsv(input)
                }
                if (parsedItems.isEmpty()) {
                    _importState.value = ImportState.Error("No tracks could be read from the supplied playlist")
                    return@launch
                }

                val matchedTracks = mutableListOf<Track>()
                val unmatchedItems = mutableListOf<ParsedTrackItem>()
                parsedItems.forEachIndexed { index, item ->
                    _importState.value = ImportState.Matching(index + 1, parsedItems.size, item.title)
                    val candidates = repository.search("${item.title} ${item.artist}".trim())
                    val bestMatch = FuzzyMatcher.findBestMatch(item.title, item.artist, candidates, threshold = 0.55f)
                    if (bestMatch != null) matchedTracks += bestMatch else unmatchedItems += item
                }

                val playlistId = repository.createPlaylist(
                    name = playlistName,
                    description = "Imported ${matchedTracks.size} tracks; ${unmatchedItems.size} unmatched"
                )
                matchedTracks.forEach { repository.addTrackToPlaylist(playlistId, it) }
                _importState.value = ImportState.Completed(playlistName, matchedTracks, unmatchedItems, playlistId)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Playlist import failed")
            }
        }
    }

    fun reset() {
        _importState.value = ImportState.Idle
        _inputText.value = ""
    }
}
