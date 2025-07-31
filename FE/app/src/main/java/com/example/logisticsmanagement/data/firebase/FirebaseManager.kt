package com.example.logisticsmanagement.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseManager {

    // Firebase 인스턴스들 (Storage 제외)
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // 현재 사용자 확인
    fun getCurrentUser() = auth.currentUser

    // 로그인 상태 확인
    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    // 컬렉션 참조 헬퍼 메서드들 (업데이트된 구조)
    fun getCompaniesCollection() = firestore.collection("companies")
    fun getUsersCollection() = firestore.collection("users")
    fun getDistributorsCollection() = firestore.collection("distributors")
    fun getWorkRecordsCollection() = firestore.collection("work_records")
    fun getMonthlySummaryCollection() = firestore.collection("monthly_summary")
    fun getCategoriesCollection() = firestore.collection("categories")

    // 특정 회사의 작업 기록 쿼리
    fun getCompanyWorkRecords(companyId: String) =
        firestore.collection("work_records").whereEqualTo("company_id", companyId)

    // 특정 회사의 월별 집계 쿼리
    fun getCompanyMonthlySummary(companyId: String) =
        firestore.collection("monthly_summary").whereEqualTo("company_id", companyId)
}