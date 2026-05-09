package com.example.constatauto.utils

object ValidationUtils {

    fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".")
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = "^\\+216[0-9]{8}$".toRegex()
        val plainPhoneRegex = "^[0-9]{8}$".toRegex()
        return phoneRegex.matches(phone) || plainPhoneRegex.matches(phone)
    }

    fun isValidCIN(cin: String): Boolean {
        val cinRegex = "^[0-9]{8}$".toRegex()
        return cinRegex.matches(cin)
    }

    fun isValidYear(year: String): Boolean {
        return try {
            val y = year.toInt()
            y in 1900..2025
        } catch (e: Exception) {
            false
        }
    }
}
