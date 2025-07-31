package com.example.logisticsmanagement.data.repository

import android.util.Log
import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.utils.DateUtils
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MonthlySummaryRepository {

    private val firestore = FirebaseManager.firestore
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val TAG = "MonthlySummaryRepo"

    // 월별 집계 데이터 조회 (엑셀 다운로드용)
    suspend fun getMonthlySummary(
        companyId: String,
        year: Int,
        month: Int
    ): Result<MonthlySummary?> {
        return try {
            val docId = "${companyId}_${year}_${month.toString().padStart(2, '0')}"
            Log.d(TAG, "월별 집계 조회 시작: $docId")

            val snapshot = firestore.collection("monthly_summary")
                .document(docId)
                .get()
                .await()

            if (snapshot.exists()) {
                val monthlySummary = snapshot.toObject(MonthlySummary::class.java)
                Log.d(TAG, "월별 집계 발견: ${monthlySummary?.totalPallets}파렛트, ${monthlySummary?.totalRecords}건")
                Result.success(monthlySummary)
            } else {
                Log.d(TAG, "월별 집계 문서가 존재하지 않음: $docId")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "월별 집계 조회 실패", e)
            Result.failure(e)
        }
    }

    // 월별 집계 저장
    suspend fun saveMonthlySummary(monthlySummary: MonthlySummary): Result<Unit> {
        return try {
            Log.d(TAG, "월별 집계 저장 시작: ${monthlySummary.id}")

            firestore.collection("monthly_summary")
                .document(monthlySummary.id)
                .set(monthlySummary)
                .await()

            Log.d(TAG, "월별 집계 저장 완료: ${monthlySummary.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "월별 집계 저장 실패", e)
            Result.failure(e)
        }
    }

    // 작업 기록을 월별 집계에 추가
    fun addWorkRecordToSummary(
        existingSummary: MonthlySummary,
        workRecord: WorkRecord
    ): MonthlySummary {
        Log.d(TAG, "월별 집계에 작업 기록 추가: ${workRecord.distributorName} - ${workRecord.totalPallets}파렛트")

        val dateString = dateFormat.format(workRecord.workDate)

        // 전체 통계 업데이트
        val newTotalPallets = existingSummary.totalPallets + workRecord.totalPallets
        val newTotalRecords = existingSummary.totalRecords + 1

        // 유통사별 집계 업데이트
        val updatedDistributorSummary = existingSummary.distributorSummary.toMutableMap()
        val existingDistributor = updatedDistributorSummary[workRecord.distributorId]
            ?: DistributorSummaryData(name = workRecord.distributorName)

        updatedDistributorSummary[workRecord.distributorId] = existingDistributor.copy(
            totalPallets = existingDistributor.totalPallets + workRecord.totalPallets,
            recordCount = existingDistributor.recordCount + 1,
            dailyBreakdown = existingDistributor.dailyBreakdown.toMutableMap().apply {
                this[dateString] = (this[dateString] ?: 0) + workRecord.totalPallets
            },
            itemBreakdown = existingDistributor.itemBreakdown.toMutableMap().apply {
                workRecord.items.forEach { item ->
                    this[item.itemName] = (this[item.itemName] ?: 0) + item.quantity
                }
            }
        )

        // 품목별 집계 업데이트
        val updatedItemSummary = existingSummary.itemSummary.toMutableMap()
        workRecord.items.forEach { item ->
            val existingItem = updatedItemSummary[item.itemName]
                ?: ItemSummaryData(category = item.category)

            updatedItemSummary[item.itemName] = existingItem.copy(
                totalPallets = existingItem.totalPallets + item.quantity,
                distributorBreakdown = existingItem.distributorBreakdown.toMutableMap().apply {
                    this[workRecord.distributorName] =
                        (this[workRecord.distributorName] ?: 0) + item.quantity
                }
            )
        }

        // 일별 집계 업데이트
        val updatedDailySummary = existingSummary.dailySummary.toMutableMap()
        val existingDaily = updatedDailySummary[dateString] ?: DailySummaryData()

        updatedDailySummary[dateString] = existingDaily.copy(
            totalPallets = existingDaily.totalPallets + workRecord.totalPallets,
            recordCount = existingDaily.recordCount + 1,
            topDistributors = updateTopDistributors(
                existingDaily.topDistributors,
                workRecord.distributorName
            )
        )

        // 분류별 집계 업데이트
        val updatedCategorySummary = existingSummary.categorySummary.toMutableMap()
        workRecord.items.forEach { item ->
            if (item.category.isNotEmpty()) {
                val existingCategory = updatedCategorySummary[item.category]
                    ?: CategorySummaryData()

                updatedCategorySummary[item.category] = existingCategory.copy(
                    totalPallets = existingCategory.totalPallets + item.quantity,
                    topItems = updateTopItems(existingCategory.topItems, item.itemName)
                )
            }
        }

        val result = existingSummary.copy(
            totalPallets = newTotalPallets,
            totalRecords = newTotalRecords,
            totalDistributors = updatedDistributorSummary.size,
            distributorSummary = updatedDistributorSummary,
            itemSummary = updatedItemSummary,
            dailySummary = updatedDailySummary,
            categorySummary = updatedCategorySummary
        )

        Log.d(TAG, "월별 집계 업데이트 완료: 총 ${result.totalPallets}파렛트, ${result.totalRecords}건")
        return result
    }

    // 작업 기록을 월별 집계에서 제거 (수정/삭제 시)
    fun removeWorkRecordFromSummary(
        existingSummary: MonthlySummary,
        workRecord: WorkRecord
    ): MonthlySummary {
        Log.d(TAG, "월별 집계에서 작업 기록 제거: ${workRecord.distributorName} - ${workRecord.totalPallets}파렛트")

        // addWorkRecordToSummary와 반대 로직
        val dateString = dateFormat.format(workRecord.workDate)

        val newTotalPallets = (existingSummary.totalPallets - workRecord.totalPallets).coerceAtLeast(0)
        val newTotalRecords = (existingSummary.totalRecords - 1).coerceAtLeast(0)

        // 간단하게 전체 수치만 차감
        val result = existingSummary.copy(
            totalPallets = newTotalPallets,
            totalRecords = newTotalRecords
        )

        Log.d(TAG, "월별 집계 제거 완료: 총 ${result.totalPallets}파렛트, ${result.totalRecords}건")
        return result
    }

    private fun updateTopDistributors(
        existing: List<String>,
        distributorName: String
    ): List<String> {
        val updated = existing.toMutableList()
        if (!updated.contains(distributorName)) {
            updated.add(distributorName)
        }
        return updated.take(5) // 상위 5개만 유지
    }

    private fun updateTopItems(
        existing: List<String>,
        itemName: String
    ): List<String> {
        val updated = existing.toMutableList()
        if (!updated.contains(itemName)) {
            updated.add(itemName)
        }
        return updated.take(10) // 상위 10개만 유지
    }
}