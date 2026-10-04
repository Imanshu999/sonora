package com.example.ui.screens.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.screens.home.HomeRowSection
import com.example.ui.viewmodel.SonoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: SonoraViewModel,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    PullToRefreshBox(
        isRefreshing = homeState.isRefreshing,
        onRefresh = { viewModel.refreshHome() },
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 200.dp)
        ) {
            item {
                Text(
                    text = "Discover",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
            if (homeState.isLoading && homeState.homeRows.isEmpty()) {
                item { Text("Loading your catalog…", modifier = Modifier) }
            }
            items(homeState.homeRows) { row ->
                HomeRowSection(
                    row = row,
                    playbackState = playbackState,
                    onTrackClick = { track -> viewModel.playTrack(track, row.tracks) }
                )
            }
        }
    }
}
