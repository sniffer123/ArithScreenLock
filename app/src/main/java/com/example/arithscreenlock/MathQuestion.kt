package com.example.arithscreenlock

data class MathQuestion(
    val operand1: Int,
    val operand2: Int,
    val operator: String,
    val answer: Int,
    val choices: List<Int> = emptyList() // 选择题选项
) {
    fun getQuestionText(): String {
        return "$operand1 $operator $operand2 ="
    }
    
    fun isMultipleChoice(): Boolean {
        return choices.isNotEmpty()
    }
}

object MathQuestionGenerator {
    enum class Operation(val symbol: String) {
        ADDITION("+"),
        SUBTRACTION("-"),
        MULTIPLICATION("×"),
        DIVISION("÷")
    }

    fun generateQuestions(
        count: Int,
        operations: List<Operation>,
        preferences: LockScreenPreferences,
        isMultipleChoice: Boolean = false
    ): List<MathQuestion> {
        val questions = mutableListOf<MathQuestion>()
        
        repeat(count) {
            val operation = operations.random()
            val maxNumber = preferences.getMaxNumberForOperation(operation)
            val question = when (operation) {
                Operation.ADDITION -> generateAddition(maxNumber)
                Operation.SUBTRACTION -> generateSubtraction(maxNumber)
                Operation.MULTIPLICATION -> generateMultiplication(maxNumber)
                Operation.DIVISION -> generateDivision(maxNumber)
            }
            
            val finalQuestion = if (isMultipleChoice) {
                question.copy(choices = generateChoices(question.answer))
            } else {
                question
            }
            
            questions.add(finalQuestion)
        }
        
        return questions
    }

    private fun generateChoices(correctAnswer: Int): List<Int> {
        val choices = mutableSetOf<Int>()
        choices.add(correctAnswer)
        
        // 生成3个错误选项
        while (choices.size < 4) {
            val wrongAnswer = when {
                correctAnswer <= 10 -> {
                    // 小数字：生成相近的错误答案
                    val offset = (-3..3).random()
                    (correctAnswer + offset).coerceAtLeast(0)
                }
                correctAnswer <= 100 -> {
                    // 中等数字：生成相对较近的错误答案
                    val offset = (-10..10).random()
                    (correctAnswer + offset).coerceAtLeast(0)
                }
                else -> {
                    // 大数字：生成更大范围的错误答案
                    val offset = (-correctAnswer / 4..correctAnswer / 4).random()
                    (correctAnswer + offset).coerceAtLeast(0)
                }
            }
            if (wrongAnswer != correctAnswer) {
                choices.add(wrongAnswer)
            }
        }
        
        return choices.toList().shuffled()
    }

    private fun generateAddition(maxNumber: Int): MathQuestion {
        val operand1 = (1..maxNumber).random()
        val operand2 = (1..maxNumber).random()
        return MathQuestion(operand1, operand2, "+", operand1 + operand2)
    }

    private fun generateSubtraction(maxNumber: Int): MathQuestion {
        val operand1 = (1..maxNumber).random()
        val operand2 = (1..operand1).random() // 确保结果为正数
        return MathQuestion(operand1, operand2, "-", operand1 - operand2)
    }

    private fun generateMultiplication(maxNumber: Int): MathQuestion {
        val operand1 = (1..(maxNumber / 2).coerceAtLeast(1)).random()
        val operand2 = (1..(maxNumber / operand1).coerceAtLeast(1)).random()
        return MathQuestion(operand1, operand2, "×", operand1 * operand2)
    }

    private fun generateDivision(maxNumber: Int): MathQuestion {
        val operand2 = (1..10).random()
        val answer = (1..(maxNumber / operand2).coerceAtLeast(1)).random()
        val operand1 = operand2 * answer
        return MathQuestion(operand1, operand2, "÷", answer)
    }
}