package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Distributor(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val mainCategory: String = "",  // 축산, 농산, 수산 등
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val contactPerson: String = "",
    val active: Boolean = true,     // isActive -> active로 변경
    val stability: Int = 0,         // stability 필드 추가 (Firebase 경고 해결)
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) : Parcelable