package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log

class ClickController(private val service: AccessibilityService) {
    private companion object {
        const val TAG = "ClickController"
    }

    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    private val clickLoop = object : Runnable {
        override fun run() {
            if (!running) return
            val settings = SettingsStore.load(service)
            tap(settings.x, settings.y)
            handler.postDelayed(this, settings.intervalMs)
        }
    }

    fun start() {
        if (running) return
        running = true
        handler.post(clickLoop)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(clickLoop)
    }

    fun isRunning(): Boolean = running

    private fun tap(x: Int, y: Int) {
        val path = Path().apply {
            moveTo(x.toFloat(), y.toFloat())
            lineTo((x + 1).toFloat(), (y + 1).toFloat())
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 80L))
            .build()
        val accepted = service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCancelled(gestureDescription: GestureDescription?) {
                    Log.w(TAG, "Tap gesture cancelled at ($x, $y)")
                }
            },
            null
        )
        if (!accepted) {
            Log.w(TAG, "Tap gesture rejected at ($x, $y)")
        }
    }
}
