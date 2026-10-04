package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.MusicRepository
import com.example.model.Track
import com.example.util.FuzzyMatcher
import com.example.util.ParsedTrackItem
import com.example.util.PlaylistParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ImportState {
    object Idle : ImportState()
    data class Matching(
        val current: Int,
        val total: Int,
        val currentTitle: String
    ) : ImportState()
    data class Completed(
        val playlistName: String,
        val matchedTracks: List<Track>,
        val unmatchedItems: List<ParsedTrackItem>,
        val playlistId: Long
    ) : ImportState()
    data class Error(val message: String) : ImportState()
}

class ExternalPlaylistImportViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun startImport() {
        val input = _inputText.value.trim()
        if (input.isBlank()) {
            _importState.value = ImportState.Error("Please enter a playlist URL or CSV text")
            return
        }

        viewModelScope.launch {
            try {
                val (playlistName, parsedItems) = PlaylistParser.parse(input)
                if (parsedItems.isEmpty()) {
                    _importState.value = ImportState.Error("No tracks could be found in the provided input")
                    return@launch
                }

                val matchedTracks = mutableListOf<Track>()
                val unmatchedItems = mutableListOf<ParsedTrackItem>()

                for ((index, item) in parsedItems.withIndex()) {
                    _importState.value = ImportState.Matching(
                        current = index + 1,
                        total = parsedItems.size,
                        currentTitle = item.title
                    )

                    // Find candidates via repository
                    val query = "${item.title} ${item.artist}".trim()
                    val candidates = repository.search(query)

                    val bestMatch = FuzzyMatcher.findBestMatch(
                        targetTitle = item.title,
                        targetArtist = item.artist,
                        candidates = candidates,
                        threshold = 0.50f
                    )

                    if (bestMatch != null) {
                        matchedTracks.add(bestMatch)
                    } else {
                        unmatchedItems.add(item)
                    }

                    delay(120L) // UI progress smoothness
                }

                // Save matched tracks as local playlist in Room
                val playlistId = repository.createPlaylist(
                    name = playlistName,
                    description = "Imported ${matchedTracks.size} tracks (${unmatchedItems.size} unmatched)"
                )

                for (track in matchedTracks) {
                    repository.addTrackToPlaylist(playlistId, track)
                }

                _importState.value = ImportState.Completed(
                    playlistName = playlistName,
                    matchedTracks = matchedTracks,
                    unmatchedItems = unmatchedItems,
                    playlistId = playlistId
                )
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Failed to import playlist")
            }
        }
    }

    fun reset() {
        _importState.value = ImportState.Idle
        _inputText.value = ""
    }
}
