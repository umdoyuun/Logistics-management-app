package com.example.logisticsmanagement.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticsmanagement.data.repository.*
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.data.model.response.DashboardData
import com.example.logisticsmanagement.utils.DateUtils
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val workRecordRepository = WorkRecordRepository()
    private val monthlySummaryRepository = MonthlySummaryRepository()

    private val _dashboardData = MutableLiveData<DashboardData>()
    val dashboardData: LiveData<DashboardData> = _dashboardData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    _currentUser.value?.let { user ->
                        loadDashboardData(user.companyId)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    fun loadDashboardData(companyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val today = Date()
                val currentYear = DateUtils.getCurrentYear()
                val currentMonth = DateUtils.getCurrentMonth()

                val todayRecordsResult = workRecordRepository.getWorkRecordsByDate(companyId, today)
                val todayRecords = todayRecordsResult.getOrNull() ?: emptyList()

                val monthlySummaryResult = monthlySummaryRepository.getMonthlySummary(
                    companyId, currentYear, currentMonth
                )
                val monthlySummary = monthlySummaryResult.getOrNull()

                val dashboardData = DashboardData(
                    todayTotalPallets = todayRecords.sumOf { it.totalPallets },
                    todayRecordCount = todayRecords.size,
                    monthlyTotalPallets = monthlySummary?.totalPallets ?: 0,
                    monthlyRecordCount = monthlySummary?.totalRecords ?: 0,
                    activeDistributorCount = monthlySummary?.totalDistributors ?: 0,
                    recentWorkRecords = emptyList(),
                    topDistributors = monthlySummary?.distributorSummary?.values?.sortedByDescending { it.totalPallets }?.take(5) ?: emptyList(),
                    topItems = monthlySummary?.itemSummary?.map { (name, data) -> name to data.totalPallets }?.sortedByDescending { it.second }?.take(5) ?: emptyList()
                )

                _dashboardData.value = dashboardData

            } catch (e: Exception) {
                _errorMessage.value = "대시보드 데이터를 불러올 수 없습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        _currentUser.value?.let { user ->
            loadDashboardData(user.companyId)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}