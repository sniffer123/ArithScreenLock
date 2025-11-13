package com.example.arithscreenlock

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class DeviceAdminManager(private val context: Context) {
    
    private val devicePolicyManager: DevicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
    
    private val adminComponent: ComponentName by lazy {
        ComponentName(context, LockScreenDeviceAdminReceiver::class.java)
    }
    
    fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }
    
    fun requestDeviceAdminPermission(): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "启用设备管理员功能可以增强锁屏安全性，防止恶意绕过。"
        )
        return intent
    }
    
    fun lockDevice() {
        if (isDeviceAdminActive()) {
            devicePolicyManager.lockNow()
        }
    }
    
    fun canLockDevice(): Boolean {
        return isDeviceAdminActive()
    }
    
    fun removeAdmin() {
        if (isDeviceAdminActive()) {
            devicePolicyManager.removeActiveAdmin(adminComponent)
        }
    }
}