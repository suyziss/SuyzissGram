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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onModeChange: (AuthMode) -> Unit,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val isRussian = language == AppLanguage.RU
    val isRegister = state.mode == AuthMode.REGISTER
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
            if (isRegister) {
                if (isRussian) "Создать аккаунт" else "Create account"
            } else {
                if (isRussian) "С возвращением" else "Welcome back"
            },
            color = colors.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (isRussian) "Общайся в своём пространстве." else "Your conversations, your space.",
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
        if (isRegister) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("@Username") },
                singleLine = true,
                supportingText = {
                    Text(if (isRussian) "4–14 символа: латиница, цифры и _" else "4–14 characters: letters, numbers and _")
                }
            )
        }
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
        if (isRegister) {
            Spacer(Modifier.height(8.dp))
            Text(
                if (isRussian) "Минимум 8 символов. Подтверждение email не требуется." else "At least 8 characters. No email code required.",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

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
                Text(
                    if (isRegister) {
                        if (isRussian) "Зарегистрироваться" else "Create account"
                    } else {
                        if (isRussian) "Войти" else "Sign in"
                    }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = {
                onModeChange(if (isRegister) AuthMode.LOGIN else AuthMode.REGISTER)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isRegister) {
                    if (isRussian) "Уже есть аккаунт? Войти" else "Already have an account? Sign in"
                } else {
                    if (isRussian) "Нет аккаунта? Зарегистрироваться" else "New here? Create account"
                }
            )
        }
    }
}
