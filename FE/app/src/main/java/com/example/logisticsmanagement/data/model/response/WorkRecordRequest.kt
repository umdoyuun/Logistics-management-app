package com.example.logisticsmanagement.data.model.request

import android.os.Parcelable
import com.example.logisticsmanagement.data.model.WorkItem
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WorkRecordRequest(
    val distributorId: String = "",
    val totalPallets: Int = 0,
    val items: List<WorkItem> = emptyList(),
    val workDate: Date = Date(),
    val notes: String = ""
) : Parcelable

// 입력 시나리오별 검증
@Parcelize
data class WorkRecordValidation(
    val isValid: Boolean = false,
    val errorMessage: String = "",
    val warnings: List<String> = emptyList()
) : Parcelable