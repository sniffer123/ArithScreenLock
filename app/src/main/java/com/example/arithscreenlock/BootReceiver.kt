package com.example.arithscreenlock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 开机自启动锁屏服务
                val serviceIntent = Intent(context, LockScreenService::class.java)
                serviceIntent.action = LockScreenService.ACTION_START_SERVICE
                context.startService(serviceIntent)
            }
            Intent.ACTION_SCREEN_OFF -> {
                // 屏幕关闭时通知服务
                val serviceIntent = Intent(context, LockScreenService::class.java)
                serviceIntent.action = LockScreenService.ACTION_SHOW_LOCK_SCREEN
                context.startService(serviceIntent)
            }
            Intent.ACTION_SCREEN_ON -> {
                // 屏幕开启时通知服务
                val serviceIntent = Intent(context, LockScreenService::class.java)
                serviceIntent.action = LockScreenService.ACTION_SHOW_LOCK_SCREEN
                context.startService(serviceIntent)
            }
        }
    }
}