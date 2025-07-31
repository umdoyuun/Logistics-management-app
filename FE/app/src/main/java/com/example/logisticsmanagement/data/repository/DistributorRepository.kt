package com.example.logisticsmanagement.data.repository

import android.util.Log
import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.Distributor
import kotlinx.coroutines.tasks.await

class DistributorRepository {

    private val firestore = FirebaseManager.firestore
    private val TAG = "DistributorRepository"

    // 유통사 저장
    suspend fun saveDistributor(distributor: Distributor): Result<String> {
        return try {
            Log.d(TAG, "저장할 유통사 데이터: $distributor")
            val docRef = firestore.collection("distributors").document()
            val distributorWithId = distributor.copy(id = docRef.id, active = true) // active로 변경
            Log.d(TAG, "Firestore에 저장할 데이터: $distributorWithId")
            docRef.set(distributorWithId).await()
            Log.d(TAG, "저장 완료: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "저장 실패", e)
            Result.failure(e)
        }
    }

    // 모든 활성 유통사 목록 조회 (active=true로 변경)
    suspend fun getAllActiveDistributors(): Result<List<Distributor>> {
        return try {
            Log.d(TAG, "유통사 목록 조회 시작")

            // 1. 먼저 모든 유통사 조회 (조건 없이)
            val allSnapshot = firestore.collection("distributors")
                .get()
                .await()

            val allDistributors = allSnapshot.toObjects(Distributor::class.java)
            Log.d(TAG, "전체 유통사 수: ${allDistributors.size}")

            allDistributors.forEach { distributor ->
                Log.d(TAG, "전체 유통사: 이름=${distributor.name}, ID=${distributor.id}, active=${distributor.active}")
            }

            // 2. active=true 조건으로 조회 (isActive -> active로 변경)
            val activeSnapshot = firestore.collection("distributors")
                .whereEqualTo("active", true)
                .get()
                .await()

            val activeDistributors = activeSnapshot.toObjects(Distributor::class.java)
            Log.d(TAG, "활성 유통사 수: ${activeDistributors.size}")

            activeDistributors.forEach { distributor ->
                Log.d(TAG, "활성 유통사: 이름=${distributor.name}, ID=${distributor.id}, active=${distributor.active}")
            }

            val sortedDistributors = activeDistributors.sortedBy { it.name }
            Log.d(TAG, "정렬된 유통사 수: ${sortedDistributors.size}")

            Result.success(sortedDistributors)
        } catch (e: Exception) {
            Log.e(TAG, "조회 실패", e)
            Result.failure(e)
        }
    }

    // 유통사 상세 정보 조회
    suspend fun getDistributorById(distributorId: String): Result<Distributor?> {
        return try {
            val snapshot = firestore.collection("distributors")
                .document(distributorId)
                .get()
                .await()

            val distributor = snapshot.toObject(Distributor::class.java)
            Result.success(distributor)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 유통사 수정
    suspend fun updateDistributor(distributor: Distributor): Result<Unit> {
        return try {
            firestore.collection("distributors")
                .document(distributor.id)
                .set(distributor)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 유통사 비활성화 (active=false로 변경)
    suspend fun deactivateDistributor(distributorId: String): Result<Unit> {
        return try {
            firestore.collection("distributors")
                .document(distributorId)
                .update("active", false)  // isActive -> active로 변경
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 카테고리별 유통사 조회 (active=true로 변경)
    suspend fun getDistributorsByCategory(category: String): Result<List<Distributor>> {
        return try {
            val snapshot = firestore.collection("distributors")
                .whereEqualTo("mainCategory", category)
                .whereEqualTo("active", true)  // isActive -> active로 변경
                .get()
                .await()

            val distributors = snapshot.toObjects(Distributor::class.java)
                .sortedBy { it.name }
            Result.success(distributors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}