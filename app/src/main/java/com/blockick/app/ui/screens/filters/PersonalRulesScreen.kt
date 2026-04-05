package com.blockick.app.ui.screens.filters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalRulesScreen(
    viewModel: PersonalRulesViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val allowedDomains by viewModel.allowedDomains.collectAsState()
    val blockedDomains by viewModel.blockedDomains.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Allowed", "Blocked")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Personal Rules", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    text = title.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedTab == index) FontWeight.Black else FontWeight.Medium,
                                    letterSpacing = 1.sp
                                ) 
                            }
                        )
                    }
                }

                val currentList = if (selectedTab == 0) allowedDomains.map { it.domain } else blockedDomains.map { it.domain }
                val emptyMessage = if (selectedTab == 0) "No allowed domains" else "No blocked domains"
                val accentColor = if (selectedTab == 0) com.blockick.app.ui.theme.SuccessColor else MaterialTheme.colorScheme.error

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (selectedTab == 0) 
                            "Domains in this list bypass the filtering engine." 
                            else "Domains in this list are always filtered.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (currentList.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.VerifiedUser else Icons.Default.Block, 
                                    contentDescription = null, 
                                    modifier = Modifier.size(64.dp).alpha(0.1f),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentList) { domain ->
                                CommonListItem(
                                    title = domain,
                                    icon = if (selectedTab == 0) Icons.Default.Check else Icons.Default.Block,
                                    iconColor = accentColor,
                                    trailingContent = {
                                        IconButton(onClick = {
                                            if (selectedTab == 0) viewModel.removeAllowedDomain(domain)
                                            else viewModel.removeBlockedDomain(domain)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline, 
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

