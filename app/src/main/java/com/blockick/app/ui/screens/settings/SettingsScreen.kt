package com.blockick.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.BuildConfig
import com.blockick.app.R
import com.blockick.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToAudit: () -> Unit
) {
    val upstreamDns by viewModel.upstreamDns.collectAsState("1.1.1.1")
    val autoUpdate by viewModel.autoUpdate.collectAsState(true)
    val updateFrequency by viewModel.updateFrequency.collectAsState(1)
    val safeSearchEnabled by viewModel.safeSearchEnabled.collectAsState(false)
    val bypassEnabled by viewModel.bypassEnabled.collectAsState(false)
    val bypassStartHour by viewModel.bypassStartHour.collectAsState(2)
    val bypassStartMinute by viewModel.bypassStartMinute.collectAsState(0)
    val bypassEndHour by viewModel.bypassEndHour.collectAsState(3)
    val bypassEndMinute by viewModel.bypassEndMinute.collectAsState(0)
    val bypassDays by viewModel.bypassDays.collectAsState("1,2,3,4,5,6,7")

    SettingsContent(
        modifier = modifier,
        contentPadding = contentPadding,
        upstreamDns = upstreamDns,
        autoUpdate = autoUpdate,
        updateFrequency = updateFrequency,
        safeSearchEnabled = safeSearchEnabled,
        bypassEnabled = bypassEnabled,
        bypassStartHour = bypassStartHour,
        bypassStartMinute = bypassStartMinute,
        bypassEndHour = bypassEndHour,
        bypassEndMinute = bypassEndMinute,
        bypassDays = bypassDays,
        onNavigateToAudit = onNavigateToAudit,
        setAutoUpdate = { viewModel.setAutoUpdate(it) },
        setUpdateFrequency = { viewModel.setUpdateFrequency(it) },
        setSafeSearchEnabled = { viewModel.setSafeSearchEnabled(it) },
        setUpstreamDns = { viewModel.setUpstreamDns(it) },
        setBypassEnabled = { viewModel.setBypassEnabled(it) },
        setBypassStartTime = { hour, minute -> viewModel.setBypassStartTime(hour, minute) },
        setBypassEndTime = { hour, minute -> viewModel.setBypassEndTime(hour, minute) },
        setBypassDays = { viewModel.setBypassDays(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    upstreamDns: String,
    autoUpdate: Boolean,
    updateFrequency: Int,
    safeSearchEnabled: Boolean,
    bypassEnabled: Boolean,
    bypassStartHour: Int,
    bypassStartMinute: Int,
    bypassEndHour: Int,
    bypassEndMinute: Int,
    bypassDays: String,
    onNavigateToAudit: () -> Unit,
    setAutoUpdate: (Boolean) -> Unit,
    setUpdateFrequency: (Int) -> Unit,
    setSafeSearchEnabled: (Boolean) -> Unit,
    setUpstreamDns: (String) -> Unit,
    setBypassEnabled: (Boolean) -> Unit,
    setBypassStartTime: (Int, Int) -> Unit,
    setBypassEndTime: (Int, Int) -> Unit,
    setBypassDays: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    var showDnsDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showStartTimeDialog by remember { mutableStateOf(false) }
    var showEndTimeDialog by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }

    val dnsOptions = listOf(
        "1.1.1.1" to "Cloudflare (Recommended)",
        "8.8.8.8" to "Google DNS",
        "9.9.9.9" to "Quad9 (Privacy Focus)"
    )
    
    val frequencyOptions = listOf(
        1 to "Daily",
        3 to "Every 3 days",
        5 to "Every 5 days",
        7 to "Every 7 days"
    )
    
    val frequencyText = frequencyOptions.find { it.first == updateFrequency }?.second ?: "Daily"
    
    val dayNames = listOf(
        1 to "Monday",
        2 to "Tuesday",
        3 to "Wednesday",
        4 to "Thursday",
        5 to "Friday",
        6 to "Saturday",
        7 to "Sunday"
    )
    
    val bypassDaysList = bypassDays.split(",").mapNotNull { it.trim().toIntOrNull() }
    val bypassDaysText = if (bypassDaysList.containsAll(listOf(1,2,3,4,5,6,7))) {
        "Every day"
    } else if (bypassDaysList.containsAll(listOf(1,2,3,4,5)) && bypassDaysList.size == 5) {
        "Weekdays"
    } else if (bypassDaysList.containsAll(listOf(6,7)) && bypassDaysList.size == 2) {
        "Weekends"
    } else {
        bypassDaysList.mapNotNull { dayId -> dayNames.find { it.first == dayId }?.second?.take(3) }.joinToString(", ")
    }
    
    val startTimeText = String.format("%02d:%02d", bypassStartHour, bypassStartMinute)
    val endTimeText = String.format("%02d:%02d", bypassEndHour, bypassEndMinute)
    
    var customDnsInput by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(dnsOptions.none { it.first == upstreamDns }) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent) 
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = contentPadding.calculateTopPadding() + 20.dp)
        ) {
            SectionHeader(
                title = "Settings",
                subtitle = "App and filtering configuration",
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            CommonListItem(
                title = "Filtering Status Check",
                subtitle = "Verify connection and blocking effectiveness",
                icon = Icons.Default.GppGood,
                iconColor = MaterialTheme.colorScheme.primary,
                onClick = onNavigateToAudit
            )

            Spacer(modifier = Modifier.height(32.dp))
            
             Text(
                 text = "Filtering & Network",
                 style = MaterialTheme.typography.titleLarge,
                 fontWeight = FontWeight.Black,
                 color = Color.White,
                 modifier = Modifier.padding(bottom = 16.dp)
             )

            CommonListItem(
                title = "DNS Filtering Provider",
                subtitle = dnsOptions.find { it.first == upstreamDns }?.let { "${it.second}" } ?: "Custom ($upstreamDns)",
                icon = Icons.Default.Dns,
                iconColor = MaterialTheme.colorScheme.secondary,
                onClick = { 
                    isCustomSelected = dnsOptions.none { it.first == upstreamDns }
                    if (isCustomSelected) customDnsInput = upstreamDns
                    showDnsDialog = true 
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CommonListItem(
                title = "Safe Search Enforcement",
                subtitle = "Enable global safe search on supported platforms",
                icon = Icons.Default.VerifiedUser,
                iconColor = MaterialTheme.colorScheme.primary,
                trailingContent = {
                    Switch(
                        checked = safeSearchEnabled, 
                        onCheckedChange = { setSafeSearchEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            CommonListItem(
                title = "Automatic Filter Updates",
                subtitle = "Keep ad block lists current with automatic updates",
                icon = Icons.Default.SystemUpdateAlt,
                iconColor = MaterialTheme.colorScheme.secondary,
                trailingContent = {
                    Switch(
                        checked = autoUpdate, 
                        onCheckedChange = { setAutoUpdate(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                },
                shape = if (autoUpdate) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                } else {
                    RoundedCornerShape(24.dp)
                }
            )

            if (autoUpdate) {
                CommonListItem(
                    title = "Update Frequency",
                    subtitle = frequencyText,
                    icon = Icons.Default.Schedule,
                    iconColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    onClick = { showFrequencyDialog = true },
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Bypass Schedule",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            CommonListItem(
                title = "Scheduled Bypass",
                subtitle = "Temporarily disable blocking for backups and notifications",
                icon = Icons.Default.Timer,
                iconColor = MaterialTheme.colorScheme.tertiary,
                trailingContent = {
                    Switch(
                        checked = bypassEnabled, 
                        onCheckedChange = { setBypassEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = Color.White.copy(alpha = 0.4f),
                            uncheckedTrackColor = Color.White.copy(alpha = 0.05f)
                        )
                    )
                },
                shape = if (bypassEnabled) {
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                } else {
                    RoundedCornerShape(24.dp)
                }
            )

            if (bypassEnabled) {
                CommonListItem(
                    title = "Active Days",
                    subtitle = bypassDaysText,
                    icon = Icons.Default.CalendarToday,
                    iconColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                    onClick = { showDaysDialog = true },
                    shape = RoundedCornerShape(0.dp)
                )

                CommonListItem(
                    title = "Start Time",
                    subtitle = startTimeText,
                    icon = Icons.AutoMirrored.Filled.Login,
                    iconColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                    onClick = { showStartTimeDialog = true },
                    shape = RoundedCornerShape(0.dp)
                )

                CommonListItem(
                    title = "End Time",
                    subtitle = endTimeText,
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                    onClick = { showEndTimeDialog = true },
                    shape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            GlassCard {
                ListItem(
                    headlineContent = { Text("BLOCKICK", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White) },
                    supportingContent = { Text("v${BuildConfig.VERSION_NAME} • Privacy-Focused Ad Blocker", color = Color.White.copy(alpha = 0.6f)) },
                    leadingContent = { 
                        Icon(
                            painter = painterResource(id = R.drawable.ic_protection_shield),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
        
        // DNS Dialog
        if (showDnsDialog) {
            AlertDialog(
                onDismissRequest = { showDnsDialog = false },
                title = { Text("Select DNS Provider", color = Color.White) },
                containerColor = Color(0xFF1A1A1A),
                text = {
                    Column {
                        dnsOptions.forEach { (address, name) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        setUpstreamDns(address)
                                        showDnsDialog = false 
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = upstreamDns == address && !isCustomSelected,
                                    onClick = { 
                                        setUpstreamDns(address)
                                        showDnsDialog = false 
                                    }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text(address, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCustomSelected = true }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isCustomSelected,
                                onClick = { isCustomSelected = true }
                            )
                            Text("Custom DNS", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                        }
                        
                        if (isCustomSelected) {
                            OutlinedTextField(
                                value = customDnsInput,
                                onValueChange = { customDnsInput = it },
                                label = { Text("IP Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    if (isCustomSelected) {
                        TextButton(onClick = { 
                            if (customDnsInput.isNotBlank()) {
                                setUpstreamDns(customDnsInput)
                                showDnsDialog = false
                            }
                        }) {
                            Text("Apply")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDnsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Frequency Dialog
        if (showFrequencyDialog) {
            AlertDialog(
                onDismissRequest = { showFrequencyDialog = false },
                title = { Text("Update Frequency", color = Color.White) },
                containerColor = Color(0xFF1A1A1A),
                text = {
                    Column {
                        frequencyOptions.forEach { (days, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        setUpdateFrequency(days)
                                        showFrequencyDialog = false 
                                    }
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = updateFrequency == days,
                                    onClick = { 
                                        setUpdateFrequency(days)
                                        showFrequencyDialog = false 
                                    }
                                )
                                Text(label, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        // Days Dialog
        if (showDaysDialog) {
            AlertDialog(
                onDismissRequest = { showDaysDialog = false },
                title = { Text("Select Active Days", color = Color.White) },
                containerColor = Color(0xFF1A1A1A),
                text = {
                    Column {
                        dayNames.forEach { (id, name) ->
                            val isSelected = bypassDaysList.contains(id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { 
                                        val newList = if (isSelected) {
                                            bypassDaysList.filter { it != id }
                                        } else {
                                            (bypassDaysList + id).sorted()
                                        }
                                        setBypassDays(newList.joinToString(","))
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val newList = if (!checked) {
                                            bypassDaysList.filter { it != id }
                                        } else {
                                            (bypassDaysList + id).sorted()
                                        }
                                        setBypassDays(newList.joinToString(","))
                                    }
                                )
                                Text(name, color = Color.White, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDaysDialog = false }) {
                        Text("Done")
                    }
                }
            )
        }

        // Time Pickers
        if (showStartTimeDialog) {
            val state = rememberTimePickerState(initialHour = bypassStartHour, initialMinute = bypassStartMinute)
            AlertDialog(
                onDismissRequest = { showStartTimeDialog = false },
                containerColor = Color(0xFF1A1A1A),
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Start Time", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                        TimePicker(
                            state = state,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Color(0xFF2A2A2A),
                                selectorColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.Transparent,
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                                periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                                periodSelectorSelectedContentColor = Color.White,
                                periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                                timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        setBypassStartTime(state.hour, state.minute)
                        showStartTimeDialog = false 
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartTimeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showEndTimeDialog) {
            val state = rememberTimePickerState(initialHour = bypassEndHour, initialMinute = bypassEndMinute)
            AlertDialog(
                onDismissRequest = { showEndTimeDialog = false },
                containerColor = Color(0xFF1A1A1A),
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("End Time", style = MaterialTheme.typography.titleMedium, color = Color.White, modifier = Modifier.padding(bottom = 16.dp))
                        TimePicker(
                            state = state,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Color(0xFF2A2A2A),
                                selectorColor = MaterialTheme.colorScheme.primary,
                                containerColor = Color.Transparent,
                                periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                                periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                                periodSelectorSelectedContentColor = Color.White,
                                periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f),
                                timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                                timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                                timeSelectorSelectedContentColor = Color.White,
                                timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { 
                        setBypassEndTime(state.hour, state.minute)
                        showEndTimeDialog = false 
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndTimeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
