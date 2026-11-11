package com.suyzissgram.domain.repository

import com.suyzissgram.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

sealed interface AuthResult {
    data class Success(val user: AuthUser) : AuthResult
    data class Error(val message: String) : AuthResult
}

interface AuthRepository {
    fun observeSession(): Flow<AuthUser?>
    fun currentSession(): AuthUser?
    suspend fun register(email: String, username: String, password: CharArray): AuthResult
    suspend fun authenticate(email: String, username: String, password: CharArray): AuthResult
    suspend fun login(email: String, password: CharArray): AuthResult
}
