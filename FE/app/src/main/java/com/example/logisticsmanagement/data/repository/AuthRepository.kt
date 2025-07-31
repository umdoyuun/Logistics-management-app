package com.example.logisticsmanagement.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.example.logisticsmanagement.data.firebase.FirebaseManager
import com.example.logisticsmanagement.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseManager.auth
    private val firestore = FirebaseManager.firestore

    // 현재 사용자 정보
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // 로그인 상태 확인
    fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    // 로그인
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("로그인에 실패했습니다."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 회원가입
    suspend fun signUp(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                Result.success(user)
            } ?: Result.failure(Exception("회원가입에 실패했습니다."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 로그아웃
    suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 사용자 프로필 저장
    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 사용자 프로필 조회
    suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 현재 사용자 프로필 조회
    suspend fun getCurrentUserProfile(): Result<User?> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser != null) {
                getUserProfile(currentUser.uid)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}