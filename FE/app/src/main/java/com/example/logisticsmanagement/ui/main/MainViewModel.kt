package com.example.logisticsmanagement.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticsmanagement.data.repository.*
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.data.model.response.DashboardData
import com.example.logisticsmanagement.utils.DateUtils
import com.example.logisticsmanagement.utils.getTopDistributors
import com.example.logisticsmanagement.utils.getTopItems
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val workRecordRepository = WorkRecordRepository()
    private val monthlySummaryRepository = MonthlySummaryRepository()
    private val distributorRepository = DistributorRepository()

    // 대시보드 데이터
    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

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
                    // 사용자 정보가 로드되면 대시보드 데이터 로드
                    _currentUser.value?.let { user ->
                        loadDashboardData(user.companyId)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    // 대시보드 데이터 로드
    fun loadDashboardData(companyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val today = Date()
                val currentYear = DateUtils.getCurrentYear()
                val currentMonth = DateUtils.getCurrentMonth()

                // 오늘 작업 기록 조회
                val todayRecordsResult = workRecordRepository.getWorkRecordsByDate(companyId, today)
                val todayRecords = todayRecordsResult.getOrNull() ?: emptyList()

                // 최근 작업 기록 조회
                val recentRecordsResult = workRecordRepository.getRecentWorkRecords(companyId, 10)
                val recentRecords = recentRecordsResult.getOrNull() ?: emptyList()

                // 월별 집계 데이터 조회
                val monthlySummaryResult = monthlySummaryRepository.getMonthlySummary(
                    companyId, currentYear, currentMonth
                )
                val monthlySummary = monthlySummaryResult.getOrNull()

                // 대시보드 데이터 구성
                val dashboardData = DashboardData(
                    todayTotalPallets = todayRecords.sumOf { it.totalPallets },
                    todayRecordCount = todayRecords.size,
                    monthlyTotalPallets = monthlySummary?.totalPallets ?: 0,
                    monthlyRecordCount = monthlySummary?.totalRecords ?: 0,
                    activeDistributorCount = monthlySummary?.totalDistributors ?: 0,
                    recentWorkRecords = emptyList(), // TODO: WorkRecordResponse로 변환
                    topDistributors = monthlySummary?.getTopDistributors(5)?.map { (name, pallets) ->
                        DistributorSummaryData(name = name, totalPallets = pallets)
                    } ?: emptyList(),
                    topItems = monthlySummary?.getTopItems(5) ?: emptyList()
                )

                _dashboardData.value = dashboardData

            } catch (e: Exception) {
                _errorMessage.value = "대시보드 데이터를 불러올 수 없습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 데이터 새로고침
    fun refreshData() {
        _currentUser.value?.let { user ->
            loadDashboardData(user.companyId)
        }
    }

    // 로그아웃
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                _errorMessage.value = "로그아웃 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    // 에러 메시지 클리어
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}