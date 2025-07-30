package com.example.logisticsmanagement.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Category(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val sortOrder: Int = 0,
    val isActive: Boolean = true,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) : Parcelable