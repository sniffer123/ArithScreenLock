package com.example.arithscreenlock

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LockScreenActivity : AppCompatActivity() {

    private lateinit var preferences: LockScreenPreferences
    private lateinit var layoutQuestions: LinearLayout
    private lateinit var tvResult: TextView
    private val questions = mutableListOf<MathQuestion>()
    private val answerViews = mutableListOf<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置为全屏并显示在锁屏上方
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        setContentView(R.layout.activity_lock_screen)

        preferences = LockScreenPreferences(this)
        
        // 如果是家长模式，直接解锁
        if (preferences.isParentMode) {
            unlockScreen()
            return
        }

        initViews()
        generateQuestions()
    }

    private fun initViews() {
        layoutQuestions = findViewById(R.id.layoutQuestions)
        tvResult = findViewById(R.id.tvResult)
        
        findViewById<View>(R.id.btnSubmit).setOnClickListener {
            checkAnswers()
        }
        
        findViewById<View>(R.id.btnParentMode).setOnClickListener {
            showParentModeDialog()
        }
    }

    private fun generateQuestions() {
        questions.clear()
        answerViews.clear()
        layoutQuestions.removeAllViews()

        val operations = preferences.getOperationsList()
        if (operations.isEmpty()) {
            // 如果没有启用任何运算，默认使用加法
            questions.addAll(
                MathQuestionGenerator.generateQuestions(
                    preferences.questionCount,
                    preferences.maxNumber,
                    listOf(MathQuestionGenerator.Operation.ADDITION)
                )
            )
        } else {
            questions.addAll(
                MathQuestionGenerator.generateQuestions(
                    preferences.questionCount,
                    preferences.maxNumber,
                    operations
                )
            )
        }

        // 为每个题目创建视图
        questions.forEach { question ->
            val questionView = LayoutInflater.from(this)
                .inflate(R.layout.item_math_question, layoutQuestions, false)
            
            val tvQuestion = questionView.findViewById<TextView>(R.id.tvQuestion)
            val etAnswer = questionView.findViewById<EditText>(R.id.etAnswer)
            
            tvQuestion.text = question.getQuestionText()
            answerViews.add(etAnswer)
            
            layoutQuestions.addView(questionView)
        }
    }

    private fun checkAnswers() {
        var correctCount = 0
        
        for (i in questions.indices) {
            val userAnswer = answerViews[i].text.toString().toIntOrNull()
            if (userAnswer == questions[i].answer) {
                correctCount++
            }
        }
        
        if (correctCount == questions.size) {
            tvResult.apply {
                text = getString(R.string.correct)
                setTextColor(getColor(android.R.color.holo_green_light))
                visibility = View.VISIBLE
            }
            
            // 延迟解锁
            tvResult.postDelayed({
                unlockScreen()
            }, 1000)
        } else {
            tvResult.apply {
                text = getString(R.string.incorrect)
                setTextColor(getColor(android.R.color.holo_red_light))
                visibility = View.VISIBLE
            }
            
            // 重新生成题目
            tvResult.postDelayed({
                generateQuestions()
                tvResult.visibility = View.GONE
            }, 2000)
        }
    }

    private fun showParentModeDialog() {
        val hintCode = generateHintCode()
        val correctPin = calculatePin(hintCode)
        
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pin_code, null)
        val tvHint = dialogView.findViewById<TextView>(R.id.tvHintCode)
        val etPin = dialogView.findViewById<EditText>(R.id.etPinCode)
        
        tvHint.text = getString(R.string.hint_code, hintCode)
        
        AlertDialog.Builder(this)
            .setTitle(R.string.pin_code_title)
            .setView(dialogView)
            .setPositiveButton("确认") { _, _ ->
                val userPin = etPin.text.toString()
                if (userPin == correctPin) {
                    preferences.isParentMode = true
                    unlockScreen()
                } else {
                    Toast.makeText(this, "PIN码错误", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun generateHintCode(): String {
        // 生成4位随机数字，第一位不能超过4
        val firstDigit = Random.nextInt(1, 5)
        val digits = mutableListOf(firstDigit)
        repeat(3) {
            digits.add(Random.nextInt(0, 10))
        }
        return digits.joinToString("")
    }

    private fun calculatePin(hintCode: String): String {
        val digits = hintCode.map { it.toString().toInt() }
        val startIndex = digits[0] - 1 // 从第N位开始（0-based index）
        
        val result = mutableListOf<Int>()
        for (i in 0 until 4) {
            result.add(digits[(startIndex + i) % 4])
        }
        
        return result.joinToString("")
    }

    private fun unlockScreen() {
        // 启动自动锁定定时器
        if (!preferences.isParentMode) {
            val intent = Intent(this, LockScreenService::class.java)
            intent.action = LockScreenService.ACTION_START_AUTO_LOCK_TIMER
            startService(intent)
        }
        
        finish()
    }

    override fun onBackPressed() {
        // 禁用返回键
    }
}