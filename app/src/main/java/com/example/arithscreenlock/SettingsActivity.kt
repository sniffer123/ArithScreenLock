package com.example.arithscreenlock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: LockScreenPreferences
    private lateinit var spinnerQuestionCount: Spinner
    private lateinit var spinnerNumberRange: Spinner
    private lateinit var spinnerAutoLockDuration: Spinner
    private lateinit var cbAddition: CheckBox
    private lateinit var cbSubtraction: CheckBox
    private lateinit var cbMultiplication: CheckBox
    private lateinit var cbDivision: CheckBox
    private lateinit var switchParentMode: Switch
    private lateinit var tvParentModeStatus: TextView
    
    // 每个运算类型的数字限制
    private lateinit var spinnerAdditionMax: Spinner
    private lateinit var spinnerSubtractionMax: Spinner
    private lateinit var spinnerMultiplicationMax: Spinner
    private lateinit var spinnerDivisionMax: Spinner
    
    // 题目类型
    private lateinit var spinnerQuestionType: Spinner
    private val handler = Handler(Looper.getMainLooper())
    private var updateStatusRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferences = LockScreenPreferences(this)
        initViews()
        loadSettings()
    }

    private fun initViews() {
        spinnerQuestionCount = findViewById(R.id.spinnerQuestionCount)
        spinnerNumberRange = findViewById(R.id.spinnerNumberRange)
        spinnerAutoLockDuration = findViewById(R.id.spinnerAutoLockDuration)
        cbAddition = findViewById(R.id.cbAddition)
        cbSubtraction = findViewById(R.id.cbSubtraction)
        cbMultiplication = findViewById(R.id.cbMultiplication)
        cbDivision = findViewById(R.id.cbDivision)
        switchParentMode = findViewById(R.id.switchParentMode)
        tvParentModeStatus = findViewById(R.id.tvParentModeStatus)
        
        spinnerAdditionMax = findViewById(R.id.spinnerAdditionMax)
        spinnerSubtractionMax = findViewById(R.id.spinnerSubtractionMax)
        spinnerMultiplicationMax = findViewById(R.id.spinnerMultiplicationMax)
        spinnerDivisionMax = findViewById(R.id.spinnerDivisionMax)
        spinnerQuestionType = findViewById(R.id.spinnerQuestionType)

        setupSpinners()

        findViewById<View>(R.id.btnSaveSettings).setOnClickListener {
            saveSettings()
        }

        // 家长模式开关监听器
        switchParentMode.setOnCheckedChangeListener { _, isChecked ->
            preferences.isParentMode = isChecked
            updateParentModeStatus()
        }
    }

    private fun setupSpinners() {
        // 题目数量选项
        val questionCountOptions = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
        val questionCountAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, questionCountOptions)
        questionCountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuestionCount.adapter = questionCountAdapter

        // 数字范围选项
        val numberRangeOptions = arrayOf("10以内", "20以内", "50以内", "100以内")
        val numberRangeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberRangeOptions)
        numberRangeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNumberRange.adapter = numberRangeAdapter

        // 自动锁定时长选项
        val autoLockOptions = arrayOf("1分钟", "2分钟", "3分钟", "5分钟", "10分钟", "15分钟", "30分钟", "60分钟")
        val autoLockAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, autoLockOptions)
        autoLockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAutoLockDuration.adapter = autoLockAdapter

        // 数字限制选项
        val numberOptions = arrayOf("5", "10", "20", "50", "100", "200")
        
        // 加法数字限制
        val additionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberOptions)
        additionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAdditionMax.adapter = additionAdapter

        // 减法数字限制
        val subtractionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberOptions)
        subtractionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubtractionMax.adapter = subtractionAdapter

        // 乘法数字限制（较小范围）
        val multiplicationOptions = arrayOf("5", "10", "12", "15", "20", "25")
        val multiplicationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, multiplicationOptions)
        multiplicationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMultiplicationMax.adapter = multiplicationAdapter

        // 除法数字限制
        val divisionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, numberOptions)
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDivisionMax.adapter = divisionAdapter

        // 题目类型选项
        val questionTypeOptions = arrayOf("填空题", "选择题")
        val questionTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, questionTypeOptions)
        questionTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerQuestionType.adapter = questionTypeAdapter
    }

    private fun loadSettings() {
        // 加载题目数量设置
        val questionCount = preferences.questionCount
        spinnerQuestionCount.setSelection(questionCount - 1)

        // 加载数字范围设置
        val maxNumber = preferences.maxNumber
        val rangeIndex = when (maxNumber) {
            10 -> 0
            20 -> 1
            50 -> 2
            100 -> 3
            else -> 1 // 默认20以内
        }
        spinnerNumberRange.setSelection(rangeIndex)

        // 加载运算类型设置
        val enabledOperations = preferences.enabledOperations
        cbAddition.isChecked = enabledOperations.contains("+")
        cbSubtraction.isChecked = enabledOperations.contains("-")
        cbMultiplication.isChecked = enabledOperations.contains("×")
        cbDivision.isChecked = enabledOperations.contains("÷")

        // 加载自动锁定时长设置
        val autoLockDuration = preferences.autoLockDuration
        val durationIndex = when (autoLockDuration) {
            1 -> 0
            2 -> 1
            3 -> 2
            5 -> 3
            10 -> 4
            15 -> 5
            30 -> 6
            60 -> 7
            else -> 3 // 默认5分钟
        }
        spinnerAutoLockDuration.setSelection(durationIndex)

        // 加载每个运算类型的数字限制设置
        loadNumberLimitSetting(spinnerAdditionMax, preferences.maxNumberAddition, arrayOf("5", "10", "20", "50", "100", "200"))
        loadNumberLimitSetting(spinnerSubtractionMax, preferences.maxNumberSubtraction, arrayOf("5", "10", "20", "50", "100", "200"))
        loadNumberLimitSetting(spinnerMultiplicationMax, preferences.maxNumberMultiplication, arrayOf("5", "10", "12", "15", "20", "25"))
        loadNumberLimitSetting(spinnerDivisionMax, preferences.maxNumberDivision, arrayOf("5", "10", "20", "50", "100", "200"))

        // 加载题目类型设置
        val questionTypeIndex = if (preferences.questionType == LockScreenPreferences.QUESTION_TYPE_MULTIPLE_CHOICE) 1 else 0
        spinnerQuestionType.setSelection(questionTypeIndex)

        // 加载家长模式设置
        switchParentMode.isChecked = preferences.isParentMode
        updateParentModeStatus()
        startStatusUpdateTimer()
    }

    private fun saveSettings() {
        // 保存题目数量
        preferences.questionCount = spinnerQuestionCount.selectedItemPosition + 1

        // 保存数字范围
        val maxNumber = when (spinnerNumberRange.selectedItemPosition) {
            0 -> 10
            1 -> 20
            2 -> 50
            3 -> 100
            else -> 20
        }
        preferences.maxNumber = maxNumber

        // 保存运算类型
        val operations = mutableSetOf<String>()
        if (cbAddition.isChecked) operations.add("+")
        if (cbSubtraction.isChecked) operations.add("-")
        if (cbMultiplication.isChecked) operations.add("×")
        if (cbDivision.isChecked) operations.add("÷")
        
        // 至少要有一种运算类型
        if (operations.isEmpty()) {
            operations.add("+")
            cbAddition.isChecked = true
            Toast.makeText(this, "至少需要选择一种运算类型", Toast.LENGTH_SHORT).show()
        }
        preferences.enabledOperations = operations

        // 保存自动锁定时长
        val autoLockDuration = when (spinnerAutoLockDuration.selectedItemPosition) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 5
            4 -> 10
            5 -> 15
            6 -> 30
            7 -> 60
            else -> 5
        }
        preferences.autoLockDuration = autoLockDuration

        // 保存每个运算类型的数字限制
        preferences.maxNumberAddition = getNumberFromSpinner(spinnerAdditionMax, arrayOf("5", "10", "20", "50", "100", "200"))
        preferences.maxNumberSubtraction = getNumberFromSpinner(spinnerSubtractionMax, arrayOf("5", "10", "20", "50", "100", "200"))
        preferences.maxNumberMultiplication = getNumberFromSpinner(spinnerMultiplicationMax, arrayOf("5", "10", "12", "15", "20", "25"))
        preferences.maxNumberDivision = getNumberFromSpinner(spinnerDivisionMax, arrayOf("5", "10", "20", "50", "100", "200"))

        // 保存题目类型
        preferences.questionType = if (spinnerQuestionType.selectedItemPosition == 1) {
            LockScreenPreferences.QUESTION_TYPE_MULTIPLE_CHOICE
        } else {
            LockScreenPreferences.QUESTION_TYPE_FILL_BLANK
        }

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun updateParentModeStatus() {
        if (preferences.isParentMode) {
            // 计算剩余时间
            val activateTime = preferences.getParentModeActivateTime()
            val currentTime = System.currentTimeMillis()
            val elapsedMinutes = (currentTime - activateTime) / (1000 * 60)
            val remainingMinutes = LockScreenPreferences.PARENT_MODE_DURATION - elapsedMinutes
            
            tvParentModeStatus.text = if (remainingMinutes > 0) {
                "已启用，剩余时间：${remainingMinutes}分钟"
            } else {
                "已过期"
            }
            tvParentModeStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        } else {
            tvParentModeStatus.text = "未启用"
            tvParentModeStatus.setTextColor(getColor(android.R.color.darker_gray))
        }
    }

    private fun startStatusUpdateTimer() {
        updateStatusRunnable = object : Runnable {
            override fun run() {
                if (preferences.isParentMode) {
                    updateParentModeStatus()
                    handler.postDelayed(this, 60000) // 每分钟更新一次
                }
            }
        }
        handler.post(updateStatusRunnable!!)
    }

    private fun loadNumberLimitSetting(spinner: Spinner, currentValue: Int, options: Array<String>) {
        val index = options.indexOfFirst { it.toInt() == currentValue }
        if (index != -1) {
            spinner.setSelection(index)
        } else {
            // 如果当前值不在选项中，选择最接近的
            val closestIndex = options.map { it.toInt() }.indexOfFirst { it >= currentValue }
            spinner.setSelection(if (closestIndex != -1) closestIndex else options.size - 1)
        }
    }

    private fun getNumberFromSpinner(spinner: Spinner, options: Array<String>): Int {
        return options[spinner.selectedItemPosition].toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateStatusRunnable?.let { handler.removeCallbacks(it) }
    }
}