package com.example.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper

class ClickController(private val service: AccessibilityService) {
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
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 60L))
            .build()
        service.dispatchGesture(gesture, null, null)
    }
}
