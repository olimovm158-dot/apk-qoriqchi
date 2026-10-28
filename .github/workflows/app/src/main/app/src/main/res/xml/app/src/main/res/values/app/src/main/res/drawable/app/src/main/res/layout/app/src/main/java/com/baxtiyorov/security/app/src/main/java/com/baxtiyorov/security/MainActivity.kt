package com.baxtiyorov.security

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatusTitle: TextView
    private lateinit var tvStatusDesc: TextView
    private lateinit var btnEnable: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusTitle = findViewById(R.id.tvStatusTitle)
        tvStatusDesc = findViewById(R.id.tvStatusDesc)
        btnEnable = findViewById(R.id.btnEnable)
        val btnScan = findViewById<Button>(R.id.btnScan)

        btnEnable.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }

        btnScan.setOnClickListener {
            scanSuspiciousApps()
        }
    }

    override fun onResume() {
        super.onResume()
        updateProtectionStatus()
    }

    private fun updateProtectionStatus() {
        val isEnabled = isAccessibilityServiceEnabled(this, InstallBlockerService::class.java)
        if (isEnabled) {
            tvStatusTitle.text = "Himoya: Faol"
            tvStatusTitle.setTextColor(Color.parseColor("#16A34A"))
            tvStatusDesc.text = "Telefoningiz shubhali APK lardan himoyalangan"
            btnEnable.text = "Sozlamalarni tekshirish"
        } else {
            tvStatusTitle.text = "Himoya: O'chirilgan"
            tvStatusTitle.setTextColor(Color.parseColor("#DC2626"))
            tvStatusDesc.text = "Himoyani yoqish uchun ruxsat bering"
            btnEnable.text = "Himoyani yoqish"
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(service.name)
    }

    private fun scanSuspiciousApps() {
        val pm = packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val riskyApps = mutableListOf<String>()

        for (pkg in packages) {
            val perms = pkg.requestedPermissions ?: continue
            if (perms.contains("android.permission.READ_SMS") &&
                perms.contains("android.permission.RECEIVE_SMS") &&
                (pkg.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
            ) {
                val appName = pkg.applicationInfo.loadLabel(pm).toString()
                riskyApps.add("$appName (${pkg.packageName})")
            }
        }

        val message = if (riskyApps.isEmpty()) {
            "SMS laringizni yashirincha o'qishi mumkin bo'lgan xavfli dasturlar topilmadi!"
        } else {
            "Diqqat! SMS o'qish huquqiga ega bo'lgan quyidagi dasturlar topildi:\n\n" + riskyApps.joinToString("\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Tekshiruv yakunlandi")
            .setMessage(message)
            .setPositiveButton("Tushundim", null)
            .show()
    }
}
