package com.blockick.app.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.data.db.entities.BlocklistEntity
import com.blockick.app.ui.components.*
import com.blockick.app.ui.components.glowStroke
import kotlinx.coroutines.launch

@Composable
fun FiltersScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: BlocklistsViewModel = hiltViewModel(),
    onNavigateToCustomLists: () -> Unit,
    onNavigateToExclusion: () -> Unit,
    onNavigateToPersonalRules: () -> Unit
) {
    val blocklists by viewModel.blocklists.collectAsState(initial = emptyList())
    var selectedParentId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    BackHandler(enabled = selectedParentId != null) {
        selectedParentId = null
    }

    FiltersContent(
        modifier = modifier,
        contentPadding = contentPadding,
        blocklists = blocklists,
        selectedParentId = selectedParentId,
        onSelectedParentIdChange = { selectedParentId = it },
        onNavigateToCustomLists = onNavigateToCustomLists,
        onNavigateToExclusion = onNavigateToExclusion,
        onNavigateToPersonalRules = onNavigateToPersonalRules,
        onUpdateAll = {
            viewModel.updateAll()
            scope.launch {
                com.blockick.app.ui.components.GlobalSnackbar.show("Syncing privacy rules...")
            }
        },
        onToggleList = { viewModel.toggleList(it) }
    )
}

@Composable
fun FiltersContent(
    blocklists: List<BlocklistEntity>,
    selectedParentId: String?,
    onSelectedParentIdChange: (String?) -> Unit,
    onNavigateToCustomLists: () -> Unit,
    onNavigateToExclusion: () -> Unit,
    onNavigateToPersonalRules: () -> Unit,
    onUpdateAll: () -> Unit,
    onToggleList: (BlocklistEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (selectedParentId != null) {
            val parent = blocklists.find { it.id == selectedParentId }
            val children = blocklists.filter { it.parentId == selectedParentId }
            
            if (parent != null) {
                ConfigurationsDetail(
                    parent = parent,
                    children = children,
                    onBack = { onSelectedParentIdChange(null) },
                    onToggle = { onToggleList(it) }
                )
            }
        } else {
            MainFiltersView(
                contentPadding = contentPadding,
                blocklists = blocklists,
                onNavigateToCustomLists = onNavigateToCustomLists,
                onNavigateToExclusion = onNavigateToExclusion,
                onNavigateToPersonalRules = onNavigateToPersonalRules,
                onUpdateAll = onUpdateAll,
                onToggle = { onToggleList(it) },
                onSelect = { onSelectedParentIdChange(it.id) }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun FiltersScreenPreview() {
    val mockLists = listOf(
        BlocklistEntity(id = "ads", name = "Advertising", url = "", format = "hosts", isEnabled = true, entryCount = 15000),
        BlocklistEntity(id = "trackers", name = "Trackers", url = "", format = "hosts", isEnabled = true, entryCount = 8000),
        BlocklistEntity(id = "malware", name = "Malware Protection", url = "", format = "hosts", isEnabled = false, entryCount = 2500)
    )

    MaterialTheme {
        FiltersContent(
            blocklists = mockLists,
            selectedParentId = null,
            onSelectedParentIdChange = {},
            onNavigateToCustomLists = {},
            onNavigateToExclusion = {},
            onNavigateToPersonalRules = {},
            onUpdateAll = {},
            onToggleList = {}
        )
    }
}

@Composable
fun MainFiltersView(
    blocklists: List<BlocklistEntity>,
    onNavigateToCustomLists: () -> Unit,
    onNavigateToExclusion: () -> Unit,
    onNavigateToPersonalRules: () -> Unit,
    onUpdateAll: () -> Unit,
    onToggle: (BlocklistEntity) -> Unit,
    onSelect: (BlocklistEntity) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val parentLists = remember(blocklists) { blocklists.filter { it.parentId == null } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding())
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 20.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ADVANCED CONTROL SECTION (Now part of the main scroll)
             item {
                 SectionHeader(
                     title = "Ad Blocking Engine",
                     subtitle = "Manage your filtering and protection rules",
                     modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                 )
             }
             
             item {
                 Text(
                     text = "CUSTOM BLOCKING RULES",
                     style = MaterialTheme.typography.labelSmall,
                     fontWeight = FontWeight.Black,
                     color = Color.White.copy(alpha = 0.4f),
                     modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                     letterSpacing = 2.sp
                 )
             }

             item {
                 AdvancedControlCard(
                     title = "Filter List Subscriptions",
                     description = "Subscribe to community-maintained ad and tracker blocklists.",
                     icon = Icons.Default.AddLink,
                     color = MaterialTheme.colorScheme.primary,
                     onClick = onNavigateToCustomLists
                 )
             }

             item {
                 AdvancedControlCard(
                     title = "Custom Rules Manager",
                     description = "Add individual domains to your personal block or allow list.",
                     icon = Icons.Default.PlaylistAddCheck,
                     color = MaterialTheme.colorScheme.secondary,
                     onClick = onNavigateToPersonalRules
                 )
             }

             item {
                 AdvancedControlCard(
                     title = "App Exceptions",
                     description = "Select apps that should bypass the ad blocking filter.",
                     icon = Icons.Default.AppBlocking,
                     color = MaterialTheme.colorScheme.tertiary,
                     onClick = onNavigateToExclusion
                 )
             }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Filter Lists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = onUpdateAll,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Rules")
                    }
                }
            }

            items(parentLists) { list ->
                val totalDomains = if (list.url.isNotEmpty()) {
                    list.entryCount
                } else {
                    blocklists.filter { it.parentId == list.id }.sumOf { it.entryCount }
                }

                BlocklistItem(
                    list = list,
                    totalDomains = totalDomains,
                    onClick = { onSelect(list) },
                    onToggle = { onToggle(list) }
                )
            }
        }
    }
}

