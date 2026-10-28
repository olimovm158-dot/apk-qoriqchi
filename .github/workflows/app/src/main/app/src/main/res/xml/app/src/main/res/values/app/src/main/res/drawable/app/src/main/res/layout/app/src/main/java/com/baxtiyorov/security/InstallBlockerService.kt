package com.baxtiyorov.security

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class InstallBlockerService : AccessibilityService() {

    private val installerPackages = listOf(
        "com.android.packageinstaller",
        "com.google.android.packageinstaller",
        "com.samsung.android.packageinstaller",
        "com.miui.packageinstaller"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.packageName == null) return

        val pkgName = event.packageName.toString()

        if (installerPackages.contains(pkgName)) {
            val root = rootInActiveWindow ?: return
            blockInstallation(root)
        }
    }

    private fun blockInstallation(node: AccessibilityNodeInfo) {
        val cancelWords = listOf("Bekor qilish", "Отмена", "Cancel")

        for (word in cancelWords) {
            val foundList = node.findAccessibilityNodeInfosByText(word)
            for (item in foundList) {
                if (item.isClickable) {
                    item.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Toast.makeText(this, "Apk Qo'riqchi: Xavfli o'rnatish to'xtatildi!", Toast.LENGTH_LONG).show()
                    return
                }
            }
        }
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    override fun onInterrupt() {}
}
