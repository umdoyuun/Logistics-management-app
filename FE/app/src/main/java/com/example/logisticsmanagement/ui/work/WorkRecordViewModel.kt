package com.example.logisticsmanagement.ui.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticsmanagement.data.repository.*
import com.example.logisticsmanagement.data.model.*
import com.example.logisticsmanagement.data.model.request.WorkRecordRequest
import kotlinx.coroutines.launch
import java.util.*

class WorkRecordViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val workRecordRepository = WorkRecordRepository()
    private val distributorRepository = DistributorRepository()

    // 유통사 목록
    private val _distributors = MutableLiveData<List<Distributor>>()
    val distributors: LiveData<List<Distributor>> = _distributors

    // 작업 기록 목록
    private val _workRecords = MutableLiveData<List<WorkRecord>>()
    val workRecords: LiveData<List<WorkRecord>> = _workRecords

    // 저장 결과
    private val _saveResult = MutableLiveData<Result<String>>()
    val saveResult: LiveData<Result<String>> = _saveResult

    // 수정 결과
    private val _updateResult = MutableLiveData<Result<Unit>>()
    val updateResult: LiveData<Result<Unit>> = _updateResult

    // 삭제 결과
    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult

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
        loadDistributors()
    }

    // 현재 사용자 로드
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                    // 사용자 정보가 로드되면 작업 기록도 로드
                    _currentUser.value?.let { user ->
                        loadTodayWorkRecords(user.companyId)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보를 불러올 수 없습니다: ${e.message}"
            }
        }
    }

    // 유통사 목록 로드
    fun loadDistributors() {
        viewModelScope.launch {
            try {
                val result = distributorRepository.getAllActiveDistributors()
                if (result.isSuccess) {
                    _distributors.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = "유통사 목록을 불러올 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "유통사 목록 로드 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    // 오늘 작업 기록 로드
    fun loadTodayWorkRecords(companyId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = workRecordRepository.getWorkRecordsByDate(companyId, Date())
                if (result.isSuccess) {
                    _workRecords.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = "작업 기록을 불러올 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "작업 기록 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 특정 날짜 작업 기록 로드
    fun loadWorkRecordsByDate(companyId: String, date: Date) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = workRecordRepository.getWorkRecordsByDate(companyId, date)
                if (result.isSuccess) {
                    _workRecords.value = result.getOrNull() ?: emptyList()
                } else {
                    _errorMessage.value = "작업 기록을 불러올 수 없습니다"
                }
            } catch (e: Exception) {
                _errorMessage.value = "작업 기록 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 작업 기록 저장
    fun saveWorkRecord(request: WorkRecordRequest) {
        val currentUser = _currentUser.value
        if (currentUser == null) {
            _errorMessage.value = "사용자 정보가 없습니다"
            return
        }

        if (!validateWorkRecordRequest(request)) {
            return
        }

        val distributor = _distributors.value?.find { it.id == request.distributorId }
        if (distributor == null) {
            _errorMessage.value = "유통사 정보를 찾을 수 없습니다"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val workRecord = WorkRecord(
                    companyId = currentUser.companyId,
                    userId = currentUser.id,
                    distributorId = request.distributorId,
                    distributorName = distributor.name,
                    totalPallets = request.totalPallets,
                    items = request.items,
                    workDate = request.workDate,
                    workTime = Date(),
                    notes = request.notes,
                    createdBy = currentUser.id,
                    createdByName = currentUser.name
                )

                val result = workRecordRepository.saveWorkRecord(workRecord)
                _saveResult.value = result

                if (result.isSuccess) {
                    // 저장 성공 시 목록 새로고침
                    loadTodayWorkRecords(currentUser.companyId)
                }

            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
                _errorMessage.value = "작업 기록 저장 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 작업 기록 수정
    fun updateWorkRecord(originalRecord: WorkRecord, updatedRequest: WorkRecordRequest) {
        val currentUser = _currentUser.value
        if (currentUser == null) {
            _errorMessage.value = "사용자 정보가 없습니다"
            return
        }

        if (!validateWorkRecordRequest(updatedRequest)) {
            return
        }

        val distributor = _distributors.value?.find { it.id == updatedRequest.distributorId }
        if (distributor == null) {
            _errorMessage.value = "유통사 정보를 찾을 수 없습니다"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val updatedRecord = originalRecord.copy(
                    distributorId = updatedRequest.distributorId,
                    distributorName = distributor.name,
                    totalPallets = updatedRequest.totalPallets,
                    items = updatedRequest.items,
                    workDate = updatedRequest.workDate,
                    notes = updatedRequest.notes,
                    updatedBy = currentUser.id
                )

                val result = workRecordRepository.updateWorkRecord(originalRecord, updatedRecord)
                _updateResult.value = result

                if (result.isSuccess) {
                    // 수정 성공 시 목록 새로고침
                    loadTodayWorkRecords(currentUser.companyId)
                }

            } catch (e: Exception) {
                _updateResult.value = Result.failure(e)
                _errorMessage.value = "작업 기록 수정 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 작업 기록 삭제
    fun deleteWorkRecord(workRecord: WorkRecord) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = workRecordRepository.deleteWorkRecord(workRecord)
                _deleteResult.value = result

                if (result.isSuccess) {
                    // 삭제 성공 시 목록 새로고침
                    _currentUser.value?.let { user ->
                        loadTodayWorkRecords(user.companyId)
                    }
                }

            } catch (e: Exception) {
                _deleteResult.value = Result.failure(e)
                _errorMessage.value = "작업 기록 삭제 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 입력 유효성 검증
    private fun validateWorkRecordRequest(request: WorkRecordRequest): Boolean {
        when {
            request.distributorId.isBlank() -> {
                _errorMessage.value = "유통사를 선택해주세요"
                return false
            }
            request.totalPallets <= 0 -> {
                _errorMessage.value = "파렛트 수는 0보다 커야 합니다"
                return false
            }
            request.items.isNotEmpty() && request.items.sumOf { it.quantity } != request.totalPallets -> {
                _errorMessage.value = "품목별 수량의 합계가 총 파렛트 수와 일치하지 않습니다"
                return false
            }
        }
        return true
    }

    // 에러 메시지 클리어
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }

    // 결과 클리어
    fun clearResults() {
        _saveResult.value = null
        _updateResult.value = null
        _deleteResult.value = null
    }
}
