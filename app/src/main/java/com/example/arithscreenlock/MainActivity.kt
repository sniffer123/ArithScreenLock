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
        initViews()
        updateServiceStatus()
    }

    private fun initViews() {
        tvServiceStatus = findViewById(R.id.tvServiceStatus)

        findViewById<View>(R.id.btnStartService).setOnClickListener {
            if (checkSystemAlertWindowPermission()) {
                startLockScreenService()
            } else {
                requestSystemAlertWindowPermission()
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

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
    }
}