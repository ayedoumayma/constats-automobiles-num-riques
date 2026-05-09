package com.example.constatauto.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    /**
     * Formate une date (Calendar ou Long) en String (dd/MM/yyyy).
     */
    fun formatDate(timeInMillis: Long): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(Date(timeInMillis))
    }

    /**
     * Formate une heure et minute en String (HH:mm).
     */
    fun formatTime(hour: Int, minute: Int): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return formatter.format(calendar.time)
    }

    /**
     * Obtient la date et l'heure actuelles au format ISO 8601 ou similaire.
     */
    fun getCurrentTimestamp(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
