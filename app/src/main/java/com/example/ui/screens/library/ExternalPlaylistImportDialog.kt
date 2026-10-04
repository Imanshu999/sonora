package com.example.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricPurple
import com.example.ui.viewmodel.ExternalPlaylistImportViewModel
import com.example.ui.viewmodel.ImportState

@Composable
fun ExternalPlaylistImportDialog(
    importViewModel: ExternalPlaylistImportViewModel,
    onDismiss: () -> Unit
) {
    val inputText by importViewModel.inputText.collectAsState()
    val importState by importViewModel.importState.collectAsState()

    var showUnmatched by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            if (importState !is ImportState.Matching) {
                importViewModel.reset()
                onDismiss()
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import External Playlist", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                when (val state = importState) {
                    is ImportState.Idle, is ImportState.Error -> {
                        Text(
                            text = "Paste a public playlist link (Spotify, YouTube, Apple Music) or CSV text to import tracks into Room:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick presets chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = false,
                                onClick = {
                                    importViewModel.onInputChanged("https://open.spotify.com/playlist/37i9dQZF1DX0XUfTFmNBRM")
                                },
                                label = { Text("Spotify Link", fontSize = 11.sp) }
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    importViewModel.onInputChanged("https://music.youtube.com/playlist?list=RDCLAK5uy_kfd5jH8")
                                },
                                label = { Text("YouTube Link", fontSize = 11.sp) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { importViewModel.onInputChanged(it) },
                            placeholder = { Text("Paste link or CSV: Title, Artist...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("playlist_import_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (state is ImportState.Error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    is ImportState.Matching -> {
                        val fraction = if (state.total > 0) state.current.toFloat() / state.total.toFloat() else 0f
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                progress = { fraction },
                                strokeWidth = 4.dp,
                                color = ElectricPurple,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Matching ${state.current} of ${state.total}",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.currentTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanAccent
                            )
                        }
                    }

                    is ImportState.Completed -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Import Successful!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Saved \"${state.playlistName}\" to your local playlists in Room.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "• Matched tracks: ${state.matchedTracks.size}\n• Unmatched items: ${state.unmatchedItems.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (state.unmatchedItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(onClick = { showUnmatched = !showUnmatched }) {
                                    Text(if (showUnmatched) "Hide Unmatched" else "View Unmatched (${state.unmatchedItems.size})")
                                }

                                AnimatedVisibility(visible = showUnmatched) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 140.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .padding(8.dp)
                                    ) {
                                        items(state.unmatchedItems) { item ->
                                            Text(
                                                text = "• ${item.title} ${if (item.artist.isNotBlank()) "- " + item.artist else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (importState) {
                is ImportState.Idle, is ImportState.Error -> {
                    Button(
                        onClick = { importViewModel.startImport() },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.testTag("start_import_button")
                    ) {
                        Text("Start Matching")
                    }
                }
                is ImportState.Completed -> {
                    Button(
                        onClick = {
                            importViewModel.reset()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                    ) {
                        Text("Done")
                    }
                }
                is ImportState.Matching -> {}
            }
        },
        dismissButton = {
            if (importState !is ImportState.Matching) {
                TextButton(onClick = {
                    importViewModel.reset()
                    onDismiss()
                }) {
                    Text("Cancel")
                }
            }
        }
    )
}
