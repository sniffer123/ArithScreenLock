package com.example.arithscreenlock

import android.os.Bundle
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

        setupSpinners()

        findViewById<View>(R.id.btnSaveSettings).setOnClickListener {
            saveSettings()
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

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }
}