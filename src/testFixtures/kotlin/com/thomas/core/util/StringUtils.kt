package com.thomas.core.util

object StringUtils {

    private val CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
    private val NUMBERS = "0123456789".toList()
    private val DOCUMENT_WEIGHTS = (10 downTo 2).toList()
    private val REGISTRATION_WEIGHTS = (5 downTo 2).toList() + (9 downTo 2).toList()
    private val PASSWORD_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
    private val PASSWORD_LOWER = "abcdefghijklmnopqrstuvwxyz".toList()
    private val PASSWORD_SYMBOLS = "\"'!@#$%&*()_-+=§`´[]{}^~,.<>;:/?|\\".toList()

    fun randomString(
        length: Int = 10,
        numbers: Boolean = true,
        spaces: Boolean = true,
    ): String = ((NUMBERS.takeIf { numbers } ?: listOf()) + (listOf(" ").takeIf { spaces } ?: listOf()) + CHARS).let { chars ->
        (1..length).map { chars.shuffled().first() }.joinToString("")
    }

    fun randomZipcode(): String = (1000000..99999999).random().toString().padStart(8, '0')

    fun randomPhone(): String = (1000000000..99999999999).random().toString()

    fun randomEmail(): String = "${randomString(numbers = false).replace(" ", "")}@email.com"

    fun randomDocumentNumber(): String = (1..9).joinToString("") { NUMBERS.random().toString() }.let {
        "$it${it.calculateDigits(DOCUMENT_WEIGHTS)}"
    }

    fun randomRegistrationNumber(): String = "${(1..8).joinToString("") { NUMBERS.random().toString() }}0001".let {
        "$it${it.calculateDigits(REGISTRATION_WEIGHTS)}"
    }

    fun randomPassword(): String = PASSWORD_UPPER.random().toString() +
            (1..3).map { PASSWORD_LOWER.random() }.joinToString(separator = "") +
            PASSWORD_SYMBOLS.random().toString() +
            (1..3).map { NUMBERS.random() }.joinToString(separator = "")

    private fun String.calculateDigits(
        weights: List<Int>,
    ): String {
        val numbers = map { it.toString().toInt() }
        val firstDigit = numbers.digits(weights)
        val secondDigit = (numbers + firstDigit).digits(listOf(weights[0] + 1) + weights)

        return "$firstDigit$secondDigit"
    }

    private fun List<Int>.digits(
        weights: List<Int>,
    ): Int = this.withIndex().sumOf { (index, element) ->
        weights[index] * element
    }.let {
        it % 11
    }.takeIf {
        it > 1
    }?.let {
        11 - it
    } ?: 0

}
