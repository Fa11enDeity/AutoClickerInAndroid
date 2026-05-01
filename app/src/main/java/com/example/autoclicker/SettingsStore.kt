package com.example.autoclicker

import android.content.Context

data class ClickSettings(
    val x: Int,
    val y: Int,
    val intervalMs: Long
)

object SettingsStore {
    private const val PREFS = "auto_clicker_settings"
    private const val KEY_X = "x"
    private const val KEY_Y = "y"
    private const val KEY_INTERVAL = "interval_ms"

    fun load(context: Context): ClickSettings {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return ClickSettings(
            x = prefs.getInt(KEY_X, 540),
            y = prefs.getInt(KEY_Y, 960),
            intervalMs = prefs.getLong(KEY_INTERVAL, 500L).coerceAtLeast(100L)
        )
    }

    fun savePoint(context: Context, x: Int, y: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_X, x)
            .putInt(KEY_Y, y)
            .apply()
    }

    fun saveInterval(context: Context, intervalMs: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_INTERVAL, intervalMs.coerceAtLeast(100L))
            .apply()
    }
}
