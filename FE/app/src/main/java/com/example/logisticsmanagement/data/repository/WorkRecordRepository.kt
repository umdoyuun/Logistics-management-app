package com.example.logisticsmanagement.data.repository

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Transaction
import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.utils.DateUtils
import kotlinx.coroutines.tasks.await
import java.util.*

class WorkRecordRepository {

    private val firestore = FirebaseManager.firestore
    private val monthlySummaryRepository = MonthlySummaryRepository()
    private val TAG = "WorkRecordRepository"

    // 작업 기록 저장 (트랜잭션 간소화)
    suspend fun saveWorkRecord(workRecord: WorkRecord): Result<String> {
        return try {
            Log.d(TAG, "작업 기록 저장 시작: ${workRecord.distributorName} - ${workRecord.totalPallets}파렛트")

            // 일단 단순하게 저장만 (트랜잭션 없이)
            val workRecordRef = firestore.collection("work_records").document()
            val recordWithId = workRecord.copy(id = workRecordRef.id)

            Log.d(TAG, "Firestore에 저장할 데이터: $recordWithId")
            workRecordRef.set(recordWithId).await()
            Log.d(TAG, "작업 기록 저장 완료: ${workRecordRef.id}")

            Result.success(workRecordRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "작업 기록 저장 실패", e)
            Result.failure(e)
        }
    }

    // 작업 기록 수정 (단순화)
    suspend fun updateWorkRecord(oldRecord: WorkRecord, newRecord: WorkRecord): Result<Unit> {
        return try {
            Log.d(TAG, "작업 기록 수정 시작: ${newRecord.id}")

            val workRecordRef = firestore.collection("work_records").document(newRecord.id)
            workRecordRef.set(newRecord).await()

            Log.d(TAG, "작업 기록 수정 완료: ${newRecord.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "작업 기록 수정 실패", e)
            Result.failure(e)
        }
    }

    // 작업 기록 삭제 (단순화)
    suspend fun deleteWorkRecord(workRecord: WorkRecord): Result<Unit> {
        return try {
            Log.d(TAG, "작업 기록 삭제 시작: ${workRecord.id}")

            val workRecordRef = firestore.collection("work_records").document(workRecord.id)
            workRecordRef.delete().await()

            Log.d(TAG, "작업 기록 삭제 완료: ${workRecord.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "작업 기록 삭제 실패", e)
            Result.failure(e)
        }
    }

    // 특정 날짜의 작업 기록 조회 (모든 orderBy 제거)
    suspend fun getWorkRecordsByDate(
        companyId: String,
        date: Date
    ): Result<List<WorkRecord>> {
        return try {
            Log.d(TAG, "작업 기록 조회 시작: $companyId, $date")

            // 조건 없이 해당 회사의 모든 작업 기록 조회 (임시)
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val allRecords = snapshot.toObjects(WorkRecord::class.java)
            Log.d(TAG, "전체 작업 기록 수: ${allRecords.size}")

            // 클라이언트에서 날짜 필터링 및 정렬
            val startOfDay = DateUtils.getStartOfDay(date)
            val endOfDay = DateUtils.getEndOfDay(date)

            val filteredRecords = allRecords.filter { record ->
                record.workDate.time >= startOfDay.time && record.workDate.time <= endOfDay.time
            }.sortedWith(compareByDescending<WorkRecord> { it.workDate }.thenByDescending { it.workTime })

            Log.d(TAG, "필터링된 작업 기록 수: ${filteredRecords.size}")
            Result.success(filteredRecords)
        } catch (e: Exception) {
            Log.e(TAG, "작업 기록 조회 실패", e)
            Result.failure(e)
        }
    }

    // 기간별 작업 기록 조회 (인덱스 오류 방지를 위해 단순화)
    suspend fun getWorkRecordsByDateRange(
        companyId: String,
        startDate: Date,
        endDate: Date
    ): Result<List<WorkRecord>> {
        return try {
            Log.d(TAG, "기간별 작업 기록 조회: $companyId, $startDate ~ $endDate")

            // 조건 없이 해당 회사의 모든 작업 기록 조회 (임시)
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .get()
                .await()

            val allRecords = snapshot.toObjects(WorkRecord::class.java)
            Log.d(TAG, "전체 작업 기록 수: ${allRecords.size}")

            // 클라이언트에서 날짜 필터링 및 정렬
            val filteredRecords = allRecords.filter { record ->
                record.workDate.time >= startDate.time && record.workDate.time <= endDate.time
            }.sortedWith(compareByDescending<WorkRecord> { it.workDate }.thenByDescending { it.workTime })

            Log.d(TAG, "필터링된 기간별 작업 기록 수: ${filteredRecords.size}")
            Result.success(filteredRecords)
        } catch (e: Exception) {
            Log.e(TAG, "기간별 작업 기록 조회 실패", e)
            Result.failure(e)
        }
    }

    // 유통사별 작업 기록 조회
    suspend fun getWorkRecordsByDistributor(
        companyId: String,
        distributorId: String,
        limit: Int = 50
    ): Result<List<WorkRecord>> {
        return try {
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .whereEqualTo("distributorId", distributorId)
                .limit(limit.toLong())
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
                .sortedWith(compareByDescending<WorkRecord> { it.workDate }.thenByDescending { it.workTime })
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 작업 기록 조회
    suspend fun getRecentWorkRecords(
        companyId: String,
        limit: Int = 10
    ): Result<List<WorkRecord>> {
        return try {
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .limit(limit.toLong())
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
                .sortedByDescending { it.workTime }
                .take(limit)
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}