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
        
        // 每个运算类型的数字限制
        private const val KEY_MAX_NUMBER_ADDITION = "max_number_addition"
        private const val KEY_MAX_NUMBER_SUBTRACTION = "max_number_subtraction"
        private const val KEY_MAX_NUMBER_MULTIPLICATION = "max_number_multiplication"
        private const val KEY_MAX_NUMBER_DIVISION = "max_number_division"
        
        // 题目类型设置
        private const val KEY_QUESTION_TYPE = "question_type" // "fill_blank" 或 "multiple_choice"
        
        const val DEFAULT_QUESTION_COUNT = 4
        const val DEFAULT_MAX_NUMBER = 50
        const val DEFAULT_AUTO_LOCK_DURATION = 5 // 分钟
        const val PARENT_MODE_DURATION = 30 // 家长模式有效时长：30分钟
        
        const val QUESTION_TYPE_FILL_BLANK = "fill_blank"
        const val QUESTION_TYPE_MULTIPLE_CHOICE = "multiple_choice"
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

    fun getParentModeActivateTime(): Long {
        return prefs.getLong(KEY_PARENT_MODE_ACTIVATE_TIME, 0)
    }

    // 每个运算类型的数字限制
    var maxNumberAddition: Int
        get() = prefs.getInt(KEY_MAX_NUMBER_ADDITION, DEFAULT_MAX_NUMBER)
        set(value) = prefs.edit().putInt(KEY_MAX_NUMBER_ADDITION, value).apply()

    var maxNumberSubtraction: Int
        get() = prefs.getInt(KEY_MAX_NUMBER_SUBTRACTION, DEFAULT_MAX_NUMBER)
        set(value) = prefs.edit().putInt(KEY_MAX_NUMBER_SUBTRACTION, value).apply()

    var maxNumberMultiplication: Int
        get() = prefs.getInt(KEY_MAX_NUMBER_MULTIPLICATION, 10) // 乘法默认较小
        set(value) = prefs.edit().putInt(KEY_MAX_NUMBER_MULTIPLICATION, value).apply()

    var maxNumberDivision: Int
        get() = prefs.getInt(KEY_MAX_NUMBER_DIVISION, 100) // 除法默认较大
        set(value) = prefs.edit().putInt(KEY_MAX_NUMBER_DIVISION, value).apply()

    // 题目类型设置
    var questionType: String
        get() = prefs.getString(KEY_QUESTION_TYPE, QUESTION_TYPE_FILL_BLANK) ?: QUESTION_TYPE_FILL_BLANK
        set(value) = prefs.edit().putString(KEY_QUESTION_TYPE, value).apply()

    fun getMaxNumberForOperation(operation: MathQuestionGenerator.Operation): Int {
        return when (operation) {
            MathQuestionGenerator.Operation.ADDITION -> maxNumberAddition
            MathQuestionGenerator.Operation.SUBTRACTION -> maxNumberSubtraction
            MathQuestionGenerator.Operation.MULTIPLICATION -> maxNumberMultiplication
            MathQuestionGenerator.Operation.DIVISION -> maxNumberDivision
        }
    }
}