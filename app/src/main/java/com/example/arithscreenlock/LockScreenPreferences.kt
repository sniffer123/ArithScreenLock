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
        
        const val DEFAULT_QUESTION_COUNT = 3
        const val DEFAULT_MAX_NUMBER = 20
        const val DEFAULT_AUTO_LOCK_DURATION = 5 // 分钟
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
        get() = prefs.getBoolean(KEY_IS_PARENT_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PARENT_MODE, value).apply()

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