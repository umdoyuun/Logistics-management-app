package com.example.logisticsmanagement.ui.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticsmanagement.data.repository.*
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.utils.DateUtils
import kotlinx.coroutines.launch
import java.io.File

class ReportViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val monthlySummaryRepository = MonthlySummaryRepository()
    private val workRecordRepository = WorkRecordRepository()
    private val TAG = "ReportViewModel"

    // 월별 집계 데이터
    private val _monthlySummary = MutableLiveData<MonthlySummary?>()
    val monthlySummary: LiveData<MonthlySummary?> = _monthlySummary

    // 엑셀 생성 결과
    private val _excelResult = MutableLiveData<Result<File>>()
    val excelResult: LiveData<Result<File>> = _excelResult

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    // 현재 사용자
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        Log.d(TAG, "ReportViewModel 초기화")
        loadCurrentUser()
    }

    // 현재 사용자 로드
    private fun loadCurrentUser() {
        Log.d(TAG, "현재 사용자 로드 시작")
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    Log.d(TAG, "현재 사용자: ${_currentUser.value?.name}, 회사: ${_currentUser.value?.companyId}")
                    // 현재 월 데이터 자동 로드
                    _currentUser.value?.let { user ->
                        loadMonthlySummary(
                            user.companyId,
                            DateUtils.getCurrentYear(),
                            DateUtils.getCurrentMonth()
                        )
                    }
                } else {
                    Log.e(TAG, "사용자 정보 로드 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "사용자 정보 로드 중 예외", e)
                _errorMessage.value = "사용자 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    // 월별 집계 데이터 로드
    fun loadMonthlySummary(companyId: String, year: Int, month: Int) {
        Log.d(TAG, "월별 집계 로드 시작: $companyId, ${year}년 ${month}월")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. 먼저 MonthlySummary 조회
                val summaryResult = monthlySummaryRepository.getMonthlySummary(companyId, year, month)
                Log.d(TAG, "MonthlySummary 조회 결과: ${summaryResult.isSuccess}")

                if (summaryResult.isSuccess) {
                    val monthlySummary = summaryResult.getOrNull()
                    if (monthlySummary != null) {
                        Log.d(TAG, "기존 월별 집계 발견: 총 ${monthlySummary.totalPallets}파렛트, ${monthlySummary.totalRecords}건")
                        _monthlySummary.value = monthlySummary
                    } else {
                        Log.d(TAG, "기존 월별 집계 없음, 실시간 생성 시도")
                        generateMonthlySummaryFromWorkRecords(companyId, year, month)
                    }
                } else {
                    Log.e(TAG, "월별 집계 조회 실패: ${summaryResult.exceptionOrNull()?.message}")
                    generateMonthlySummaryFromWorkRecords(companyId, year, month)
                }
            } catch (e: Exception) {
                Log.e(TAG, "월별 집계 로드 중 예외", e)
                _errorMessage.value = "월별 집계 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 작업 기록으로부터 월별 집계 실시간 생성
    private suspend fun generateMonthlySummaryFromWorkRecords(companyId: String, year: Int, month: Int) {
        try {
            Log.d(TAG, "작업 기록으로부터 월별 집계 생성 시작")

            val startDate = DateUtils.getStartOfMonth(year, month)
            val endDate = DateUtils.getEndOfMonth(year, month)

            Log.d(TAG, "조회 기간: $startDate ~ $endDate")

            val workRecordsResult = workRecordRepository.getWorkRecordsByDateRange(
                companyId, startDate, endDate
            )

            if (workRecordsResult.isSuccess) {
                val workRecords = workRecordsResult.getOrNull() ?: emptyList()
                Log.d(TAG, "해당 월 작업 기록 수: ${workRecords.size}")

                workRecords.forEach { record ->
                    Log.d(TAG, "작업 기록: ${record.distributorName} - ${record.totalPallets}파렛트 (${record.workDate})")
                }

                if (workRecords.isNotEmpty()) {
                    // 간단한 집계 생성
                    val totalPallets = workRecords.sumOf { it.totalPallets }
                    val totalRecords = workRecords.size
                    val distributorGroups = workRecords.groupBy { it.distributorId }

                    val distributorSummary = distributorGroups.mapValues { (_, records) ->
                        DistributorSummaryData(
                            name = records.first().distributorName,
                            totalPallets = records.sumOf { it.totalPallets },
                            recordCount = records.size
                        )
                    }

                    val simpleSummary = MonthlySummary(
                        id = "${companyId}_${year}_${month.toString().padStart(2, '0')}",
                        companyId = companyId,
                        year = year,
                        month = month,
                        totalPallets = totalPallets,
                        totalRecords = totalRecords,
                        totalDistributors = distributorGroups.size,
                        distributorSummary = distributorSummary
                    )

                    Log.d(TAG, "생성된 월별 집계: 총 ${totalPallets}파렛트, ${totalRecords}건, ${distributorGroups.size}개 유통사")
                    _monthlySummary.value = simpleSummary
                } else {
                    Log.d(TAG, "해당 월에 작업 기록이 없음")
                    _monthlySummary.value = null
                }
            } else {
                Log.e(TAG, "작업 기록 조회 실패: ${workRecordsResult.exceptionOrNull()?.message}")
                _monthlySummary.value = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "월별 집계 생성 중 예외", e)
            _monthlySummary.value = null
        }
    }

    // 엑셀 파일 생성 (CSV 방식)
    fun generateExcelReport(year: Int, month: Int) {
        Log.d(TAG, "엑셀 리포트 생성 시작: ${year}년 ${month}월")

        val currentUser = _currentUser.value
        if (currentUser == null) {
            Log.e(TAG, "사용자 정보가 없음")
            _errorMessage.value = "사용자 정보가 없습니다"
            return
        }

        val monthlySummary = _monthlySummary.value
        if (monthlySummary == null) {
            Log.e(TAG, "월별 집계 데이터가 없음")
            _errorMessage.value = "월별 집계 데이터가 없습니다"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "엑셀 생성 기능은 추후 구현 예정")
                _errorMessage.value = "엑셀 생성 기능은 다음 단계에서 구현됩니다"

            } catch (e: Exception) {
                Log.e(TAG, "엑셀 생성 중 예외", e)
                _excelResult.value = Result.failure(e)
                _errorMessage.value = "엑셀 생성 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 에러 메시지 클리어
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    // 결과 클리어
    fun clearResults() {
        _excelResult.value = null
    }
}