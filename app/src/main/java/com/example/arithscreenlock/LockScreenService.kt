package com.example.arithscreenlock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class LockScreenService : Service() {

    private lateinit var preferences: LockScreenPreferences
    private val handler = Handler(Looper.getMainLooper())
    private var autoLockRunnable: Runnable? = null
    private var isServiceRunning = false

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    // 屏幕关闭时显示锁屏
                    if (isServiceRunning && !preferences.isParentMode) {
                        showLockScreen()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    // 屏幕开启时也显示锁屏
                    if (isServiceRunning && !preferences.isParentMode) {
                        showLockScreen()
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_START_SERVICE = "START_SERVICE"
        const val ACTION_STOP_SERVICE = "STOP_SERVICE"
        const val ACTION_START_AUTO_LOCK_TIMER = "START_AUTO_LOCK_TIMER"
        const val ACTION_SHOW_LOCK_SCREEN = "SHOW_LOCK_SCREEN"
        
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "lock_screen_service"
    }

    override fun onCreate() {
        super.onCreate()
        preferences = LockScreenPreferences(this)
        createNotificationChannel()
        registerScreenReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SERVICE -> {
                startForegroundService()
                isServiceRunning = true
            }
            ACTION_STOP_SERVICE -> {
                stopForegroundService()
                isServiceRunning = false
            }
            ACTION_START_AUTO_LOCK_TIMER -> {
                startAutoLockTimer()
            }
            ACTION_SHOW_LOCK_SCREEN -> {
                showLockScreen()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "锁屏服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "算术锁屏服务通知"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("算术锁屏")
            .setContentText("锁屏服务运行中")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun startAutoLockTimer() {
        // 取消之前的定时器
        autoLockRunnable?.let { handler.removeCallbacks(it) }
        
        val delayMillis = if (preferences.isParentMode) {
            // 家长模式：30分钟有效期
            LockScreenPreferences.PARENT_MODE_DURATION * 60 * 1000L
        } else {
            // 普通模式：用户设置的自动锁定时长
            preferences.autoLockDuration * 60 * 1000L
        }
        
        autoLockRunnable = Runnable {
            // 时间到后重置家长模式并显示锁屏
            preferences.isParentMode = false
            showLockScreen()
        }
        
        handler.postDelayed(autoLockRunnable!!, delayMillis)
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        autoLockRunnable?.let { handler.removeCallbacks(it) }
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}