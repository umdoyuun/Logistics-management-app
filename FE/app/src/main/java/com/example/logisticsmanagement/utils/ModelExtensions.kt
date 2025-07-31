package com.example.logisticsmanagement.utils

import com.example.logisticsmanagement.data.model.*
import java.text.SimpleDateFormat
import java.util.*

// WorkRecord 확장 함수들
fun WorkRecord.getFormattedDate(): String {
    val format = SimpleDateFormat("MM월 dd일", Locale.getDefault())
    return format.format(workDate)
}

fun WorkRecord.getFormattedDateTime(): String {
    val format = SimpleDateFormat("MM월 dd일 HH:mm", Locale.getDefault())
    return format.format(workTime)
}

fun WorkRecord.getTotalItemQuantity(): Int {
    return items.sumOf { it.quantity }
}

fun WorkRecord.isItemQuantityMatched(): Boolean {
    return totalPallets == getTotalItemQuantity()
}

// MonthlySummary 확장 함수들
fun MonthlySummary.getFormattedMonth(): String {
    return "${year}년 ${month}월"
}

fun MonthlySummary.getTopDistributors(limit: Int = 5): List<Pair<String, Int>> {
    return distributorSummary.map { (_, data) -> data.name to data.totalPallets }
        .sortedByDescending { it.second }
        .take(limit)
}

fun MonthlySummary.getTopItems(limit: Int = 5): List<Pair<String, Int>> {
    return itemSummary.map { (name, data) -> name to data.totalPallets }
        .sortedByDescending { it.second }
        .take(limit)
}

// Distributor 확장 함수들
fun Distributor.getDisplayName(): String {
    return if (code.isNotEmpty()) "$name ($code)" else name
}

// User 확장 함수들
fun User.getDisplayName(): String {
    return if (position.isNotEmpty()) "$name ($position)" else name
}

fun User.isAdmin(): Boolean = role == UserRole.ADMIN
fun User.isManager(): Boolean = role == UserRole.MANAGER || role == UserRole.ADMIN