package com.blockick.app.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.blockick.app.R
import com.blockick.app.data.db.dao.StatsDao
import com.blockick.app.data.preferences.AppPreferences
import com.blockick.app.ui.MainActivity
import com.blockick.app.vpn.VpnController
import com.blockick.app.vpn.VpnStatus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class VpnWidgetProvider : AppWidgetProvider() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun vpnController(): VpnController
        fun statsDao(): StatsDao
        fun appPreferences(): AppPreferences
    }

    override fun onReceive(context: Context, intent: Intent) {
        // If it's our custom toggle action, handle it first
        if (intent.action == ACTION_TOGGLE_VPN) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    handleToggle(context)
                    updateAllWidgets(context)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        // For other actions (like ACTION_APPWIDGET_UPDATE), let the provider handle it
        // but we override onUpdate to use a coroutine with goAsync if needed.
        super.onReceive(context, intent)
    }

    private suspend fun handleToggle(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val controller = entryPoint.vpnController()
        
        Log.d("VpnWidget", "Widget toggle clicked. Current status: ${controller.status.value}")
        
        when (controller.status.value) {
            VpnStatus.RUNNING, VpnStatus.STARTING -> {
                controller.stop()
            }
            VpnStatus.STOPPED -> {
                if (controller.needsPermission()) {
                    val mainIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(mainIntent)
                } else {
                    controller.start()
                }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                for (appWidgetId in appWidgetIds) {
                    updateWidgetSync(context, appWidgetManager, appWidgetId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun updateWidgetSync(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val controller = entryPoint.vpnController()
        val statsDao = entryPoint.statsDao()
        val prefs = entryPoint.appPreferences()

        val views = RemoteViews(context.packageName, R.layout.vpn_widget)
        
        // 1. Determine status (with preference fallback)
        var status = controller.status.value
        if (status == VpnStatus.STOPPED) {
            val isEnabled = prefs.vpnEnabled.first()
            if (isEnabled) {
                status = VpnStatus.STARTING
            }
        }

        val isRunning = status == VpnStatus.RUNNING
        val isStarting = status == VpnStatus.STARTING
        
        // 2. Update UI based on status
        val statusText = when (status) {
            VpnStatus.RUNNING -> "Protection Active"
            VpnStatus.STARTING -> "Starting..."
            VpnStatus.STOPPED -> "Protection Disabled"
        }
        
        val statusColor = when (status) {
            VpnStatus.RUNNING -> 0xFF00E5FF.toInt() // Cyan
            VpnStatus.STARTING -> 0xFFFFCA28.toInt() // Amber
            VpnStatus.STOPPED -> 0xFF808080.toInt() // Gray
        }
        
        views.setTextViewText(R.id.widget_status_text, statusText)
        views.setTextColor(R.id.widget_status_text, if (isRunning || isStarting) android.graphics.Color.WHITE else android.graphics.Color.GRAY)
        
        views.setImageViewResource(R.id.widget_status_indicator, R.drawable.shape_circle)
        views.setInt(R.id.widget_status_indicator, "setColorFilter", statusColor)
        
        views.setTextViewText(R.id.widget_toggle_button, if (isRunning || isStarting) "STOP" else "START")

        // 3. Fetch stats
        try {
            val today = LocalDate.now().toString()
            val stats = statsDao.getStatsForDate(today)
            val blockedToday = stats?.blockedCount ?: 0
            val totalBlocked = statsDao.getTotalBlocked().first() ?: 0
            
            views.setTextViewText(R.id.widget_stats_text, "$blockedToday ads blocked today")
            views.setTextViewText(R.id.widget_total_text, "Total: $totalBlocked blocked")
        } catch (e: Exception) {
            Log.e("VpnWidget", "Error fetching stats", e)
        }

        // 4. Setup Intents
        val toggleIntent = Intent(context, VpnWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_VPN
        }
        val pendingToggle = PendingIntent.getBroadcast(
            context, 0, toggleIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_toggle_button, pendingToggle)

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingMain = PendingIntent.getActivity(
            context, 1, mainIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingMain)

        // 5. Apply update
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, VpnWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(componentName)
        onUpdate(context, appWidgetManager, ids)
    }

    companion object {
        private const val ACTION_TOGGLE_VPN = "com.blockick.app.ACTION_TOGGLE_VPN"
    }
}
