package com.example.logisticsmanagement.data.repository

import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.Company
import kotlinx.coroutines.tasks.await

class CompanyRepository {

    private val firestore = FirebaseManager.firestore

    // 회사 정보 저장
    suspend fun saveCompany(company: Company): Result<String> {
        return try {
            val docRef = firestore.collection("companies").document()
            val companyWithId = company.copy(id = docRef.id)
            docRef.set(companyWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 회사 정보 조회
    suspend fun getCompanyById(companyId: String): Result<Company?> {
        return try {
            val snapshot = firestore.collection("companies")
                .document(companyId)
                .get()
                .await()

            val company = snapshot.toObject(Company::class.java)
            Result.success(company)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 활성 회사 목록 조회
    suspend fun getActiveCompanies(): Result<List<Company>> {
        return try {
            val snapshot = firestore.collection("companies")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val companies = snapshot.toObjects(Company::class.java)
            Result.success(companies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 회사 정보 수정
    suspend fun updateCompany(company: Company): Result<Unit> {
        return try {
            firestore.collection("companies")
                .document(company.id)
                .set(company)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}