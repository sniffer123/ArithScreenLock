package com.example.arithscreenlock

import android.content.Context
import android.content.SharedPreferences

class LockScreenPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "lock_screen_prefs"
        private const val KEY_QUESTION_COUNT = "question_count"
        private const val KEY_MAX_NUMBER = "max_number"
        private const val KEY_OPERATIONS = "operations"
        private const val KEY_AUTO_LOCK_DURATION = "auto_lock_duration"
        private const val KEY_IS_PARENT_MODE = "is_parent_mode"
        private const val KEY_PARENT_MODE_ACTIVATE_TIME = "parent_mode_activate_time"
        
        const val DEFAULT_QUESTION_COUNT = 3
        const val DEFAULT_MAX_NUMBER = 20
        const val DEFAULT_AUTO_LOCK_DURATION = 5 // 分钟
        const val PARENT_MODE_DURATION = 30 // 家长模式有效时长：30分钟
    }

    var questionCount: Int
        get() = prefs.getInt(KEY_QUESTION_COUNT, DEFAULT_QUESTION_COUNT)
        set(value) = prefs.edit().putInt(KEY_QUESTION_COUNT, value).apply()

    var maxNumber: Int
        get() = prefs.getInt(KEY_MAX_NUMBER, DEFAULT_MAX_NUMBER)
        set(value) = prefs.edit().putInt(KEY_MAX_NUMBER, value).apply()

    var enabledOperations: Set<String>
        get() = prefs.getStringSet(KEY_OPERATIONS, setOf("+", "-")) ?: setOf("+", "-")
        set(value) = prefs.edit().putStringSet(KEY_OPERATIONS, value).apply()

    var autoLockDuration: Int
        get() = prefs.getInt(KEY_AUTO_LOCK_DURATION, DEFAULT_AUTO_LOCK_DURATION)
        set(value) = prefs.edit().putInt(KEY_AUTO_LOCK_DURATION, value).apply()

    var isParentMode: Boolean
        get() {
            val isEnabled = prefs.getBoolean(KEY_IS_PARENT_MODE, false)
            if (isEnabled) {
                // 检查家长模式是否已过期
                val activateTime = prefs.getLong(KEY_PARENT_MODE_ACTIVATE_TIME, 0)
                val currentTime = System.currentTimeMillis()
                val elapsedMinutes = (currentTime - activateTime) / (1000 * 60)
                
                if (elapsedMinutes >= PARENT_MODE_DURATION) {
                    // 家长模式已过期，自动关闭
                    prefs.edit().putBoolean(KEY_IS_PARENT_MODE, false).apply()
                    return false
                }
            }
            return isEnabled
        }
        set(value) {
            prefs.edit().putBoolean(KEY_IS_PARENT_MODE, value).apply()
            if (value) {
                // 激活家长模式时记录当前时间
                prefs.edit().putLong(KEY_PARENT_MODE_ACTIVATE_TIME, System.currentTimeMillis()).apply()
            }
        }

    fun getOperationsList(): List<MathQuestionGenerator.Operation> {
        return enabledOperations.mapNotNull { symbol ->
            when (symbol) {
                "+" -> MathQuestionGenerator.Operation.ADDITION
                "-" -> MathQuestionGenerator.Operation.SUBTRACTION
                "×" -> MathQuestionGenerator.Operation.MULTIPLICATION
                "÷" -> MathQuestionGenerator.Operation.DIVISION
                else -> null
            }
        }
    }
}