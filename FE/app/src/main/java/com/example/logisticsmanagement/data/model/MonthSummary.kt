package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class MonthlySummary(
    @DocumentId
    val id: String = "",  // company_id_YYYY_MM 형식
    val companyId: String = "",
    val year: Int = 0,
    val month: Int = 0,

    // 전체 집계
    val totalPallets: Int = 0,
    val totalRecords: Int = 0,
    val totalDistributors: Int = 0,

    // 상세 집계 데이터 (JSON으로 저장)
    val distributorSummary: Map<String, DistributorSummaryData> = emptyMap(),
    val itemSummary: Map<String, ItemSummaryData> = emptyMap(),
    val dailySummary: Map<String, DailySummaryData> = emptyMap(),
    val categorySummary: Map<String, CategorySummaryData> = emptyMap(),

    @ServerTimestamp
    val lastUpdated: Date? = null,
    @ServerTimestamp
    val createdAt: Date? = null
) : Parcelable

@Parcelize
data class DistributorSummaryData(
    val name: String = "",
    val totalPallets: Int = 0,
    val recordCount: Int = 0,
    val dailyBreakdown: Map<String, Int> = emptyMap(),  // "2024-01-01" -> 50
    val itemBreakdown: Map<String, Int> = emptyMap()    // "보성 사과" -> 300
) : Parcelable

@Parcelize
data class ItemSummaryData(
    val totalPallets: Int = 0,
    val distributorBreakdown: Map<String, Int> = emptyMap(),  // "롯데마트" -> 300
    val category: String = ""
) : Parcelable

@Parcelize
data class DailySummaryData(
    val totalPallets: Int = 0,
    val recordCount: Int = 0,
    val topDistributors: List<String> = emptyList()
) : Parcelable

@Parcelize
data class CategorySummaryData(
    val totalPallets: Int = 0,
    val distributorCount: Int = 0,
    val topItems: List<String> = emptyList()
) : Parcelable