@Composable
fun AdvancedControlCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        onClick = onClick,
        modifier = modifier,
        containerColor = Color.White.copy(alpha = 0.02f),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp).glowStroke(color, CircleShape, glowRadius = 6.dp, alpha = 0.3f),
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun BlocklistItem(
    list: BlocklistEntity,
    totalDomains: Int,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    val isParentGroup = list.url.isEmpty()
    
    CommonListItem(
        title = list.name,
        subtitle = if (isParentGroup) "$totalDomains domains in group" else "$totalDomains domains",
        icon = if (isParentGroup) Icons.Default.FolderSpecial else Icons.Default.Security,
        iconColor = if (list.isEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
        trailingContent = {
            Switch(
                checked = list.isEnabled, 
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                    uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                )
            )
        },
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationsDetail(
    parent: BlocklistEntity,
    children: List<BlocklistEntity>,
    onBack: () -> Unit,
    onToggle: (BlocklistEntity) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(parent.name, fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .fillMaxSize()
            ) {
                if (parent.description.isNotEmpty()) {
                    GlassCard {
                        Text(
                            text = parent.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                Text(
                    text = "Profiles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(children) { child ->
                        ConfigurationItem(
                            child = child,
                            onToggle = { onToggle(child) }
                        )
                    }

                    if (parent.authorUrl.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AuthorAttributionCard(
                                author = parent.author,
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(parent.authorUrl))
                                    context.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthorAttributionCard(
    author: String,
    onClick: () -> Unit
) {
    GlassCard(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp).glowStroke(MaterialTheme.colorScheme.primary, CircleShape, glowRadius = 6.dp, alpha = 0.3f),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Project Maintainer",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Text(
                        text = if (author.isNotEmpty()) author else "Official Source",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ConfigurationItem(
    child: BlocklistEntity,
    onToggle: () -> Unit
) {
    CommonListItem(
        title = child.name,
        subtitle = "${child.entryCount} domains",
        icon = if (child.isEnabled) Icons.Default.Security else Icons.Default.Layers,
        iconColor = if (child.isEnabled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
        trailingContent = {
            Checkbox(
                checked = child.isEnabled,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        },
        onClick = onToggle
    )
}

