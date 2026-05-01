package com.example.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.app.Activity

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var pointText: TextView
    private lateinit var intervalInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buildContent(): ScrollView {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
        }
        val title = TextView(this).apply {
            text = "Auto Clicker"
            textSize = 28f
            gravity = Gravity.START
        }
        statusText = TextView(this).apply {
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }
        pointText = TextView(this).apply {
            textSize = 16f
            setPadding(0, 0, 0, 12)
        }
        intervalInput = EditText(this).apply {
            hint = "Click interval in ms"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        root.addView(title)
        root.addView(statusText)
        root.addView(pointText)
        root.addView(label("Frequency"))
        root.addView(intervalInput)
        root.addView(button("Save frequency") {
            saveInterval()
        })
        root.addView(button("Grant overlay permission") {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        root.addView(button("Open accessibility settings") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        })
        root.addView(button("Show floating controls") {
            sendCommand(AutoClickAccessibilityService.ACTION_SHOW_CONTROLS)
        })
        root.addView(button("Start clicking") {
            saveInterval()
            sendCommand(AutoClickAccessibilityService.ACTION_START)
        })
        root.addView(button("Stop clicking") {
            sendCommand(AutoClickAccessibilityService.ACTION_STOP)
        })

        return ScrollView(this).apply {
            addView(root)
        }
    }

    private fun label(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 14f
        setPadding(0, 12, 0, 0)
    }

    private fun button(text: String, onClick: () -> Unit): Button = Button(this).apply {
        this.text = text
        setAllCaps(false)
        setOnClickListener { onClick() }
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 12
        }
    }

    private fun saveInterval() {
        val interval = intervalInput.text.toString().toLongOrNull()
        if (interval == null || interval < 100L) {
            Toast.makeText(this, "Use an interval of at least 100ms.", Toast.LENGTH_SHORT).show()
            return
        }
        SettingsStore.saveInterval(this, interval)
        refresh()
    }

    private fun sendCommand(action: String) {
        if (!PermissionState.isAccessibilityEnabled(this)) {
            Toast.makeText(this, "Enable the accessibility service first.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!PermissionState.canDrawOverlays(this) && action == AutoClickAccessibilityService.ACTION_SHOW_CONTROLS) {
            Toast.makeText(this, "Grant overlay permission first.", Toast.LENGTH_SHORT).show()
            return
        }
        sendBroadcast(Intent(action).setPackage(packageName))
    }

    private fun refresh() {
        val settings = SettingsStore.load(this)
        val overlay = if (PermissionState.canDrawOverlays(this)) "granted" else "missing"
        val accessibility = if (PermissionState.isAccessibilityEnabled(this)) "enabled" else "disabled"
        statusText.text = "Overlay: $overlay\nAccessibility: $accessibility"
        pointText.text = "Point: (${settings.x}, ${settings.y})"
        if (intervalInput.text.isNullOrBlank()) {
            intervalInput.setText(settings.intervalMs.toString())
        }
    }
}
