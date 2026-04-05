package com.blockick.app.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blockick.app.ui.navigation.Screen
import com.blockick.app.ui.navigation.navItems
import com.blockick.app.ui.components.noiseBackground
import com.blockick.app.ui.screens.home.HomeScreen
import com.blockick.app.ui.screens.filters.FiltersScreen
import com.blockick.app.ui.screens.filters.PersonalRulesScreen
import com.blockick.app.ui.screens.filters.CustomBlocklistsScreen
import com.blockick.app.ui.screens.stats.StatsScreen
import com.blockick.app.ui.screens.activity.ActivityDetailScreen
import com.blockick.app.ui.screens.settings.SettingsScreen
import com.blockick.app.ui.screens.settings.ExcludedAppsScreen
import com.blockick.app.ui.screens.settings.NetworkAuditScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(snackbarHostState) {
        com.blockick.app.ui.components.GlobalSnackbar.setHostState(snackbarHostState)
    }

    val showBottomBar = navItems.any { it.route == currentDestination?.route }

    val cyanGlow = Color(0xFF00E5FF)
    val purpleGlow = Color(0xFF2979FF)
    val baseBackground = Color(0xFF0E000A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(baseBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyanGlow.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.1f),
                        radius = size.width * 0.8f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.1f),
                    radius = size.width * 0.8f
                )

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(purpleGlow.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.8f),
                        radius = size.width * 1.0f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.8f),
                    radius = size.width * 1.0f
                )
            }
            .noiseBackground(alpha = 0.07f)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            // Allow scaffold to manage system insets for innerPadding
            contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top),
            snackbarHost = { 
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (showBottomBar) 80.dp else 0.dp)
                ) 
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    // Transparent surface with glass effect to avoid "weird shape" blocking content
                    Surface(
                        color = Color.Black.copy(alpha = 0.92f),
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            // Crisp divider to define the boundary
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(Color.White.copy(alpha = 0.15f))
                            )
                            NavigationBar(
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                navItems.forEach { screen ->
                                    val selected = currentDestination?.route == screen.route
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon, 
                                                contentDescription = screen.label,
                                                modifier = Modifier.size(24.dp)
                                            ) 
                                        },
                                        label = { 
                                            Text(
                                                text = screen.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
                                                fontSize = 11.sp
                                            ) 
                                        },
                                        selected = selected,
                                        onClick = {
                                            if (!selected) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            indicatorColor = Color.Transparent,
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = Color.White.copy(alpha = 0.3f),
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            unselectedTextColor = Color.White.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) { 
                    HomeScreen(
                        contentPadding = innerPadding,
                        onLogClick = { log ->
                            navController.navigate(Screen.ActivityDetail.createRoute(log.domain))
                        }
                    ) 
                }
                
                composable(Screen.Activity.route) { 
                    StatsScreen(
                        contentPadding = innerPadding,
                        onLogClick = { log ->
                            navController.navigate(Screen.ActivityDetail.createRoute(log.domain))
                        }
                    ) 
                }

                composable(Screen.Filters.route) {
                    FiltersScreen(
                        contentPadding = innerPadding,
                        onNavigateToCustomLists = { navController.navigate(Screen.CustomBlocklists.route) },
                        onNavigateToExclusion = { navController.navigate(Screen.ExcludedApps.route) },
                        onNavigateToPersonalRules = { navController.navigate(Screen.PersonalRules.route) }
                    )
                }
                
                composable(Screen.Settings.route) { 
                    SettingsScreen(
                        contentPadding = innerPadding,
                        onNavigateToAudit = {
                            navController.navigate(Screen.NetworkAudit.route)
                        }
                    ) 
                }

                // Sub-screens
                composable(Screen.PersonalRules.route) {
                    PersonalRulesScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.CustomBlocklists.route) {
                    CustomBlocklistsScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.ExcludedApps.route) {
                    ExcludedAppsScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.NetworkAudit.route) {
                    NetworkAuditScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.ActivityDetail.route) {
                    ActivityDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

