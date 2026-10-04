package com.example.ui.screens.library

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.SonoraApplication
import com.example.model.Playlist
import com.example.model.Track
import com.example.ui.components.TrackItem
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.RosePulse
import com.example.ui.theme.VividCyan
import com.example.ui.viewmodel.ExternalPlaylistImportViewModel
import com.example.ui.viewmodel.SonoraViewModel

@Composable
fun LibraryScreen(
    viewModel: SonoraViewModel,
    modifier: Modifier = Modifier
) {
    val likedTracks by viewModel.likedTracks.collectAsState()
    val downloadedTracks by viewModel.downloadedTracks.collectAsState()
    val historyTracks by viewModel.listeningHistory.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val downloadStatuses by viewModel.downloadStatuses.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Liked Songs", "Playlists", "Downloads", "History")

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    val importViewModel = remember { ExternalPlaylistImportViewModel(SonoraApplication.instance.repository) }
    val context = LocalContext.current
    val csvPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            }.getOrNull()?.let(importViewModel::importCsv)
            showImportDialog = true
        }
    }
    var viewingPlaylist by remember { mutableStateOf<Playlist?>(null) }
    val playlistTracks by remember(viewingPlaylist) {
        if (viewingPlaylist != null) {
            viewModel.player.exoPlayer // reference
            viewModel // reference
        }
        mutableStateOf(emptyList<Track>())
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Library",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedTab == 1) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.testTag("import_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Import External Playlist",
                                tint = VividCyan
                            )
                        }
                        IconButton(
                            onClick = { csvPicker.launch(arrayOf("text/csv", "text/plain", "application/csv")) },
                            modifier = Modifier.testTag("import_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = "Import CSV playlist",
                                tint = VividCyan
                            )
                        }
                        IconButton(
                            onClick = { showCreatePlaylistDialog = true },
                            modifier = Modifier.testTag("create_playlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Playlist",
                                tint = ElectricPurple
                            )
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 20.dp,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricPurple
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == index) ElectricPurple else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Content
            when (selectedTab) {
                0 -> LikedSongsTab(
                    tracks = likedTracks,
                    playbackState = playbackState,
                    onPlayAll = {
                        if (likedTracks.isNotEmpty()) {
                            viewModel.playTrack(likedTracks.first(), likedTracks)
                        }
                    },
                    onTrackClick = { track -> viewModel.playTrack(track, likedTracks) },
                    onToggleLike = { track -> viewModel.toggleLiked(track) },
                    onDownload = { track -> viewModel.downloadTrack(track) }
                )
                1 -> PlaylistsTab(
                    playlists = playlists,
                    onCreateClick = { showCreatePlaylistDialog = true },
                    onImportClick = { showImportDialog = true },
                    onPlaylistClick = { playlist ->
                        // Start mix or play
                    },
                    onDeletePlaylist = { playlistId -> viewModel.deletePlaylist(playlistId) }
                )
                2 -> DownloadsTab(
                    tracks = downloadedTracks,
                    playbackState = playbackState,
                    onPlayAll = {
                        if (downloadedTracks.isNotEmpty()) {
                            viewModel.playTrack(downloadedTracks.first(), downloadedTracks)
                        }
                    },
                    onTrackClick = { track -> viewModel.playTrack(track, downloadedTracks) },
                    onRemoveDownload = { track -> viewModel.removeDownload(track) }
                )
                3 -> HistoryTab(
                    tracks = historyTracks,
                    playbackState = playbackState,
                    onTrackClick = { track -> viewModel.playTrack(track, historyTracks) }
                )
            }
        }
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        var playlistDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("New Playlist", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = playlistDesc,
                        onValueChange = { playlistDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            viewModel.createPlaylist(playlistName.trim(), playlistDesc.trim())
                            showCreatePlaylistDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    enabled = playlistName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showImportDialog) {
        ExternalPlaylistImportDialog(
            importViewModel = importViewModel,
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
fun LikedSongsTab(
    tracks: List<Track>,
    playbackState: com.example.playback.PlaybackState,
    onPlayAll: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onToggleLike: (Track) -> Unit,
    onDownload: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            icon = Icons.Default.Favorite,
            title = "No liked songs yet",
            subtitle = "Tap the heart icon on any track to add it to your favorites."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tracks.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All")
                    }
                }
            }

            items(tracks) { track ->
                val isCurrent = playbackState.currentTrack?.id == track.id
                TrackItem(
                    track = track,
                    isPlaying = isCurrent && playbackState.isPlaying,
                    isCurrentTrack = isCurrent,
                    onClick = { onTrackClick(track) },
                    onToggleLike = { onToggleLike(track) },
                    onPlayNext = {},
                    onAddToQueue = {},
                    onDownload = { onDownload(track) }
                )
            }
        }
    }
}

@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onDeletePlaylist: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 200.dp)
    ) {
        // Import External Playlist Banner Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clickable { onImportClick() },
                shape = RoundedCornerShape(14.dp),
                color = VividCyan.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, VividCyan.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(VividCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = VividCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import External Playlist",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Paste Spotify, YouTube, Apple Music or CSV",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                EmptyLibraryState(
                    icon = Icons.Default.QueueMusic,
                    title = "No custom playlists yet",
                    subtitle = "Create or import playlists from Spotify, YouTube, or Apple Music.",
                    actionLabel = "Create Playlist",
                    onAction = onCreateClick
                )
            }
        } else {
            items(playlists) { playlist ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onPlaylistClick(playlist) },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (playlist.coverArtworkUrl != null) {
                                AsyncImage(
                                    model = playlist.coverArtworkUrl,
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = ElectricPurple)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${playlist.trackCount} tracks",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadsTab(
    tracks: List<Track>,
    playbackState: com.example.playback.PlaybackState,
    onPlayAll: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onRemoveDownload: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            icon = Icons.Default.DownloadDone,
            title = "No downloads yet",
            subtitle = "Download CC-licensed songs to listen offline without internet."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tracks.size} offline tracks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VividCyan
                    )
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(containerColor = VividCyan),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Offline", color = Color.Black)
                    }
                }
            }

            items(tracks) { track ->
                val isCurrent = playbackState.currentTrack?.id == track.id
                TrackItem(
                    track = track,
                    isPlaying = isCurrent && playbackState.isPlaying,
                    isCurrentTrack = isCurrent,
                    onClick = { onTrackClick(track) },
                    onToggleLike = {},
                    onPlayNext = {},
                    onAddToQueue = {},
                    onDownload = { onRemoveDownload(track) }
                )
            }
        }
    }
}

@Composable
fun HistoryTab(
    tracks: List<Track>,
    playbackState: com.example.playback.PlaybackState,
    onTrackClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyLibraryState(
            icon = Icons.Default.History,
            title = "No history yet",
            subtitle = "Songs you play will appear here in your listening log."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            items(tracks) { track ->
                val isCurrent = playbackState.currentTrack?.id == track.id
                TrackItem(
                    track = track,
                    isPlaying = isCurrent && playbackState.isPlaying,
                    isCurrentTrack = isCurrent,
                    onClick = { onTrackClick(track) },
                    onToggleLike = {},
                    onPlayNext = {},
                    onAddToQueue = {},
                    onDownload = {}
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
