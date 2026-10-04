package com.example.ui.screens.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.SonoraApplication
import com.example.model.AudioQuality
import com.example.model.SonoraTheme
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.VividCyan
import com.example.ui.viewmodel.SonoraViewModel

@Composable
fun SettingsScreen(
    viewModel: SonoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTheme by viewModel.currentTheme.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val preferredSource by viewModel.preferredSource.collectAsState()

    var showThemeDialog by remember { mutableStateOf(false) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSourceDialog by remember { mutableStateOf(false) }

    val cacheBytes = remember {
        SonoraApplication.instance.cacheDir.walkTopDown().sumOf { it.length() }
    }
    val cacheFormatted = "%.1f MB".format(cacheBytes.toFloat() / (1024f * 1024f))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(bottom = 200.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Appearance Section
        item {
            SettingsCategoryHeader(title = "Appearance & Display")
        }

        item {
            SettingsClickableItem(
                icon = Icons.Default.DarkMode,
                title = "Theme Mode",
                subtitle = currentTheme.title,
                onClick = { showThemeDialog = true }
            )
        }

        item {
            SettingsSwitchItem(
                icon = Icons.Default.Palette,
                title = "Material You Dynamic Colors",
                subtitle = "Derive accent highlights from your wallpaper (Android 12+)",
                checked = dynamicColor,
                onCheckedChange = { viewModel.setDynamicColor(it) }
            )
        }

        // Audio Quality Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsCategoryHeader(title = "Music & Regional Preferences")
        }

        item {
            SettingsClickableItem(
                icon = androidx.compose.material.icons.Icons.Default.ColorLens,
                title = "Language & Regional Focus",
                subtitle = selectedLanguage.title,
                onClick = { showLanguageDialog = true }
            )
        }

        item {
            SettingsClickableItem(
                icon = Icons.Default.HighQuality,
                title = "Music Source Engine",
                subtitle = when (preferredSource) {
                    "YOUTUBE" -> "YouTube Music (InnerTube)"
                    "AUDIUS" -> "Audius Decentralized Network"
                    "JAMENDO" -> "Jamendo (Creative Commons)"
                    else -> "All Sources (Unified Discovery)"
                },
                onClick = { showSourceDialog = true }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsCategoryHeader(title = "Audio Quality & Streaming")
        }

        item {
            SettingsClickableItem(
                icon = Icons.Default.HighQuality,
                title = "Streaming Bitrate",
                subtitle = audioQuality.title,
                onClick = { showQualityDialog = true }
            )
        }

        // Storage & Cache Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsCategoryHeader(title = "Storage & Downloads")
        }

        item {
            SettingsClickableItem(
                icon = Icons.Default.CleaningServices,
                title = "Audio & Artwork Cache",
                subtitle = "$cacheFormatted cached on device",
                onClick = { showClearCacheDialog = true }
            )
        }

        // About & Legal Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SettingsCategoryHeader(title = "About Sonora")
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricPurple),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Sonora Music v1.0",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Premium open music streaming experience",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "• Powered by Jamendo API (CC-licensed legal music catalog)\n• Audius Decentralized Protocol\n• Synced Karaoke Lyrics by LRCLIB\n• AndroidX Media3 ExoPlayer with MediaSession background audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Choose Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    SonoraTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == theme,
                                onClick = {
                                    viewModel.setTheme(theme)
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = theme.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (currentTheme == theme) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Close", color = ElectricPurple)
                }
            }
        )
    }

    // Quality Dialog
    if (showQualityDialog) {
        AlertDialog(
            onDismissRequest = { showQualityDialog = false },
            title = { Text("Audio Streaming Quality", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    AudioQuality.values().forEach { quality ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAudioQuality(quality)
                                    showQualityDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = audioQuality == quality,
                                onClick = {
                                    viewModel.setAudioQuality(quality)
                                    showQualityDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = quality.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = quality.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityDialog = false }) {
                    Text("Close", color = ElectricPurple)
                }
            }
        )
    }

    // Clear Cache Dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Temporary Cache?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will free $cacheFormatted of temporary audio and album artwork files. Your liked tracks and downloaded offline songs will be preserved.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            SonoraApplication.instance.cacheDir.deleteRecursively()
                        } catch (_: Exception) {}
                        showClearCacheDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Language & Region Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Language & Regional Focus", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    com.example.model.MusicLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lang.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedLanguage == lang) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Done", color = ElectricPurple)
                }
            }
        )
    }

    // Music Source Engine Dialog
    if (showSourceDialog) {
        val sourceOptions = listOf(
            Pair("ALL", "All Sources (Unified Discovery)"),
            Pair("YOUTUBE", "YouTube Music (InnerTube)"),
            Pair("AUDIUS", "Audius Decentralized Network"),
            Pair("JAMENDO", "Jamendo (Creative Commons)")
        )
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Music Source Engine", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    sourceOptions.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setPreferredSource(key)
                                    showSourceDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferredSource == key,
                                onClick = {
                                    viewModel.setPreferredSource(key)
                                    showSourceDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = ElectricPurple)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (preferredSource == key) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Done", color = ElectricPurple)
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = ElectricPurple,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = ElectricPurple, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = ElectricPurple
            )
        )
    }
}
