package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Company(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val email: String = "",
    val businessNumber: String = "",
    val managerName: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) : Parcelable