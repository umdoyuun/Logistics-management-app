package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class User(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val position: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val companyId: String = "",
    val role: UserRole = UserRole.WORKER,
    val active: Boolean = true,        // isActive -> active로 변경
    val stability: Int = 0,            // stability 필드 추가
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) : Parcelable

enum class UserRole(val displayName: String) {
    ADMIN("관리자"),
    MANAGER("매니저"),
    WORKER("작업자")
}