package com.suyzissgram.data.repository

import android.content.SharedPreferences
import android.util.Base64
import com.suyzissgram.domain.model.AuthUser
import com.suyzissgram.domain.repository.AuthRepository
import com.suyzissgram.domain.repository.AuthResult
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class LocalAuthRepository(
    private val preferences: SharedPreferences
) : AuthRepository {
    private val session = MutableStateFlow(loadSession())

    override fun observeSession(): Flow<AuthUser?> = session.asStateFlow()

    override fun currentSession(): AuthUser? = session.value

    override suspend fun register(
        email: String,
        username: String,
        password: CharArray
    ): AuthResult = withContext(Dispatchers.Default) {
        val normalizedEmail = email.trim().lowercase()
        val normalizedUsername = username.trim().removePrefix("@")

        if (!EMAIL_PATTERN.matches(normalizedEmail)) {
            password.fill('\u0000')
            return@withContext AuthResult.Error("Введите корректный email")
        }
        if (!USERNAME_PATTERN.matches(normalizedUsername)) {
            password.fill('\u0000')
            return@withContext AuthResult.Error("Username должен содержать 4–14 символов: латинские буквы, цифры и _")
        }
        if (password.size < MIN_PASSWORD_LENGTH) {
            password.fill('\u0000')
            return@withContext AuthResult.Error("Пароль должен содержать минимум 8 символов")
        }
        if (preferences.getString(KEY_ACCOUNT_ID, null) != null) {
            password.fill('\u0000')
            return@withContext AuthResult.Error(
                "На этом устройстве уже создан локальный аккаунт. Войдите в него."
            )
        }

        val salt = ByteArray(SALT_LENGTH).also(SecureRandom()::nextBytes)
        val passwordHash = derivePasswordHash(password, salt)
        password.fill('\u0000')
        val user = AuthUser(
            id = generateAccountId(),
            email = normalizedEmail,
            username = normalizedUsername
        )

        preferences.edit()
            .putString(KEY_ACCOUNT_ID, user.id)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_PASSWORD_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString(KEY_PASSWORD_HASH, Base64.encodeToString(passwordHash, Base64.NO_WRAP))
            .putBoolean(KEY_SESSION_ACTIVE, true)
            .apply()

        session.value = user
        AuthResult.Success(user)
    }

    override suspend fun authenticate(
        email: String,
        username: String,
        password: CharArray
    ): AuthResult {
        val normalizedEmail = email.trim().lowercase()
        val savedEmail = preferences.getString(KEY_EMAIL, null)
        return when {
            savedEmail == null -> register(email, username, password)
            normalizedEmail == savedEmail -> login(email, password)
            else -> {
                password.fill('\u0000')
                AuthResult.Error(
                    "На этом устройстве уже есть локальный аккаунт с другим email. Войдите в него."
                )
            }
        }
    }

    override suspend fun login(
        email: String,
        password: CharArray
    ): AuthResult = withContext(Dispatchers.Default) {
        val normalizedEmail = email.trim().lowercase()
        val savedEmail = preferences.getString(KEY_EMAIL, null)
        val savedSalt = decode(KEY_PASSWORD_SALT)
        val savedHash = decode(KEY_PASSWORD_HASH)
        val accountId = preferences.getString(KEY_ACCOUNT_ID, null)
        val username = preferences.getString(KEY_USERNAME, null)

        if (savedEmail == null || savedSalt == null || savedHash == null ||
            accountId == null || username == null
        ) {
            password.fill('\u0000')
            return@withContext AuthResult.Error("Локальный аккаунт не найден")
        }

        val candidateHash = derivePasswordHash(password, savedSalt)
        password.fill('\u0000')
        val passwordMatches = MessageDigest.isEqual(savedHash, candidateHash)
        candidateHash.fill(0)

        if (normalizedEmail != savedEmail || !passwordMatches) {
            return@withContext AuthResult.Error("Неверный email или пароль")
        }

        val user = AuthUser(getOrMigrateAccountId(accountId), savedEmail, username)
        preferences.edit().putBoolean(KEY_SESSION_ACTIVE, true).apply()
        session.value = user
        AuthResult.Success(user)
    }

    private fun getOrMigrateAccountId(storedId: String): String {
        if (ACCOUNT_ID_PATTERN.matches(storedId)) return storedId
        return generateAccountId().also { newId ->
            preferences.edit().putString(KEY_ACCOUNT_ID, newId).apply()
        }
    }

    private fun generateAccountId(): String {
        val random = SecureRandom()
        return buildString(ACCOUNT_ID_LENGTH) {
            append(random.nextInt(9) + 1)
            repeat(ACCOUNT_ID_LENGTH - 1) {
                append(random.nextInt(10))
            }
        }
    }

    private fun loadSession(): AuthUser? {
        if (!preferences.getBoolean(KEY_SESSION_ACTIVE, false)) return null
        val storedId = preferences.getString(KEY_ACCOUNT_ID, null) ?: return null
        val id = getOrMigrateAccountId(storedId)
        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        val username = preferences.getString(KEY_USERNAME, null) ?: return null
        return AuthUser(id, email, username)
    }

    private fun decode(key: String): ByteArray? {
        val value = preferences.getString(key, null) ?: return null
        return runCatching { Base64.decode(value, Base64.NO_WRAP) }.getOrNull()
    }

    private fun derivePasswordHash(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private companion object {
        val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        val USERNAME_PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]{3,13}$")
        val ACCOUNT_ID_PATTERN = Regex("^[0-9]{10,13}$")
        const val MIN_PASSWORD_LENGTH = 8
        const val ACCOUNT_ID_LENGTH = 10
        const val SALT_LENGTH = 16
        const val HASH_BITS = 256
        const val PBKDF2_ITERATIONS = 600_000

        const val KEY_ACCOUNT_ID = "auth_account_id"
        const val KEY_EMAIL = "auth_email"
        const val KEY_USERNAME = "auth_username"
        const val KEY_PASSWORD_SALT = "auth_password_salt"
        const val KEY_PASSWORD_HASH = "auth_password_hash"
        const val KEY_SESSION_ACTIVE = "auth_session_active"
    }
}
