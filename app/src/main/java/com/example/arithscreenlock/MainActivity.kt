package com.example.arithscreenlock

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var preferences: LockScreenPreferences
    private lateinit var tvServiceStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        preferences = LockScreenPreferences(this)
        deviceAdminManager = DeviceAdminManager(this)
        initViews()
        updateServiceStatus()
    }

    private fun initViews() {
        tvServiceStatus = findViewById(R.id.tvServiceStatus)

        findViewById<View>(R.id.btnStartService).setOnClickListener {
            when {
                !checkSystemAlertWindowPermission() -> {
                    requestSystemAlertWindowPermission()
                }
                !deviceAdminManager.isDeviceAdminActive() -> {
                    requestDeviceAdminPermission()
                }
                else -> {
                    startLockScreenService()
                }
            }
        }

        findViewById<View>(R.id.btnStopService).setOnClickListener {
            stopLockScreenService()
        }

        findViewById<View>(R.id.btnSettings).setOnClickListener {
            showPasswordDialog()
        }
    }

    private fun checkSystemAlertWindowPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

    private fun requestSystemAlertWindowPermission() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage("应用需要系统窗口权限才能显示锁屏界面")
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun requestDeviceAdminPermission() {
        AlertDialog.Builder(this)
            .setTitle("设备管理员权限")
            .setMessage("启用设备管理员功能可以增强锁屏安全性，防止恶意绕过。建议启用以获得更好的安全保护。")
            .setPositiveButton("启用") { _, _ ->
                val intent = deviceAdminManager.requestDeviceAdminPermission()
                startActivityForResult(intent, REQUEST_CODE_DEVICE_ADMIN)
            }
            .setNegativeButton("跳过") { _, _ ->
                startLockScreenService()
            }
            .show()
    }

    private fun startLockScreenService() {
        val intent = Intent(this, LockScreenService::class.java)
        intent.action = LockScreenService.ACTION_START_SERVICE
        startService(intent)
        updateServiceStatus()
    }

    private fun stopLockScreenService() {
        val intent = Intent(this, LockScreenService::class.java)
        intent.action = LockScreenService.ACTION_STOP_SERVICE
        startService(intent)
        
        // 重置家长模式
        preferences.isParentMode = false
        updateServiceStatus()
    }

    private fun showPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)

        AlertDialog.Builder(this)
            .setTitle("控制台验证")
            .setView(dialogView)
            .setPositiveButton("确认") { _, _ ->
                val password = etPassword.text.toString()
                if (password == "123456") {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun updateServiceStatus() {
        // 这里可以检查服务是否正在运行
        // 为了简化，我们假设服务状态基于是否启动过
        tvServiceStatus.text = getString(R.string.service_running)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_DEVICE_ADMIN -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "设备管理员权限已启用", Toast.LENGTH_SHORT).show()
                    startLockScreenService()
                } else {
                    Toast.makeText(this, "设备管理员权限被拒绝，安全性可能降低", Toast.LENGTH_LONG).show()
                    startLockScreenService()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
}