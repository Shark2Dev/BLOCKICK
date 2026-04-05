package com.blockick.app.ui.screens.settings

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.ui.components.GlassCard

@Composable
fun ExcludedAppsScreen(
    viewModel: ExcludedAppsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    ExcludedAppsContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onToggleExclusion = viewModel::toggleExclusion,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExcludedAppsContent(
    uiState: List<AppInfo>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onToggleExclusion: (AppInfo) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("User Apps", "System Apps")

    val filteredApps = remember(uiState, selectedTab) {
        val isSystem = selectedTab == 1
        uiState.filter { it.isSystemApp == isSystem }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("App Exclusion", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    placeholder = { Text("Search apps...", color = Color.White.copy(alpha = 0.4f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    shape = CircleShape,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        focusedContainerColor = Color.White.copy(alpha = 0.03f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    modifier = Modifier.padding(horizontal = 24.dp),
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary
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
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                                    letterSpacing = 1.sp,
                                    color = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f)
                                )
                            }
                        )
                    }
                }

                Text(
                    text = if (selectedTab == 0)
                        "Excluded apps will bypass the filtering tunnel."
                        else "Exclude system apps only if essential for network connectivity.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color.White.copy(alpha = 0.02f)
                    ) {
                        if (filteredApps.isEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    if (searchQuery.isEmpty()) "No apps found" else "No matching apps",
                                    color = Color.White.copy(alpha = 0.4f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                items(filteredApps, key = { it.packageName }) { app ->
                                    AppExclusionItem(
                                        app = app,
                                        onToggle = { onToggleExclusion(app) }
                                    )
                                    if (app != filteredApps.last()) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 12.dp),
                                            color = Color.White.copy(alpha = 0.05f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppExclusionItem(
    app: AppInfo,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                if (app.icon != null) {
                    Image(
                        bitmap = app.icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.05f)
                    ) {
                        Icon(Icons.Default.Android, null, modifier = Modifier.padding(8.dp), tint = Color.White.copy(alpha = 0.3f))
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (app.isExcluded) FontWeight.Black else FontWeight.SemiBold,
                    color = if (app.isExcluded) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            Switch(
                checked = app.isExcluded,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                ),
                modifier = Modifier.scale(0.7f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun ExcludedAppsScreenPreview() {
    val mockApps = listOf(
        AppInfo("com.android.chrome", "Chrome", true, false),
        AppInfo("com.spotify.music", "Spotify", false, false),
        AppInfo("com.android.settings", "Settings", false, true)
    )

    MaterialTheme {
        Box(Modifier.background(Color(0xFF0E000A))) {
            ExcludedAppsContent(
                uiState = mockApps,
                searchQuery = "",
                onSearchQueryChanged = {},
                onToggleExclusion = {},
                onBack = {}
            )
        }
    }
}

