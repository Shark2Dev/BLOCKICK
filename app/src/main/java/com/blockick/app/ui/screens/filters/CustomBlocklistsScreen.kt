package com.blockick.app.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.data.db.entities.CustomListEntity
import com.blockick.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBlocklistsScreen(
    viewModel: CustomBlocklistsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val customLists by viewModel.customLists.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Custom Blocklists", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp)) },
                    text = { Text("ADD NEW LIST", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.extraLarge
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Add your own filter list URLs. Supported formats: Hosts, Domain list, or Adblock style.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (customLists.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.LinkOff, 
                                contentDescription = null, 
                                modifier = Modifier.size(64.dp).alpha(0.1f),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(16.dp))
                            Text("No custom lists added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(customLists) { list ->
                            CustomBlocklistItem(
                                list = list,
                                onToggle = { viewModel.toggleCustomList(list) },
                                onDelete = { viewModel.removeCustomList(list) }
                            )
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCustomListDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, url, format ->
                    viewModel.addCustomList(name, url, format)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun CustomBlocklistItem(
    list: CustomListEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    CommonListItem(
        title = list.name,
        subtitle = "${list.entryCount} domains â€¢ ${list.format.uppercase()}",
        icon = Icons.Default.Dns,
        iconColor = if (list.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
                Switch(
                    checked = list.isEnabled, 
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    )
}

@Composable
fun AddCustomListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("hosts") }
    val formats = listOf("hosts", "domain", "adblock")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom List") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List Name") },
                    placeholder = { Text("e.g., My Blocklist") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    placeholder = { Text("https://example.com/list.txt") },
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Text("Format", style = MaterialTheme.typography.labelLarge)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        formats.forEach { f ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = format == f, onClick = { format = f })
                                Text(f.uppercase(), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotEmpty() && url.isNotEmpty()) onConfirm(name, url, format) },
                enabled = name.isNotEmpty() && url.isNotEmpty()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

