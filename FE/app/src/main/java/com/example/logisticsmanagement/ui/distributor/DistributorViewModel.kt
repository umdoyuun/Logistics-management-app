package com.example.logisticsmanagement.ui.distributor

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.logisticsmanagement.data.repository.DistributorRepository
import com.example.logisticsmanagement.data.model.Distributor
import kotlinx.coroutines.launch

class DistributorViewModel : ViewModel() {

    private val distributorRepository = DistributorRepository()
    private val TAG = "DistributorViewModel"

    // 유통사 목록
    private val _distributors = MutableLiveData<List<Distributor>>()
    val distributors: LiveData<List<Distributor>> = _distributors

    // 저장 결과
    private val _saveResult = MutableLiveData<Result<String>>()
    val saveResult: LiveData<Result<String>> = _saveResult

    // 삭제 결과
    private val _deleteResult = MutableLiveData<Result<Unit>>()
    val deleteResult: LiveData<Result<Unit>> = _deleteResult

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        Log.d(TAG, "DistributorViewModel 초기화")
        loadDistributors()
    }

    // 유통사 목록 로드
    fun loadDistributors() {
        Log.d(TAG, "유통사 목록 로드 시작")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = distributorRepository.getAllActiveDistributors()
                Log.d(TAG, "Repository 결과: ${result.isSuccess}")

                if (result.isSuccess) {
                    val distributorList = result.getOrNull() ?: emptyList()
                    Log.d(TAG, "조회된 유통사 수: ${distributorList.size}")
                    distributorList.forEach { distributor ->
                        Log.d(TAG, "유통사: ${distributor.name}, ID: ${distributor.id}, 활성: ${distributor.active}")
                    }
                    _distributors.value = distributorList
                } else {
                    val exception = result.exceptionOrNull()
                    Log.e(TAG, "유통사 목록 로드 실패", exception)
                    _errorMessage.value = "유통사 목록을 불러올 수 없습니다: ${exception?.message}"
                }
            } catch (e: Exception) {
                Log.e(TAG, "유통사 목록 로드 중 예외 발생", e)
                _errorMessage.value = "유통사 목록 로드 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d(TAG, "유통사 목록 로드 완료")
            }
        }
    }

    // 유통사 저장
    fun saveDistributor(distributor: Distributor) {
        Log.d(TAG, "유통사 저장 시작: ${distributor.name}")

        if (!validateDistributor(distributor)) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = if (distributor.id.isEmpty()) {
                    Log.d(TAG, "새 유통사 저장")
                    distributorRepository.saveDistributor(distributor)
                } else {
                    Log.d(TAG, "기존 유통사 수정")
                    distributorRepository.updateDistributor(distributor)
                    Result.success("수정 완료")
                }

                Log.d(TAG, "저장 결과: ${result.isSuccess}")
                _saveResult.value = result

                if (result.isSuccess) {
                    loadDistributors() // 목록 새로고침
                }

            } catch (e: Exception) {
                Log.e(TAG, "유통사 저장 중 예외 발생", e)
                _saveResult.value = Result.failure(e)
                _errorMessage.value = "유통사 저장 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 유통사 비활성화
    fun deactivateDistributor(distributorId: String) {
        Log.d(TAG, "유통사 비활성화: $distributorId")
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = distributorRepository.deactivateDistributor(distributorId)
                _deleteResult.value = result

                if (result.isSuccess) {
                    loadDistributors() // 목록 새로고침
                }

            } catch (e: Exception) {
                Log.e(TAG, "유통사 비활성화 중 예외 발생", e)
                _deleteResult.value = Result.failure(e)
                _errorMessage.value = "유통사 비활성화 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 샘플 데이터 추가
    fun addSampleDistributors() {
        Log.d(TAG, "샘플 데이터 추가 시작")

        val sampleDistributors = listOf(
            Distributor(
                name = "abc로지스틱",
                code = "ABC001",
                mainCategory = "농산",
                address = "서울시 강남구",
                phone = "02-1234-5678",
                email = "contact@abclogistics.com",
                contactPerson = "김대리"
            ),
            Distributor(
                name = "가나다운수",
                code = "GND001",
                mainCategory = "축산",
                address = "부산시 해운대구",
                phone = "051-9876-5432",
                email = "info@gndtrans.com",
                contactPerson = "이과장"
            ),
            Distributor(
                name = "대한물류",
                code = "DHM001",
                mainCategory = "수산",
                address = "인천시 연수구",
                phone = "032-5555-1111",
                email = "support@daehanlogistics.com",
                contactPerson = "박부장"
            ),
            Distributor(
                name = "서울운송",
                code = "SEO001",
                mainCategory = "농산",
                address = "서울시 송파구",
                phone = "02-7777-8888",
                email = "seoul@seoultrans.co.kr",
                contactPerson = "최팀장"
            )
        )

        _isLoading.value = true
        viewModelScope.launch {
            try {
                var successCount = 0
                sampleDistributors.forEach { distributor ->
                    Log.d(TAG, "샘플 유통사 저장 중: ${distributor.name}")
                    val result = distributorRepository.saveDistributor(distributor)
                    if (result.isSuccess) {
                        successCount++
                        Log.d(TAG, "샘플 유통사 저장 성공: ${distributor.name}")
                    } else {
                        Log.e(TAG, "샘플 유통사 저장 실패: ${distributor.name}, ${result.exceptionOrNull()?.message}")
                    }
                }

                Log.d(TAG, "샘플 데이터 추가 완료: $successCount/$${sampleDistributors.size}")

                if (successCount > 0) {
                    loadDistributors() // 목록 새로고침
                    _errorMessage.value = "샘플 유통사 ${successCount}개가 추가되었습니다"
                } else {
                    _errorMessage.value = "샘플 유통사 추가에 실패했습니다"
                }

            } catch (e: Exception) {
                Log.e(TAG, "샘플 데이터 추가 중 예외 발생", e)
                _errorMessage.value = "샘플 데이터 추가 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 입력 유효성 검증
    private fun validateDistributor(distributor: Distributor): Boolean {
        when {
            distributor.name.isBlank() -> {
                _errorMessage.value = "유통사명을 입력해주세요"
                return false
            }
            distributor.code.isBlank() -> {
                _errorMessage.value = "유통사 코드를 입력해주세요"
                return false
            }
            distributor.mainCategory.isBlank() -> {
                _errorMessage.value = "주요 분류를 선택해주세요"
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
        _deleteResult.value = null
    }
}