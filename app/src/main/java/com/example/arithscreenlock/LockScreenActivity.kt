package com.example.arithscreenlock

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
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
    
    private lateinit var keyguardManager: KeyguardManager
    private val handler = Handler(Looper.getMainLooper())
    private var isUnlocked = false
    
    // 防绕过检查任务
    private val bypassCheckRunnable = object : Runnable {
        override fun run() {
            if (!isUnlocked && !isFinishing) {
                checkAndRestoreLockScreen()
                handler.postDelayed(this, 500) // 每500ms检查一次
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        
        // 设置为全屏并显示在锁屏上方，增强防绕过
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // 设置为不可取消的任务
        setTaskDescription(android.app.ActivityManager.TaskDescription("锁屏", null, 0))

        setContentView(R.layout.activity_lock_screen)

        preferences = LockScreenPreferences(this)
        
        // 如果是家长模式，直接解锁
        if (preferences.isParentMode) {
            unlockScreen()
            return
        }

        initViews()
        generateQuestions()
        
        // 启动防绕过检查
        startBypassProtection()
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

    private fun startBypassProtection() {
        handler.post(bypassCheckRunnable)
    }
    
    private fun checkAndRestoreLockScreen() {
        // 如果Activity不在前台，重新启动锁屏
        if (!hasWindowFocus() && !isUnlocked) {
            val intent = Intent(this, LockScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_CLEAR_TOP or
                          Intent.FLAG_ACTIVITY_SINGLE_TOP or
                          Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
    }

    private fun unlockScreen() {
        isUnlocked = true
        handler.removeCallbacks(bypassCheckRunnable)
        
        // 启动自动锁定定时器（家长模式30分钟，普通模式用户设置时长）
        val intent = Intent(this, LockScreenService::class.java)
        intent.action = LockScreenService.ACTION_START_AUTO_LOCK_TIMER
        startService(intent)
        
        finish()
    }

    override fun onBackPressed() {
        // 禁用返回键
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // 禁用所有系统键
        return when (keyCode) {
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_MENU,
            KeyEvent.KEYCODE_SEARCH,
            KeyEvent.KEYCODE_APP_SWITCH -> true
            else -> super.onKeyDown(keyCode, event)
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && !isUnlocked) {
            // 失去焦点时，尝试重新获得焦点
            handler.postDelayed({
                if (!isUnlocked && !isFinishing) {
                    val intent = Intent(this, LockScreenActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                                  Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                  Intent.FLAG_ACTIVITY_SINGLE_TOP or
                                  Intent.FLAG_ACTIVITY_NO_ANIMATION
                    startActivity(intent)
                }
            }, 100)
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (!isUnlocked) {
            // 被暂停时立即重新启动
            val intent = Intent(this, LockScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_CLEAR_TOP or
                          Intent.FLAG_ACTIVITY_SINGLE_TOP or
                          Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
    }
    
    override fun onStop() {
        super.onStop()
        if (!isUnlocked) {
            // 被停止时立即重新启动
            val intent = Intent(this, LockScreenActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_CLEAR_TOP or
                          Intent.FLAG_ACTIVITY_SINGLE_TOP or
                          Intent.FLAG_ACTIVITY_NO_ANIMATION
            startActivity(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(bypassCheckRunnable)
        if (!isUnlocked) {
            // 被销毁时立即重新启动
            val intent = Intent(this, LockScreenService::class.java)
            intent.action = LockScreenService.ACTION_SHOW_LOCK_SCREEN
            startService(intent)
        }
    }
}