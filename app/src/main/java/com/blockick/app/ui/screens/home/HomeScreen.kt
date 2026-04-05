package com.blockick.app.ui.screens.home

import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.R
import com.blockick.app.data.db.entities.QueryLogEntity
import com.blockick.app.ui.MainActivity
import com.blockick.app.ui.components.*
import com.blockick.app.vpn.VpnStatus

fun Context.findActivity(): MainActivity? = when (this) {
    is MainActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun HomeScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: HomeViewModel = hiltViewModel(LocalContext.current.findActivity()!!),
    onLogClick: (QueryLogEntity) -> Unit
) {
    val blockedToday by viewModel.blockedToday.collectAsState(0)
    val blockedWeekly by viewModel.blockedWeekly.collectAsState(0)
    val blockedTotal by viewModel.blockedTotal.collectAsState(0)
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()
    val rulesCount by viewModel.rulesCount.collectAsState()
    val recentBlocked by viewModel.recentBlockedLogs.collectAsState(emptyList())
    
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            if (event is HomeEvent.Loading) {
                isLoading = event.isLoading
            }
        }
    }

    HomeContent(
        contentPadding = contentPadding,
        blockedToday = blockedToday,
        blockedWeekly = blockedWeekly,
        blockedTotal = blockedTotal,
        vpnStatus = vpnStatus,
        activeProfile = activeProfile,
        rulesCount = rulesCount,
        recentBlocked = recentBlocked,
        isLoading = isLoading,
        onToggleVpn = { viewModel.toggleVpn() },
        onSetProfile = { viewModel.setProfile(it) },
        onUpdateAll = { viewModel.updateAll() },
        onLogClick = onLogClick
    )
}

@Composable
fun HomeContent(
    blockedToday: Int,
    blockedWeekly: Int,
    blockedTotal: Int,
    vpnStatus: VpnStatus,
    activeProfile: String,
    rulesCount: Int,
    recentBlocked: List<QueryLogEntity>,
    isLoading: Boolean,
    onToggleVpn: () -> Unit,
    onSetProfile: (String) -> Unit,
    onUpdateAll: () -> Unit,
    onLogClick: (QueryLogEntity) -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             SectionHeader(
                 title = "BLOCKICK",
                 subtitle = "Current filtering status",
                 modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
             )

            if (rulesCount == 0) {
                FilterUpdateCard(onUpdate = onUpdateAll)
            }

            AdBlockingToggle(vpnStatus = vpnStatus, onToggle = onToggleVpn)

            ProfileSelector(activeProfile = activeProfile, onProfileSelected = onSetProfile, isLoading = isLoading)

            Spacer(modifier = Modifier.height(40.dp))

            StatisticsHeader(today = blockedToday, weekly = blockedWeekly, total = blockedTotal)

            Spacer(modifier = Modifier.height(40.dp))

            if (vpnStatus == VpnStatus.RUNNING) {
                LiveProtectionFeed(recentBlocked, onLogClick)
            }
            
            Spacer(modifier = Modifier.height(contentPadding.calculateBottomPadding() + 24.dp))
        }

        if (isLoading) {
            LoadingOverlay()
        }
    }
}

