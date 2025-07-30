package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class WorkRecord(
    @DocumentId
    val id: String = "",
    val companyId: String = "",
    val userId: String = "",
    val distributorId: String = "",
    val distributorName: String = "",  // 중복 저장 (성능 최적화)

    // 🔥 핵심 데이터
    val totalPallets: Int = 0,  // 유통사별 총 파렛트 수

    // 품목별 세부 내역 (선택적)
    val items: List<WorkItem> = emptyList(),

    val workDate: Date = Date(),
    val workTime: Date = Date(),
    val status: WorkStatus = WorkStatus.COMPLETED,
    val notes: String = "",

    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null,
    val createdBy: String = "",
    val createdByName: String = "",  // 중복 저장 (성능 최적화)
    val updatedBy: String = ""
) : Parcelable

@Parcelize
data class WorkItem(
    val itemName: String = "",
    val quantity: Int = 0,
    val unit: WorkUnit = WorkUnit.PALLET,
    val category: String = "",  // 축산, 농산, 수산 등
    val notes: String = ""
) : Parcelable

enum class WorkUnit(val displayName: String) {
    PALLET("파렛트"),
    BOX("박스"),
    CASE("케이스"),
    PIECE("개")
}

enum class WorkStatus(val displayName: String) {
    PENDING("대기중"),
    COMPLETED("완료"),
    CANCELLED("취소됨")
}