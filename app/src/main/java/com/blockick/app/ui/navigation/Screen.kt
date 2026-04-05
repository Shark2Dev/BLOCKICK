package com.blockick.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String, 
    val label: String, 
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Activity : Screen("activity", "Activity", Icons.Filled.ListAlt, Icons.Outlined.ListAlt)
    object Filters : Screen("filters", "Protection", Icons.Filled.Shield, Icons.Outlined.Shield)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    
    // Sub-screens
    object Blocklists : Screen("blocklists", "Lists", Icons.Filled.Block, Icons.Outlined.Block)
    object ExcludedApps : Screen("excluded_apps", "App Exclusion", Icons.Filled.AppBlocking, Icons.Outlined.AppBlocking)
    object CustomBlocklists : Screen("custom_blocklists", "Custom Lists", Icons.Filled.AddLink, Icons.Outlined.AddLink)
    object PersonalRules : Screen("personal_rules", "Personal Rules", Icons.Filled.PlaylistAddCheck, Icons.Outlined.PlaylistAddCheck)
    object NetworkAudit : Screen("network_audit", "Security Audit", Icons.Filled.GppGood, Icons.Outlined.GppGood)
    
    object ActivityDetail : Screen(
        "activity_detail/{domain}", 
        "Detail", 
        Icons.Filled.BarChart, 
        Icons.Outlined.BarChart
    ) {
        fun createRoute(domain: String) = "activity_detail/$domain"
    }
}

val navItems = listOf(
    Screen.Home,
    Screen.Activity,
    Screen.Filters,
    Screen.Settings
)