@Composable
fun AdBlockingToggle(
    vpnStatus: VpnStatus,
    onToggle: () -> Unit
) {
    val isRunning = vpnStatus == VpnStatus.RUNNING
    val isStarting = vpnStatus == VpnStatus.STARTING
    val isActive = isRunning || isStarting

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = if (isActive) 0.5f else 0.1f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "glow"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isActive) 1.03f else 1f,
        animationSpec = infiniteRepeatable(animation = tween(2500, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
    ) {
        val toggleSize = 220.dp
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(toggleSize)) {
            if (isActive) {
                Box(modifier = Modifier.size(toggleSize * 0.9f).background(Brush.radialGradient(listOf(
                    if (isRunning) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Yellow.copy(alpha = 0.25f),
                    Color.Transparent
                ))))
            }
            Box(
                modifier = Modifier
                    .size(toggleSize * 0.95f)
                    .then(if (isActive) Modifier.glowStroke(if (isRunning) MaterialTheme.colorScheme.primary else Color.Yellow, CircleShape, glowRadius = 12.dp, alpha = 0.2f) else Modifier)
                    .border(
                        width = 0.5.dp, 
                        brush = Brush.sweepGradient(listOf(Color.Transparent, if (isActive) (if (isRunning) MaterialTheme.colorScheme.primary else Color.Yellow).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), Color.Transparent)), 
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(toggleSize * 0.75f)
                    .then(if (isActive) Modifier.glowStroke(if (isRunning) MaterialTheme.colorScheme.primary else Color.Yellow, CircleShape, glowRadius = 20.dp, alpha = glowAlpha * 0.8f) else Modifier)
                    .border(
                        width = 1.dp, 
                        color = if (isActive) (if (isRunning) MaterialTheme.colorScheme.primary else Color.Yellow).copy(alpha = glowAlpha) else Color.White.copy(alpha = 0.03f), 
                        shape = CircleShape
                    )
            )

            val brush = if (isRunning) Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
            else if (isStarting) Brush.verticalGradient(listOf(Color.Yellow, Color(0xFFFFCA28)))
            else Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surface))

            Box(
                modifier = Modifier
                    .size(toggleSize * 0.55f * scale)
                    .then(if (isActive) Modifier.glowStroke(Color.White, CircleShape, glowRadius = 15.dp, alpha = 0.2f) else Modifier)
                    .clip(CircleShape)
                    .background(brush)
                    .clickable { onToggle() }
                    .border(width = 1.dp, color = if (isActive) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isStarting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(toggleSize * 0.25f),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_protection_shield), 
                        contentDescription = if (isRunning) "Stop Protection" else "Start Protection", 
                        modifier = Modifier.size(toggleSize * 0.25f), 
                        tint = if (isRunning) Color.White else Color.White.copy(alpha = 0.2f)
                    )
                }
            }
        }
        
         Text(
             text = when (vpnStatus) {
                 VpnStatus.RUNNING -> "AD BLOCKING ACTIVE"
                 VpnStatus.STARTING -> "STARTING FILTERING"
                 VpnStatus.STOPPED -> "AD BLOCKING PAUSED"
             },
             style = MaterialTheme.typography.labelSmall,
             color = if (isRunning) MaterialTheme.colorScheme.primary else if (isStarting) Color.Yellow else Color.White.copy(alpha = 0.4f),
             fontWeight = FontWeight.Black,
             letterSpacing = 4.sp
         )
    }
}

@Composable
fun LiveProtectionFeed(recentBlocked: List<QueryLogEntity>, onLogClick: (QueryLogEntity) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "LIVE TRACKER FEED", 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = Color.White.copy(alpha = 0.4f),
                letterSpacing = 2.sp
            )
            StatusChip(text = "MONITORING", color = MaterialTheme.colorScheme.primary)
        }

        GlassCard(containerColor = Color.White.copy(alpha = 0.02f)) {
            if (recentBlocked.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    Text("Watching for trackers...", color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentBlocked.take(3).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { onLogClick(log) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    Icons.Default.Security, 
                                    contentDescription = "Tracker Blocked", 
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), 
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    log.domain, 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                                contentDescription = "View Details", 
                                tint = Color.White.copy(alpha = 0.2f), 
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (log != recentBlocked.take(3).last()) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsHeader(today: Int, weekly: Int, total: Int) {
    GlassCard(containerColor = Color.White.copy(alpha = 0.05f)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatItem(label = "TODAY", count = today, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)).align(Alignment.CenterVertically))
            StatItem(label = "WEEKLY", count = weekly, color = Color.White, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)).align(Alignment.CenterVertically))
            StatItem(label = "TOTAL", count = total, color = Color.White, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatItem(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp,
            fontSize = 11.sp
        )
    }
}

@Composable
fun FilterUpdateCard(onUpdate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CloudDownload, contentDescription = "Update Required", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(12.dp))
            Text("Filter Update Required", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
             Button(
                 onClick = onUpdate,
                 colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                 shape = CircleShape
             ) {
                 Text("Update Filter Lists", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
             }
        }
    }
}

@Composable
fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
    }
}

@Composable
fun ProfileSelector(
    activeProfile: String,
    onProfileSelected: (String) -> Unit,
    isLoading: Boolean
) {
    val profiles = listOf(
        Triple("MINIMAL", Icons.Default.ShieldMoon, "Light"),
        Triple("BALANCED", Icons.Default.Shield, "Optimal"),
        Triple("ULTRA", Icons.Default.Security, "Aggressive")
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        profiles.forEach { (name, icon, _) ->
            val selected = activeProfile.equals(name, ignoreCase = true)
            Surface(
                onClick = { if (!isLoading) onProfileSelected(name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.weight(1f),
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 0.5.dp, 
                    color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
