# 安全功能和防绕过机制

## 🔒 防绕过机制

### 1. Activity生命周期保护

#### 核心机制：
- **onPause()保护**: Activity被暂停时立即重启
- **onStop()保护**: Activity被停止时立即重启  
- **onDestroy()保护**: Activity被销毁时通过Service重新启动
- **onWindowFocusChanged()**: 失去焦点时自动重新获取

#### 实现细节：
```kotlin
override fun onPause() {
    super.onPause()
    if (!isUnlocked) {
        // 被暂停时立即重新启动
        restartLockScreen()
    }
}
```

### 2. 系统按键拦截

#### 禁用的按键：
- **返回键** (BACK): 完全禁用
- **Home键** (HOME): 拦截并忽略
- **菜单键** (MENU): 拦截并忽略  
- **搜索键** (SEARCH): 拦截并忽略
- **任务切换键** (APP_SWITCH): 拦截并忽略

#### 实现方式：
```kotlin
override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
    return when (keyCode) {
        KeyEvent.KEYCODE_HOME,
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_MENU -> true // 拦截并忽略
        else -> super.onKeyDown(keyCode, event)
    }
}
```

### 3. 窗口标志增强

#### 关键标志：
- `FLAG_SHOW_WHEN_LOCKED`: 在系统锁屏上显示
- `FLAG_DISMISS_KEYGUARD`: 解除系统锁屏
- `FLAG_KEEP_SCREEN_ON`: 保持屏幕常亮
- `FLAG_FULLSCREEN`: 全屏显示
- `FLAG_LAYOUT_IN_SCREEN`: 布局覆盖整个屏幕
- `FLAG_LAYOUT_NO_LIMITS`: 不受系统UI限制

### 4. 任务栈保护

#### Activity配置：
- `android:launchMode="singleInstance"`: 独立任务栈
- `android:excludeFromRecents="true"`: 不显示在最近任务
- `android:taskAffinity=""`: 独立任务关联
- `android:noHistory="false"`: 保持在历史记录中

### 5. 后台监控服务

#### 监控功能：
- **实时检查**: 每秒检查锁屏状态
- **自动恢复**: 检测到绕过时立即恢复
- **进程监控**: 监控应用前台状态
- **任务检查**: 检查当前运行任务

#### 监控逻辑：
```kotlin
private val monitorRunnable = object : Runnable {
    override fun run() {
        if (!isLockScreenActive()) {
            showLockScreen() // 立即恢复锁屏
        }
        handler.postDelayed(this, 1000) // 每秒检查
    }
}
```

### 6. 设备管理员增强

#### 管理员权限：
- `USES_POLICY_FORCE_LOCK`: 强制锁定设备
- `USES_POLICY_WATCH_LOGIN`: 监控登录尝试
- `USES_POLICY_RESET_PASSWORD`: 重置密码权限
- `USES_POLICY_WIPE_DATA`: 数据擦除权限

#### 安全增强：
- 设备管理员权限难以被用户意外禁用
- 提供额外的系统级锁定能力
- 增强应用的持久性和安全性

## 🛡️ 多层防护策略

### 第一层：UI层防护
- 禁用返回键和系统按键
- 全屏显示，覆盖状态栏和导航栏
- 拦截触摸事件和手势

### 第二层：Activity层防护  
- 生命周期监控和自动恢复
- 窗口焦点监控
- 任务栈保护

### 第三层：Service层防护
- 后台服务持续监控
- 定期检查和自动恢复
- 进程状态监控

### 第四层：系统层防护
- 设备管理员权限
- 系统级锁定能力
- 权限保护

## ⚠️ 已知限制

### 1. Root权限绕过
- Root用户可以强制停止应用
- Root用户可以修改系统设置
- **缓解措施**: 检测Root环境并警告

### 2. ADB调试绕过
- 开发者选项的ADB可以强制停止
- 可以通过ADB命令绕过
- **缓解措施**: 检测调试模式并警告

### 3. 系统级操作
- 强制重启设备可以绕过
- 安全模式启动可以绕过
- **缓解措施**: 开机自启动快速激活

### 4. 高级用户绕过
- 任务管理器强制关闭
- 系统设置中禁用应用
- **缓解措施**: 多层监控和快速恢复

## 🔧 测试建议

### 基础测试：
1. 按返回键 → 应该被拦截
2. 按Home键 → 应该被拦截或立即恢复
3. 下拉通知栏 → 应该立即恢复锁屏
4. 打开最近任务 → 应该立即恢复锁屏

### 高级测试：
1. 通过任务管理器关闭应用 → 应该快速重启
2. 在设置中强制停止 → 应该通过Service恢复
3. 禁用网络/飞行模式 → 锁屏应该继续工作
4. 低电量/省电模式 → 锁屏应该继续工作

### 压力测试：
1. 快速连续按返回键
2. 快速切换应用
3. 同时按多个系统键
4. 长时间运行稳定性测试

## 📝 安全建议

1. **定期更新**: 跟上Android系统更新，适配新的安全机制
2. **权限管理**: 合理使用权限，避免过度请求
3. **用户教育**: 向用户解释安全功能的重要性
4. **监控日志**: 记录绕过尝试，分析安全威胁
5. **备份机制**: 提供紧急解锁方式，避免完全锁死