package com.example.logisticsmanagement.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MM월 dd일", Locale.getDefault())

    fun formatDate(date: Date): String = dateFormat.format(date)

    fun formatDateTime(date: Date): String = dateTimeFormat.format(date)

    fun formatDisplayDate(date: Date): String = displayDateFormat.format(date)

    fun parseDate(dateString: String): Date? {
        return try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun getStartOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }

    fun getStartOfMonth(year: Int, month: Int): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfMonth(year: Int, month: Int): Date {
        return Calendar.getInstance().apply {
            set(year, month - 1, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.time
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}