package com.blockick.app.ui.screens.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.blockick.app.data.db.dao.QueryLogDao.TopDomain
import com.blockick.app.data.db.entities.QueryLogEntity
import com.blockick.app.data.db.entities.StatsEntity
import com.blockick.app.ui.components.*
import com.blockick.app.ui.components.glowStroke
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun TopBlockedSection(topDomains: List<TopDomain>, onDomainClick: (String) -> Unit) {
    Text(
        text = "Most Blocked Ads & Trackers",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Black,
        color = Color.White,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    GlassCard {
        topDomains.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDomainClick(item.domain) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        modifier = Modifier.size(24.dp).glowStroke(MaterialTheme.colorScheme.primary, CircleShape, glowRadius = 4.dp, alpha = 0.3f),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = item.domain,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${item.count}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (index < topDomains.size - 1) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: StatsViewModel = hiltViewModel(),
    onLogClick: (QueryLogEntity) -> Unit
) {
    val recentLogs by viewModel.recentLogs.collectAsState(initial = emptyList())
    val weeklyStats by viewModel.weeklyStats.collectAsState(initial = emptyList())
    val topBlocked by viewModel.topBlockedDomains.collectAsState(emptyList())

    StatsContent(
        modifier = modifier,
        contentPadding = contentPadding,
        recentLogs = recentLogs,
        weeklyStats = weeklyStats,
        topBlocked = topBlocked,
        onLogClick = onLogClick,
        onBlockToggle = { log -> viewModel.toggleBlock(log.domain, log.isBlocked) },
        onAllowToggle = { log -> viewModel.toggleAllow(log.domain, !log.isBlocked) }
    )
}

@Composable
fun StatsContent(
    recentLogs: List<QueryLogEntity>,
    weeklyStats: List<StatsEntity>,
    topBlocked: List<TopDomain>,
    onLogClick: (QueryLogEntity) -> Unit,
    onBlockToggle: (QueryLogEntity) -> Unit,
    onAllowToggle: (QueryLogEntity) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
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
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Filtering Activity",
                        subtitle = "Comprehensive request analysis and blocking metrics",
                        modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                    )
                }
                
                item {
                    WeeklyStatsCard(weeklyStats)
                }

                if (topBlocked.isNotEmpty()) {
                    item {
                        TopBlockedSection(topBlocked, onDomainClick = { domain ->
                            onLogClick(QueryLogEntity(domain = domain, isBlocked = true, queryType = "A"))
                        })
                    }
                }

                 item {
                     Text(
                         text = "Recent Blocking Activity",
                         style = MaterialTheme.typography.titleLarge,
                         fontWeight = FontWeight.Black,
                         color = Color.White,
                         modifier = Modifier.padding(bottom = 8.dp)
                     )
                 }

                 if (recentLogs.isEmpty()) {
                     item {
                         Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                             Text("No filtering activity detected yet.", color = Color.White.copy(alpha = 0.4f))
                         }
                     }
                 } else {
                    items(recentLogs) { log ->
                        InteractiveQueryLogItem(
                            log = log,
                            onBlockToggle = { onBlockToggle(log) },
                            onAllowToggle = { onAllowToggle(log) },
                            onClick = { onLogClick(log) }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0E000A)
@Composable
fun StatsScreenPreview() {
    val mockLogs = listOf(
        QueryLogEntity(domain = "google-analytics.com", isBlocked = true, timestamp = System.currentTimeMillis()),
        QueryLogEntity(domain = "example.com", isBlocked = false, timestamp = System.currentTimeMillis() - 60000)
    )
    val mockStats = listOf(
        StatsEntity(date = LocalDate.now().toString(), blockedCount = 150, totalCount = 1150),
        StatsEntity(date = LocalDate.now().minusDays(1).toString(), blockedCount = 120, totalCount = 1020)
    )
    val mockTopBlocked = listOf(
        TopDomain(domain = "doubleclick.net", count = 450),
        TopDomain(domain = "facebook.com", count = 320)
    )

    MaterialTheme {
        StatsContent(
            recentLogs = mockLogs,
            weeklyStats = mockStats,
            topBlocked = mockTopBlocked,
            onLogClick = {},
            onBlockToggle = {},
            onAllowToggle = {}
        )
    }
}

@Composable
fun WeeklyStatsCard(weeklyStats: List<StatsEntity>) {
    GlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weekly Blocking Trends",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            StatusChip(text = "7-DAY HISTORY", color = MaterialTheme.colorScheme.primary)
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.height(160.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val days = (6 downTo 0).map { LocalDate.now().minusDays(it.toLong()) }
            val maxCount = (weeklyStats.maxOfOrNull { it.blockedCount } ?: 10).coerceAtLeast(1)
            
            days.forEach { date ->
                val stats = weeklyStats.find { it.date == date.toString() }
                val blockedCount = stats?.blockedCount ?: 0
                
                val animatedHeight by animateFloatAsState(
                    targetValue = (blockedCount.toFloat() / maxCount).coerceIn(0.05f, 1f),
                    animationSpec = tween(1200, easing = FastOutSlowInEasing),
                    label = "barHeight"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (blockedCount > 0) blockedCount.toString() else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .width(18.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(animatedHeight)
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .glowStroke(MaterialTheme.colorScheme.primary, CircleShape, glowRadius = 6.dp, alpha = 0.2f)
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = date.dayOfWeek.name.take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (date == LocalDate.now()) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                        fontWeight = if (date == LocalDate.now()) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveQueryLogItem(
    log: QueryLogEntity,
    onBlockToggle: () -> Unit,
    onAllowToggle: () -> Unit,
    onClick: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    val time = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(formatter)

    CommonListItem(
        title = log.domain,
        subtitle = "${if (log.isBlocked) "BLOCKED" else "ALLOWED"} • $time",
        icon = if (log.isBlocked) Icons.Default.Security else Icons.Default.Public,
        iconColor = if (log.isBlocked) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50).copy(alpha = 0.7f),
        onClick = onClick,
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Details",
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(20.dp)
            )
        }
    )
}
