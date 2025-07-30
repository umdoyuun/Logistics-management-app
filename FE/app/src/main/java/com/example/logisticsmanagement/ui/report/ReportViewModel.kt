package com.example.logisticsmanagement.ui.report

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
        loadCurrentUser()
    }

    // 현재 사용자 로드
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    // 현재 월 데이터 자동 로드
                    _currentUser.value?.let { user ->
                        loadMonthlySummary(
                            user.companyId,
                            DateUtils.getCurrentYear(),
                            DateUtils.getCurrentMonth()
                        )
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    // 월별 집계 데이터 로드
    fun loadMonthlySummary(companyId: String, year: Int, month: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = monthlySummaryRepository.getMonthlySummary(companyId, year, month)
                if (result.isSuccess) {
                    _monthlySummary.value = result.getOrNull()
                } else {
                    _errorMessage.value = "월별 집계 데이터를 불러올 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "월별 집계 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 엑셀 파일 생성 (CSV 방식)
    fun generateExcelReport(year: Int, month: Int) {
        val currentUser = _currentUser.value
        if (currentUser == null) {
            _errorMessage.value = "사용자 정보가 없습니다"
            return
        }

        val monthlySummary = _monthlySummary.value
        if (monthlySummary == null) {
            _errorMessage.value = "월별 집계 데이터가 없습니다"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // TODO: CSV 생성 로직 구현
                // 이후 단계에서 CsvExporter를 사용하여 구현

                _errorMessage.value = "엑셀 생성 기능은 다음 단계에서 구현됩니다"

            } catch (e: Exception) {
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