package com.example.logisticsmanagement.data.repository

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

    // 작업 기록 저장 (실시간 월별 집계 업데이트 포함)
    suspend fun saveWorkRecord(workRecord: WorkRecord): Result<String> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. 일일 작업 기록 저장
                val workRecordRef = firestore.collection("work_records").document()
                val recordWithId = workRecord.copy(id = workRecordRef.id)
                transaction.set(workRecordRef, recordWithId)

                // 2. 월별 집계 데이터 업데이트
                updateMonthlySummaryInTransaction(transaction, recordWithId, isAdd = true)

                workRecordRef.id
            }.await()

            Result.success("작업 기록이 저장되었습니다")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 작업 기록 수정
    suspend fun updateWorkRecord(oldRecord: WorkRecord, newRecord: WorkRecord): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. 일일 작업 기록 수정
                val workRecordRef = firestore.collection("work_records").document(newRecord.id)
                transaction.set(workRecordRef, newRecord)

                // 2. 월별 데이터에서 기존 데이터 차감
                updateMonthlySummaryInTransaction(transaction, oldRecord, isAdd = false)

                // 3. 월별 데이터에 새 데이터 추가
                updateMonthlySummaryInTransaction(transaction, newRecord, isAdd = true)

                Unit
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 작업 기록 삭제
    suspend fun deleteWorkRecord(workRecord: WorkRecord): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                // 1. 일일 작업 기록 삭제
                val workRecordRef = firestore.collection("work_records").document(workRecord.id)
                transaction.delete(workRecordRef)

                // 2. 월별 데이터에서 차감
                updateMonthlySummaryInTransaction(transaction, workRecord, isAdd = false)

                Unit
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 특정 날짜의 작업 기록 조회
    suspend fun getWorkRecordsByDate(
        companyId: String,
        date: Date
    ): Result<List<WorkRecord>> {
        return try {
            val startOfDay = DateUtils.getStartOfDay(date)
            val endOfDay = DateUtils.getEndOfDay(date)

            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .whereGreaterThanOrEqualTo("workDate", startOfDay)
                .whereLessThanOrEqualTo("workDate", endOfDay)
                .orderBy("workDate", Query.Direction.DESCENDING)
                .orderBy("workTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 기간별 작업 기록 조회
    suspend fun getWorkRecordsByDateRange(
        companyId: String,
        startDate: Date,
        endDate: Date
    ): Result<List<WorkRecord>> {
        return try {
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .whereGreaterThanOrEqualTo("workDate", startDate)
                .whereLessThanOrEqualTo("workDate", endDate)
                .orderBy("workDate", Query.Direction.DESCENDING)
                .orderBy("workTime", Query.Direction.DESCENDING)
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
            Result.success(records)
        } catch (e: Exception) {
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
                .orderBy("workDate", Query.Direction.DESCENDING)
                .orderBy("workTime", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 최근 작업 기록 조회 (대시보드용)
    suspend fun getRecentWorkRecords(
        companyId: String,
        limit: Int = 10
    ): Result<List<WorkRecord>> {
        return try {
            val snapshot = firestore.collection("work_records")
                .whereEqualTo("companyId", companyId)
                .orderBy("workTime", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val records = snapshot.toObjects(WorkRecord::class.java)
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 트랜잭션 내에서 월별 집계 업데이트
    private fun updateMonthlySummaryInTransaction(
        transaction: Transaction,
        workRecord: WorkRecord,
        isAdd: Boolean
    ) {
        val calendar = Calendar.getInstance().apply { time = workRecord.workDate }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        val monthlyDocId = "${workRecord.companyId}_${year}_${month.toString().padStart(2, '0')}"
        val monthlyRef = firestore.collection("monthly_summary").document(monthlyDocId)

        // 기존 월별 데이터 조회
        val monthlySnapshot = transaction.get(monthlyRef)
        val existingData = if (monthlySnapshot.exists()) {
            monthlySnapshot.toObject(MonthlySummary::class.java)!!
        } else {
            MonthlySummary(
                id = monthlyDocId,
                companyId = workRecord.companyId,
                year = year,
                month = month
            )
        }

        // 데이터 업데이트
        val updatedData = if (isAdd) {
            monthlySummaryRepository.addWorkRecordToSummary(existingData, workRecord)
        } else {
            monthlySummaryRepository.removeWorkRecordFromSummary(existingData, workRecord)
        }

        transaction.set(monthlyRef, updatedData)
    }
}
