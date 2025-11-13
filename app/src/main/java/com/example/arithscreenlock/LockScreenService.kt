package com.example.arithscreenlock

import android.app.ActivityManager
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
    
    // 监控任务，定期检查锁屏状态
    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (isServiceRunning && !preferences.isParentMode) {
                checkLockScreenStatus()
            }
            handler.postDelayed(this, 1000) // 每秒检查一次
        }
    }

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
        const val ACTION_START_MONITORING = "START_MONITORING"
        const val ACTION_STOP_MONITORING = "STOP_MONITORING"
        
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
                startMonitoring()
            }
            ACTION_STOP_SERVICE -> {
                stopForegroundService()
                isServiceRunning = false
                stopMonitoring()
            }
            ACTION_START_AUTO_LOCK_TIMER -> {
                startAutoLockTimer()
            }
            ACTION_SHOW_LOCK_SCREEN -> {
                showLockScreen()
            }
            ACTION_START_MONITORING -> {
                startMonitoring()
            }
            ACTION_STOP_MONITORING -> {
                stopMonitoring()
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

    private fun startMonitoring() {
        handler.post(monitorRunnable)
    }
    
    private fun stopMonitoring() {
        handler.removeCallbacks(monitorRunnable)
    }
    
    private fun checkLockScreenStatus() {
        if (!isLockScreenActive()) {
            // 如果锁屏不在前台，重新启动
            showLockScreen()
        }
    }
    
    private fun isLockScreenActive(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Android 5.0+ 使用不同的方法
                val runningProcesses = activityManager.runningAppProcesses
                runningProcesses?.any { process ->
                    process.processName == packageName && 
                    process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                } ?: false
            } else {
                @Suppress("DEPRECATION")
                val tasks = activityManager.getRunningTasks(1)
                tasks.isNotEmpty() && 
                tasks[0].topActivity?.className == LockScreenActivity::class.java.name
            }
        } catch (e: SecurityException) {
            // 如果没有权限，假设锁屏不活跃
            false
        }
        return runningTasks
    }

    private fun showLockScreen() {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                   Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                   Intent.FLAG_ACTIVITY_SINGLE_TOP or
                   Intent.FLAG_ACTIVITY_NO_ANIMATION
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenReceiver)
        autoLockRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacks(monitorRunnable)
        isServiceRunning = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}