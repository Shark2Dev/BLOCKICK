package com.blockick.app.ui.screens.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.ui.components.GlassCard
import com.blockick.app.ui.components.glowStroke

@Composable
fun NetworkAuditScreen(
    viewModel: NetworkAuditViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    NetworkAuditContent(
        uiState = uiState,
        onBack = onBack,
        onStartScan = { viewModel.startScan() },
        onResultActionClick = { result ->
            if (result.title.contains("DNS")) {
                context.startActivity(Intent(Settings.ACTION_VPN_SETTINGS))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkAuditContent(
    uiState: AuditUiState,
    onBack: () -> Unit,
    onStartScan: () -> Unit,
    onResultActionClick: (AuditResult) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Network Health Check", fontWeight = FontWeight.Black, color = Color.White) },
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
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Score Circle
                HealthScoreCircle(uiState.overallScore, uiState.isScanning)

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onStartScan,
                    enabled = !uiState.isScanning,
                    modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isScanning) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    if (uiState.isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("DIAGNOSING...", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.White)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("RE-CHECK STATUS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                    uiState.results.forEach { result ->
                        AuditResultCard(result) {
                            onResultActionClick(result)
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun NetworkAuditScreenPreview() {
    val mockResults = listOf(
        AuditResult("Protection Status", "VPN tunnel is active and filtering traffic.", AuditStatus.SECURE),
        AuditResult("Private DNS Conflict", "Android Private DNS is active and might bypass your blocklists.", AuditStatus.WARNING, "Open Settings"),
        AuditResult("Upstream DNS Security", "Using encrypted DoH (DNS-over-HTTPS) for external queries.", AuditStatus.SECURE)
    )

    MaterialTheme {
        NetworkAuditContent(
            uiState = AuditUiState(
                isScanning = false,
                results = mockResults,
                overallScore = 85
            ),
            onBack = {},
            onStartScan = {},
            onResultActionClick = {}
        )
    }
}

@Composable
fun HealthScoreCircle(score: Int, isScanning: Boolean) {
    val animatedScore by animateIntAsState(
        targetValue = if (isScanning) 0 else score,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "score"
    )

    val color = when {
        isScanning -> MaterialTheme.colorScheme.primary
        score >= 80 -> com.blockick.app.ui.theme.SuccessColor
        score >= 50 -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.error
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
        CircularProgressIndicator(
            progress = { if (isScanning) 0.2f else animatedScore / 100f },
            modifier = Modifier.fillMaxSize().glowStroke(color, CircleShape, strokeWidth = 8.dp, glowRadius = 12.dp, alpha = 0.4f),
            color = color,
            strokeWidth = 8.dp,
            trackColor = Color.White.copy(alpha = 0.05f),
        )
        
        Surface(
            modifier = Modifier.size(180.dp).alpha(if (isScanning) 0.08f else 0.15f),
            shape = CircleShape,
            color = color
        ) {}

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isScanning) "--" else "$animatedScore",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "HEALTH SCORE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = color.copy(alpha = 0.7f),
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun AuditResultCard(result: AuditResult, onActionClick: () -> Unit) {
    val statusColor = when (result.status) {
        AuditStatus.SECURE -> com.blockick.app.ui.theme.SuccessColor
        AuditStatus.WARNING -> Color(0xFFFFC107)
        AuditStatus.DANGER -> MaterialTheme.colorScheme.error
        AuditStatus.LOADING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }

    val icon = when (result.status) {
        AuditStatus.SECURE -> Icons.Default.CheckCircle
        AuditStatus.WARNING -> Icons.Default.Warning
        AuditStatus.DANGER -> Icons.Default.Report
        AuditStatus.LOADING -> Icons.Default.Sync
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.White.copy(alpha = 0.02f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp).glowStroke(statusColor, CircleShape, glowRadius = 6.dp, alpha = 0.3f),
                shape = CircleShape,
                color = statusColor.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = result.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    lineHeight = 16.sp
                )
                if (result.action != null) {
                    TextButton(
                        onClick = onActionClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = result.action.uppercase(), 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

