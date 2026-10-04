package com.example.ui.screens.player

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SyncedLyrics
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.NeonViolet

@Composable
fun LyricsView(
    lyrics: SyncedLyrics?,
    isLoading: Boolean,
    currentPositionMs: Long,
    offsetMs: Long,
    onSeekTo: (Long) -> Unit,
    onAdjustOffset: (Long) -> Unit,
    onResetOffset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val adjustedPosition = (currentPositionMs + offsetMs).coerceAtLeast(0L)

    val currentLineIndex by remember(lyrics, adjustedPosition) {
        derivedStateOf {
            if (lyrics == null || !lyrics.isSynced || lyrics.lines.isEmpty()) -1
            else {
                val idx = lyrics.lines.indexOfLast { it.timeMs <= adjustedPosition }
                if (idx >= 0) idx else 0
            }
        }
    }

    // Auto-scroll to active lyric line
    LaunchedEffect(currentLineIndex) {
        if (currentLineIndex >= 0) {
            val target = (currentLineIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(target)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("lyrics_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Offset Adjustment Controls Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.35f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sync Offset: ${if (offsetMs >= 0) "+${offsetMs}ms" else "${offsetMs}ms"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyanAccent
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAdjustOffset(-500L) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "-0.5s", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { onAdjustOffset(500L) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "+0.5s", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    if (offsetMs != 0L) {
                        IconButton(
                            onClick = onResetOffset,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Reset offset", tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonViolet)
            }
        } else if (lyrics == null || lyrics.lines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = lyrics?.rawPlain?.ifBlank { "No lyrics available for this track" }
                        ?: "No lyrics available for this track",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 60.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                itemsIndexed(lyrics.lines) { index, line ->
                    val isCurrent = index == currentLineIndex
                    val isPast = index < currentLineIndex

                    Text(
                        text = line.text,
                        style = if (isCurrent) {
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp,
                                lineHeight = 32.sp
                            )
                        } else {
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                lineHeight = 26.sp
                            )
                        },
                        color = when {
                            isCurrent -> Color.White
                            isPast -> Color.White.copy(alpha = 0.6f)
                            else -> Color.White.copy(alpha = 0.35f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSeekTo(line.timeMs) }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
