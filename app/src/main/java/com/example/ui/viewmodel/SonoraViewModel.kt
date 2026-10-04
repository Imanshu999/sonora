package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.DataStoreManager
import com.example.data.local.DownloadStatus
import com.example.data.repository.MusicRepository
import com.example.model.AudioQuality
import com.example.model.EqualizerPreset
import com.example.model.HomeRow
import com.example.model.MusicLanguage
import com.example.model.Playlist
import com.example.model.SonoraTheme
import com.example.model.SyncedLyrics
import com.example.model.Track
import com.example.playback.PlaybackState
import com.example.playback.SonoraPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val homeRows: List<HomeRow> = emptyList(),
    val trending: List<Track> = emptyList(),
    val newReleases: List<Track> = emptyList(),
    val genreTracks: List<Track> = emptyList(),
    val selectedGenre: String = "All",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

data class SearchUiState(
    val query: String = "",
    val selectedSource: String = "ALL", // "ALL", "YOUTUBE", "AUDIUS", "JAMENDO"
    val results: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

class SonoraViewModel(
    private val repository: MusicRepository,
    val player: SonoraPlayer,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = player.state

    // Home state
    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    // Search state
    private val _searchState = MutableStateFlow(SearchUiState())
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // Language & Source preference
    val selectedLanguage: StateFlow<MusicLanguage> = dataStoreManager.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MusicLanguage.ALL)

    val preferredSources: StateFlow<Set<String>> = dataStoreManager.preferredSourcesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), setOf("YOUTUBE", "AUDIUS", "JAMENDO"))

    val preferredSource: StateFlow<String> = dataStoreManager.preferredSourceFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ALL")

    // Lyrics state
    private val _currentLyrics = MutableStateFlow<SyncedLyrics?>(null)
    val currentLyrics: StateFlow<SyncedLyrics?> = _currentLyrics.asStateFlow()
    private val _lyricsOffsetMs = MutableStateFlow(0L)
    val lyricsOffsetMs: StateFlow<Long> = _lyricsOffsetMs.asStateFlow()
    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    // Library flows
    val likedTracks: StateFlow<List<Track>> = repository.likedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedTracks: StateFlow<List<Track>> = repository.downloadedTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val listeningHistory: StateFlow<List<Track>> = repository.listeningHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadStatuses: StateFlow<Map<String, DownloadStatus>> = repository.getDownloadStatus()

    // Settings flows
    val currentTheme: StateFlow<SonoraTheme> = dataStoreManager.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SonoraTheme.DARK)

    val dynamicColor: StateFlow<Boolean> = dataStoreManager.dynamicColorFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val audioQuality: StateFlow<AudioQuality> = dataStoreManager.audioQualityFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AudioQuality.HIGH)

    val crossfadeSeconds: StateFlow<Int> = dataStoreManager.crossfadeSecondsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val equalizerPreset: StateFlow<EqualizerPreset> = dataStoreManager.equalizerPresetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerPreset.FLAT)

    val loudnessNormalization: StateFlow<Boolean> = dataStoreManager.loudnessNormalizationFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private var searchJob: Job? = null
    private var lastLyricsTrackId: String? = null

    init {
        loadHomeData()
        observeCurrentTrackForLyrics()
        observeLanguageChanges()
    }

    private fun observeLanguageChanges() {
        viewModelScope.launch {
            selectedLanguage.collectLatest { lang ->
                loadHomeData(language = lang)
            }
        }
    }

    fun loadHomeData(
        language: MusicLanguage = selectedLanguage.value,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            if (!forceRefresh && _homeState.value.homeRows.isEmpty()) {
                _homeState.value = _homeState.value.copy(isLoading = true, errorMessage = null)
            }
            try {
                val rows = repository.getHomeRows(
                    language = language,
                    sourceFilter = preferredSources.value.joinToString(","),
                    forceRefresh = forceRefresh
                )
                val trending = repository.getTrendingTracks()
                val releases = repository.getNewReleases()
                val genreTracks = repository.getTracksByGenre("Synthwave")
                _homeState.value = _homeState.value.copy(
                    homeRows = rows,
                    trending = trending,
                    newReleases = releases,
                    genreTracks = genreTracks,
                    isLoading = false,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _homeState.value = _homeState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    errorMessage = e.message ?: "Unable to load songs"
                )
            }
        }
    }

    fun refreshHome() {
        _homeState.value = _homeState.value.copy(isRefreshing = true)
        loadHomeData(forceRefresh = true)
    }

    fun setLanguage(language: MusicLanguage) {
        viewModelScope.launch {
            dataStoreManager.setLanguage(language)
        }
    }

    fun setPreferredSource(source: String) {
        viewModelScope.launch {
            dataStoreManager.setPreferredSource(source)
            loadHomeData(forceRefresh = true)
        }
    }

    fun setSourceEnabled(source: String, enabled: Boolean) {
        viewModelScope.launch {
            val current = preferredSources.value.toMutableSet()
            if (enabled) current.add(source) else current.remove(source)
            if (current.isEmpty()) return@launch
            dataStoreManager.setPreferredSources(current)
            loadHomeData(forceRefresh = true)
        }
    }

    fun selectGenre(genre: String) {
        if (_homeState.value.selectedGenre == genre) return
        _homeState.value = _homeState.value.copy(selectedGenre = genre)
        viewModelScope.launch {
            val tracks = if (genre == "All") {
                _homeState.value.trending
            } else {
                repository.getTracksByGenre(genre)
            }
            _homeState.value = _homeState.value.copy(genreTracks = tracks)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchState.value = _searchState.value.copy(results = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(350L) // debounce
            _searchState.value = _searchState.value.copy(isSearching = true)
            try {
                val results = repository.search(query, _searchState.value.selectedSource)
                _searchState.value = _searchState.value.copy(results = results, isSearching = false)
            } catch (_: Exception) {
                _searchState.value = _searchState.value.copy(isSearching = false)
            }
        }
    }

    fun setSourceFilter(source: String) {
        _searchState.value = _searchState.value.copy(selectedSource = source)
        if (_searchState.value.query.isNotBlank()) {
            onSearchQueryChanged(_searchState.value.query)
        }
    }

    fun playTrack(track: Track, queue: List<Track>? = null) {
        player.playTrack(track, queue)
    }

    fun togglePlayPause() = player.togglePlayPause()

    fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    fun next() = player.next()

    fun previous() = player.previous()

    fun toggleShuffle() = player.toggleShuffle()

    fun toggleRepeat() = player.toggleRepeat()

    fun addToQueue(track: Track) = player.addToQueue(track)

    fun playNext(track: Track) = player.playNext(track)

    fun removeFromQueue(index: Int) = player.removeFromQueue(index)

    fun reorderQueue(from: Int, to: Int) = player.reorderQueue(from, to)

    fun setSleepTimer(minutes: Int) = player.setSleepTimer(minutes)

    fun cancelSleepTimer() = player.cancelSleepTimer()

    fun toggleLiked(track: Track) {
        viewModelScope.launch {
            val isLiked = repository.toggleLiked(track)
            // If currently playing track was toggled, update it
            val current = playbackState.value.currentTrack
            if (current != null && current.id == track.id) {
                // Update track in queue
                val updatedQueue = playbackState.value.queue.map {
                    if (it.id == track.id) it.copy(isLiked = isLiked) else it
                }
                player.playTrack(current.copy(isLiked = isLiked), updatedQueue)
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            repository.downloadTrack(track)
        }
    }

    fun removeDownload(track: Track) {
        viewModelScope.launch {
            repository.removeDownload(track)
        }
    }

    fun startSmartMix() {
        viewModelScope.launch {
            val mix = repository.getSmartMix()
            if (mix.isNotEmpty()) {
                player.playTrack(mix.first(), mix)
            }
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track)
        }
    }

    fun adjustLyricsOffset(deltaMs: Long) {
        _lyricsOffsetMs.value += deltaMs
    }

    fun resetLyricsOffset() {
        _lyricsOffsetMs.value = 0L
    }

    private fun observeCurrentTrackForLyrics() {
        viewModelScope.launch {
            playbackState.collect { state ->
                val track = state.currentTrack
                if (track != null && track.id != lastLyricsTrackId) {
                    lastLyricsTrackId = track.id
                    loadLyricsForTrack(track)
                }
            }
        }
    }

    private fun loadLyricsForTrack(track: Track) {
        viewModelScope.launch {
            _isLyricsLoading.value = true
            _lyricsOffsetMs.value = 0L
            val lyrics = repository.getSyncedLyrics(track)
            _currentLyrics.value = lyrics
            _isLyricsLoading.value = false
        }
    }

    fun setTheme(theme: SonoraTheme) {
        viewModelScope.launch {
            dataStoreManager.setTheme(theme)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setDynamicColor(enabled)
        }
    }

    fun setAudioQuality(quality: AudioQuality) {
        viewModelScope.launch {
            dataStoreManager.setAudioQuality(quality)
        }
    }

    fun setCrossfade(seconds: Int) {
        viewModelScope.launch {
            dataStoreManager.setCrossfade(seconds)
            player.setCrossfade(seconds)
        }
    }

    fun setEqualizerPreset(preset: EqualizerPreset) {
        viewModelScope.launch {
            dataStoreManager.setEqualizerPreset(preset)
            player.setEqualizerPreset(preset)
        }
    }

    fun setLoudnessNormalization(enabled: Boolean) {
        viewModelScope.launch {
            dataStoreManager.setLoudnessNormalization(enabled)
            player.setLoudnessNormalization(enabled)
        }
    }
}

class SonoraViewModelFactory(
    private val repository: MusicRepository,
    private val player: SonoraPlayer,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SonoraViewModel(repository, player, dataStoreManager) as T
    }
}
