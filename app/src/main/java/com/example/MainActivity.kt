package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.luminance
import com.example.ui.components.FloatingGlassNav
import com.example.ui.components.GlassBackground
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.search.DiscoverScreen
import com.example.ui.screens.library.LibraryScreen
import com.example.ui.screens.player.PlayerScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.SonoraTheme
import com.example.ui.viewmodel.SonoraViewModel
import com.example.ui.viewmodel.SonoraViewModelFactory

data class NavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {

    private val viewModel: SonoraViewModel by viewModels {
        val app = application as SonoraApplication
        SonoraViewModelFactory(app.repository, app.player, app.dataStoreManager)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshHomeIfStale()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val dynamicColor by viewModel.dynamicColor.collectAsState()

            // Notification permission request for background playback controls on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            SonoraTheme(
                themeSetting = currentTheme,
                dynamicColor = dynamicColor
            ) {
                SonoraMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SonoraMainApp(viewModel: SonoraViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    val playbackState by viewModel.playbackState.collectAsState()
    val hasTrack = playbackState.currentTrack != null

    BackHandler(enabled = isPlayerExpanded) {
        isPlayerExpanded = false
    }

    val navItems = remember {
        listOf(
            NavItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavItem("Discover", Icons.Filled.Explore, Icons.Outlined.Explore, "nav_discover"),
            NavItem("Search", Icons.Filled.Search, Icons.Outlined.Search, "nav_search"),
            NavItem("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic, "nav_library"),
            NavItem("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
        )
    }

    // Glass only makes sense over a dark ambient background; in the light theme we keep the plain surface.
    val isDarkSurface = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 0: ambient background built from the current artwork colours.
        if (isDarkSurface) {
            GlassBackground(artworkUrl = playbackState.currentTrack?.artworkUrl)
        } else {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        }

        // Layer 1: screens. No bottomBar -> content scrolls *behind* the floating glass bar.
        // Each screen already keeps a bottom contentPadding so the last row is never hidden.
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.statusBars
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToSearch = { selectedTab = 2 }
                    )
                    1 -> DiscoverScreen(viewModel = viewModel)
                    2 -> SearchScreen(viewModel = viewModel)
                    3 -> LibraryScreen(viewModel = viewModel)
                    4 -> SettingsScreen(viewModel = viewModel)
                }
            }
        }

        // Layer 2: floating glass mini-player + floating glass nav. Hidden while the full player is open.
        AnimatedVisibility(
            visible = !isPlayerExpanded,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp)
            ) {
                if (hasTrack) {
                    MiniPlayer(
                        playbackState = playbackState,
                        onExpandPlayer = { isPlayerExpanded = true },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onPrevious = { viewModel.previous() },
                        onSeekBack = { viewModel.seekTo((playbackState.currentPositionMs - 10_000L).coerceAtLeast(0L)) },
                        onSeekForward = {
                            val duration = playbackState.durationMs
                            viewModel.seekTo(
                                if (duration > 0) (playbackState.currentPositionMs + 10_000L).coerceAtMost(duration)
                                else playbackState.currentPositionMs + 10_000L
                            )
                        },
                        onNext = { viewModel.next() },
                        onToggleLike = {
                            playbackState.currentTrack?.let { viewModel.toggleLiked(it) }
                        },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }
                FloatingGlassNav(
                    items = navItems,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Layer 3: full-screen player. Grows up from the mini-player (scale + slide) instead of a plain slide.
        AnimatedVisibility(
            visible = isPlayerExpanded && hasTrack,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
            ) + scaleIn(
                initialScale = 0.9f,
                transformOrigin = TransformOrigin(0.5f, 1f)
            ) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) +
                scaleOut(targetScale = 0.9f, transformOrigin = TransformOrigin(0.5f, 1f)) +
                fadeOut()
        ) {
            PlayerScreen(
                viewModel = viewModel,
                onDismiss = { isPlayerExpanded = false }
            )
        }
    }
}
