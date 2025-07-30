package com.example.logisticsmanagement.data.repository

import com.google.firebase.firestore.Query
import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.Distributor
import kotlinx.coroutines.tasks.await

class DistributorRepository {

    private val firestore = FirebaseManager.firestore

    // 유통사 저장
    suspend fun saveDistributor(distributor: Distributor): Result<String> {
        return try {
            val docRef = firestore.collection("distributors").document()
            val distributorWithId = distributor.copy(id = docRef.id)
            docRef.set(distributorWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 모든 활성 유통사 목록 조회 (전역 마스터)
    suspend fun getAllActiveDistributors(): Result<List<Distributor>> {
        return try {
            val snapshot = firestore.collection("distributors")
                .whereEqualTo("isActive", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val distributors = snapshot.toObjects(Distributor::class.java)
            Result.success(distributors)
        } catch (e: Exception) {
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

    // 유통사 비활성화 (soft delete)
    suspend fun deactivateDistributor(distributorId: String): Result<Unit> {
        return try {
            firestore.collection("distributors")
                .document(distributorId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 카테고리별 유통사 조회
    suspend fun getDistributorsByCategory(category: String): Result<List<Distributor>> {
        return try {
            val snapshot = firestore.collection("distributors")
                .whereEqualTo("mainCategory", category)
                .whereEqualTo("isActive", true)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()

            val distributors = snapshot.toObjects(Distributor::class.java)
            Result.success(distributors)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}