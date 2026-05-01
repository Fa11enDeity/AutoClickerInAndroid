package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.accessibility.AccessibilityEvent

class AutoClickAccessibilityService : AccessibilityService() {
    private lateinit var clickController: ClickController
    private var floatingControlManager: FloatingControlManager? = null

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_START -> startClicking()
                ACTION_STOP -> stopClicking()
                ACTION_SHOW_CONTROLS -> floatingControlManager?.show()
                ACTION_HIDE_CONTROLS -> floatingControlManager?.hide()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        clickController = ClickController(this)
        floatingControlManager = FloatingControlManager(
            context = this,
            onStart = { startClicking() },
            onStop = { stopClicking() }
        )
        val filter = IntentFilter().apply {
            addAction(ACTION_START)
            addAction(ACTION_STOP)
            addAction(ACTION_SHOW_CONTROLS)
            addAction(ACTION_HIDE_CONTROLS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(commandReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(commandReceiver, filter)
        }
        floatingControlManager?.show()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() {
        stopClicking()
    }

    override fun onDestroy() {
        stopClicking()
        floatingControlManager?.hide()
        runCatching { unregisterReceiver(commandReceiver) }
        if (instance === this) instance = null
        super.onDestroy()
    }

    private fun startClicking() {
        clickController.start()
        floatingControlManager?.refreshStatus(true)
    }

    private fun stopClicking() {
        if (::clickController.isInitialized) {
            clickController.stop()
        }
        floatingControlManager?.refreshStatus(false)
    }

    companion object {
        const val ACTION_START = "com.example.autoclicker.action.START"
        const val ACTION_STOP = "com.example.autoclicker.action.STOP"
        const val ACTION_SHOW_CONTROLS = "com.example.autoclicker.action.SHOW_CONTROLS"
        const val ACTION_HIDE_CONTROLS = "com.example.autoclicker.action.HIDE_CONTROLS"

        @Volatile
        var instance: AutoClickAccessibilityService? = null
            private set
    }
}
