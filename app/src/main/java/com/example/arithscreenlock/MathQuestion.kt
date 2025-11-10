package com.example.arithscreenlock

data class MathQuestion(
    val operand1: Int,
    val operand2: Int,
    val operator: String,
    val answer: Int
) {
    fun getQuestionText(): String {
        return "$operand1 $operator $operand2 ="
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
        maxNumber: Int,
        operations: List<Operation>
    ): List<MathQuestion> {
        val questions = mutableListOf<MathQuestion>()
        
        repeat(count) {
            val operation = operations.random()
            val question = when (operation) {
                Operation.ADDITION -> generateAddition(maxNumber)
                Operation.SUBTRACTION -> generateSubtraction(maxNumber)
                Operation.MULTIPLICATION -> generateMultiplication(maxNumber)
                Operation.DIVISION -> generateDivision(maxNumber)
            }
            questions.add(question)
        }
        
        return questions
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