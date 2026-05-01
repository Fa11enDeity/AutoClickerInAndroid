package com.example.autoclicker

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class FloatingControlManager(
    private val context: Context,
    private val onStart: () -> Unit,
    private val onStop: () -> Unit
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var marker: View? = null
    private var panel: View? = null
    private var markerParams: WindowManager.LayoutParams? = null
    private var statusText: TextView? = null
    private var running = false

    fun show() {
        if (!PermissionState.canDrawOverlays(context)) return
        if (!running && marker == null) {
            addMarker()
        }
        if (panel == null) {
            panel = createPanel()
            val panelParams = overlayParams(width = WindowManager.LayoutParams.WRAP_CONTENT, height = WindowManager.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 24
                y = 120
            }
            windowManager.addView(panel, panelParams)
        }
        refreshStatus()
    }

    fun hide() {
        removeMarker()
        panel?.let { runCatching { windowManager.removeView(it) } }
        panel = null
        statusText = null
    }

    fun setRunning(running: Boolean) {
        this.running = running
        if (running) {
            removeMarker()
        } else if (panel != null && marker == null) {
            addMarker()
        }
        refreshStatus()
    }

    fun refreshStatus() {
        val settings = SettingsStore.load(context)
        statusText?.text = "(${settings.x}, ${settings.y}) ${settings.intervalMs}ms ${if (running) "ON" else "OFF"}"
    }

    private fun addMarker() {
        val settings = SettingsStore.load(context)
        marker = createMarker()
        markerParams = overlayParams(width = 72, height = 72).apply {
            gravity = Gravity.TOP or Gravity.START
            x = settings.x - 36
            y = settings.y - 36
        }
        windowManager.addView(marker, markerParams)
    }

    private fun removeMarker() {
        marker?.let { runCatching { windowManager.removeView(it) } }
        marker = null
        markerParams = null
    }

    private fun createMarker(): View {
        val view = TextView(context).apply {
            text = "+"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xCC146C5F.toInt())
        }
        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        view.setOnTouchListener { _, event ->
            val params = markerParams ?: return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = params.x
                    startY = params.y
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (event.rawX - downRawX).toInt()
                    params.y = startY + (event.rawY - downRawY).toInt()
                    windowManager.updateViewLayout(view, params)
                    SettingsStore.savePoint(context, params.x + 36, params.y + 36)
                    refreshStatus()
                    true
                }
                else -> true
            }
        }
        return view
    }

    private fun createPanel(): View {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xEE202124.toInt())
        }
        statusText = TextView(context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        val start = Button(context).apply {
            text = "Start"
            setOnClickListener {
                onStart()
            }
        }
        val stop = Button(context).apply {
            text = "Stop"
            setOnClickListener {
                onStop()
            }
        }
        row.addView(start)
        row.addView(stop)
        container.addView(statusText)
        container.addView(row)
        return container
    }

    private fun overlayParams(width: Int, height: Int): WindowManager.LayoutParams {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        return WindowManager.LayoutParams(
            width,
            height,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
    }
}
