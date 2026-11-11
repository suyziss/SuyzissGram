package com.suyzissgram.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suyzissgram.domain.model.AppLanguage

@Composable
fun AuthScreen(
    state: AuthUiState,
    language: AppLanguage,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isRussian = language == AppLanguage.RU
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxSize()
            .background(colors.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "SUYZISSGRAM",
            color = colors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (isRussian) "Добро пожаловать" else "Welcome to SuyzissGram",
            color = colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isRussian) {
                "Введите email и пароль. Если аккаунт уже существует — выполним вход, иначе создадим новый."
            } else {
                "Enter your email and password. We will sign you in or create a new account automatically."
            },
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (isRussian) "Электронная почта" else "Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (isRussian) "@Username" else "@Username") },
            singleLine = true,
            supportingText = {
                Text(
                    if (isRussian) {
                        "Для нового аккаунта · 4–14 символов: латиница, цифры и _"
                    } else {
                        "For new accounts · 4–14 characters: letters, numbers and _"
                    }
                )
            }
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (isRussian) "Пароль" else "Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isRussian) {
                "Для нового аккаунта пароль должен содержать минимум 8 символов. Подтверждение email не требуется."
            } else {
                "New account passwords must contain at least 8 characters. No email verification is required."
            },
            color = colors.onSurfaceVariant,
            fontSize = 12.sp
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(state.error, color = colors.error)
        }

        Spacer(Modifier.height(22.dp))
        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = if (isRussian) "Продолжить" else "Continue"
                )
            }
        }
    }
}
