package com.example.logisticsmanagement.data.model.response

import android.os.Parcelable
import com.example.logisticsmanagement.data.model.*
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkRecordResponse(
    val workRecord: WorkRecord,
    val company: Company? = null,
    val user: User? = null,
    val distributor: Distributor? = null
) : Parcelable

@Parcelize
data class DashboardData(
    val todayTotalPallets: Int = 0,
    val todayRecordCount: Int = 0,
    val monthlyTotalPallets: Int = 0,
    val monthlyRecordCount: Int = 0,
    val activeDistributorCount: Int = 0,
    val recentWorkRecords: List<WorkRecordResponse> = emptyList(),
    val topDistributors: List<DistributorSummaryData> = emptyList(),
    val topItems: List<Pair<String, Int>> = emptyList()  // 아이템명, 수량
) : Parcelable