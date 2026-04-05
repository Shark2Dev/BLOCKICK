package com.blockick.app.ui.screens.activity

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.ui.components.GlassCard
import com.blockick.app.ui.components.StatusChip
import com.blockick.app.ui.components.glowStroke

@Composable
fun ActivityDetailScreen(
    viewModel: ActivityDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    ActivityDetailContent(
        uiState = uiState,
        onBack = onBack,
        onToggleAllow = { viewModel.toggleAllow() },
        onToggleBlock = { viewModel.toggleBlock() },
        onCopyAddress = { clipboardManager.setText(AnnotatedString(uiState.domain)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailContent(
    uiState: ActivityDetailUiState,
    onBack: () -> Unit,
    onToggleAllow: () -> Unit,
    onToggleBlock: () -> Unit,
    onCopyAddress: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Request Analysis", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { innerPadding ->
            // PRIORITY STATUS LOGIC
            val isAllowed = uiState.isAllowed || (!uiState.isBlocked && !uiState.isLoading)
            
            val statusColor = if (isAllowed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
            val statusIcon = if (isAllowed) Icons.Default.CheckCircle else Icons.Default.Security
            val statusText = if (isAllowed) "STATUS: ALLOWED" else "STATUS: BLOCKED"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                if (uiState.isLoading) {
                    Box(Modifier.height(160.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    // MAIN STATUS INDICATOR
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                        Surface(
                            modifier = Modifier.size(140.dp).glowStroke(statusColor, CircleShape, glowRadius = 15.dp, alpha = 0.3f),
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.05f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.15f))
                        ) {}
                        
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = statusColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = uiState.domain,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    StatusChip(
                        text = statusText,
                        color = statusColor
                    )
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // ACTIONS SECTION
                    DetailSectionHeader("MANAGEMENT ACTIONS")
                    GlassCard {
                        if (!isAllowed) {
                            ActionItem(
                                title = if (uiState.isAllowed) "Remove from Whitelist" else "Add to Whitelist",
                                icon = Icons.Outlined.CheckCircle,
                                color = Color(0xFF4CAF50),
                                onClick = onToggleAllow
                            )
                        } else {
                            ActionItem(
                                title = if (uiState.isBlocked) "Unblock Domain" else "Block Domain",
                                icon = Icons.Outlined.Block,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = onToggleBlock
                            )
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        ActionItem(
                            title = "Copy Address",
                            icon = Icons.Default.ContentCopy,
                            color = Color.White.copy(alpha = 0.6f),
                            onClick = onCopyAddress
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // SPECIFICATIONS SECTION
                    DetailSectionHeader("TECHNICAL SPECIFICATIONS")
                    GlassCard {
                        SpecRow(label = "Domain Endpoint", value = uiState.domain)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                        SpecRow(label = "Protocol Query Type", value = uiState.queryType)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                        SpecRow(label = "Interaction History", value = "${uiState.occurrenceCount} Total Hits")
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun ActivityDetailScreenPreview() {
    MaterialTheme {
        ActivityDetailContent(
            uiState = ActivityDetailUiState(
                domain = "google-analytics.com",
                queryType = "HTTPS/DOH",
                occurrenceCount = 142,
                isAllowed = false,
                isBlocked = true,
                isLoading = false
            ),
            onBack = {},
            onToggleAllow = {},
            onToggleBlock = {},
            onCopyAddress = {}
        )
    }
}

@Composable
fun DetailSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Black,
        letterSpacing = 2.sp,
        color = Color.White.copy(alpha = 0.4f),
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 12.dp)
    )
}

@Composable
fun ActionItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
fun SpecRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.5f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = Color.White)
    }
}

