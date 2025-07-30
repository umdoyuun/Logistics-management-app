package com.example.logisticsmanagement.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.example.logisticsmanagement.data.repository.AuthRepository
import com.example.logisticsmanagement.data.model.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    // 로그인 결과
    private val _loginResult = MutableLiveData<Result<FirebaseUser>>()
    val loginResult: LiveData<Result<FirebaseUser>> = _loginResult

    // 회원가입 결과
    private val _signUpResult = MutableLiveData<Result<Unit>>()
    val signUpResult: LiveData<Result<Unit>> = _signUpResult

    // 로딩 상태
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 현재 사용자 프로필
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    init {
        // 앱 시작 시 현재 사용자 확인
        checkCurrentUser()
    }

    // 로그인
    fun signIn(email: String, password: String) {
        if (!validateSignInInput(email, password)) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.signIn(email, password)
                _loginResult.value = result

                if (result.isSuccess) {
                    loadCurrentUserProfile()
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
                _errorMessage.value = "로그인 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 회원가입
    fun signUp(email: String, password: String, user: User) {
        if (!validateSignUpInput(email, password, user)) {
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Firebase Auth에 회원가입
                val authResult = authRepository.signUp(email, password)
                if (authResult.isFailure) {
                    _signUpResult.value = Result.failure(authResult.exceptionOrNull()!!)
                    _errorMessage.value = "회원가입 실패: ${authResult.exceptionOrNull()?.message}"
                    return@launch
                }

                // 2. Firestore에 사용자 프로필 저장
                val firebaseUser = authResult.getOrNull()!!
                val userProfile = user.copy(id = firebaseUser.uid)
                val profileResult = authRepository.saveUserProfile(userProfile)

                _signUpResult.value = profileResult

                if (profileResult.isFailure) {
                    _errorMessage.value = "프로필 저장 실패: ${profileResult.exceptionOrNull()?.message}"
                }

            } catch (e: Exception) {
                _signUpResult.value = Result.failure(e)
                _errorMessage.value = "회원가입 중 오류가 발생했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 로그아웃
    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _currentUser.value = null
            } catch (e: Exception) {
                _errorMessage.value = "로그아웃 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    // 현재 사용자 확인
    private fun checkCurrentUser() {
        if (authRepository.isUserLoggedIn()) {
            loadCurrentUserProfile()
        }
    }

    // 현재 사용자 프로필 로드
    private fun loadCurrentUserProfile() {
        viewModelScope.launch {
            try {
                val result = authRepository.getCurrentUserProfile()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                } else {
                    _errorMessage.value = "사용자 정보를 불러올 수 없습니다."
                }
            } catch (e: Exception) {
                _errorMessage.value = "사용자 정보 로드 중 오류가 발생했습니다: ${e.message}"
            }
        }
    }

    // 로그인 상태 확인
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }

    // 입력 유효성 검증
    private fun validateSignInInput(email: String, password: String): Boolean {
        when {
            email.isBlank() -> {
                _errorMessage.value = "이메일을 입력해주세요"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _errorMessage.value = "올바른 이메일 형식이 아닙니다"
                return false
            }
            password.isBlank() -> {
                _errorMessage.value = "비밀번호를 입력해주세요"
                return false
            }
            password.length < 6 -> {
                _errorMessage.value = "비밀번호는 6자 이상이어야 합니다"
                return false
            }
        }
        return true
    }

    private fun validateSignUpInput(email: String, password: String, user: User): Boolean {
        when {
            user.name.isBlank() -> {
                _errorMessage.value = "이름을 입력해주세요"
                return false
            }
            email.isBlank() -> {
                _errorMessage.value = "이메일을 입력해주세요"
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _errorMessage.value = "올바른 이메일 형식이 아닙니다"
                return false
            }
            password.isBlank() -> {
                _errorMessage.value = "비밀번호를 입력해주세요"
                return false
            }
            password.length < 6 -> {
                _errorMessage.value = "비밀번호는 6자 이상이어야 합니다"
                return false
            }
            user.companyId.isBlank() -> {
                _errorMessage.value = "소속 회사를 선택해주세요"
                return false
            }
        }
        return true
    }

    // 에러 메시지 클리어
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}