package com.example.autoclicker

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

object PermissionState {
    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun isAccessibilityEnabled(context: Context): Boolean {
        return isEnabledInSecureSettings(context) || isEnabledInAccessibilityManager(context)
    }

    private fun isEnabledInSecureSettings(context: Context): Boolean {
        val expected = ComponentName(context, AutoClickAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        for (service in splitter) {
            val component = ComponentName.unflattenFromString(service)
            if (component == expected) return true
        }
        return false
    }

    private fun isEnabledInAccessibilityManager(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val services = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expected = ComponentName(context, AutoClickAccessibilityService::class.java)
        return services.any { info ->
            val fromResolveInfo = info.resolveInfo?.serviceInfo?.let { serviceInfo ->
                ComponentName(serviceInfo.packageName, serviceInfo.name) == expected
            } ?: false
            val fromId = ComponentName.unflattenFromString(info.id) == expected
            fromResolveInfo || fromId
        }
    }
}
