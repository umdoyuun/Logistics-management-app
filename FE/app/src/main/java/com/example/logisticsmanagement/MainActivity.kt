package com.example.logisticsmanagement

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.logisticsmanagement.data.firebase.FirebaseManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase 연결 테스트
        testFirebaseConnection()
    }

    private fun testFirebaseConnection() {
        Log.d("Firebase", "Auth instance: ${FirebaseManager.auth}")
        Log.d("Firebase", "Firestore instance: ${FirebaseManager.firestore}")

        // Firestore 연결 테스트 - 테스트 데이터 저장
        FirebaseManager.firestore.collection("test")
            .document("connection")
            .set(mapOf(
                "status" to "connected",
                "timestamp" to System.currentTimeMillis(),
                "app_version" to "1.0",
                "test_type" to "firebase_connection"
            ))
            .addOnSuccessListener {
                Log.d("Firebase", "✅ Firestore connection successful!")

                // 샘플 데이터 생성 테스트
                createSampleData()
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "❌ Firestore connection failed", e)
            }
    }

    private fun createSampleData() {
        // 샘플 회사 데이터
        val sampleCompany = mapOf(
            "name" to "서울 물류센터",
            "address" to "서울시 강남구 테헤란로 123",
            "manager_name" to "김센터장",
            "phone" to "02-1234-5678",
            "is_active" to true,
            "created_at" to System.currentTimeMillis()
        )

        FirebaseManager.getCompaniesCollection()
            .document("logistics_center_seoul_001")
            .set(sampleCompany)
            .addOnSuccessListener {
                Log.d("Firebase", "✅ Sample company data created!")
            }

        // 샘플 유통사 데이터
        val sampleDistributor = mapOf(
            "name" to "롯데마트",
            "code" to "LOTTE",
            "main_category" to "축산",
            "phone" to "02-2222-3333",
            "is_active" to true,
            "created_at" to System.currentTimeMillis()
        )

        FirebaseManager.getDistributorsCollection()
            .document("distributor_lotte_mart")
            .set(sampleDistributor)
            .addOnSuccessListener {
                Log.d("Firebase", "✅ Sample distributor data created!")
            }

        // 샘플 카테고리 데이터
        val categories = listOf(
            mapOf("id" to "livestock", "name" to "축산", "sort_order" to 1),
            mapOf("id" to "agriculture", "name" to "농산", "sort_order" to 2),
            mapOf("id" to "marine", "name" to "수산", "sort_order" to 3)
        )

        categories.forEach { category ->
            FirebaseManager.getCategoriesCollection()
                .document(category["id"] as String)
                .set(category)
                .addOnSuccessListener {
                    Log.d("Firebase", "✅ Sample category ${category["name"]} created!")
                }
        }
    }
}