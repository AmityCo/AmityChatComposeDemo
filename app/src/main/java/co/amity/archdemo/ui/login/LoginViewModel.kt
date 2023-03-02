/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.amity.archdemo.ui.login

import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.amity.archdemo.data.local.models.ApiResponse
import co.amity.archdemo.data.remote.AuthRepository
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.firebase.auth.AuthCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<LoginUiState> = authRepository
        .isSignedIn.map {if (it) LoginUiState.Authorized else LoginUiState.Unauthorized()}
        .catch { LoginUiState.Unauthorized(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginUiState.Loading)

    suspend fun googleSignIn(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        when (val oneTapResponse: ApiResponse<BeginSignInResult> =
            authRepository.signInWithGoogle()) {
            is ApiResponse.Success -> {
                val result = oneTapResponse.data!!
                val intent = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(intent)
            }
            is ApiResponse.Loading -> { /* no-op */ }
            else -> {
                // No saved credentials found. Launch the One Tap sign-up flow
                googleSignUp(launcher)
            }
        }
    }

    private suspend fun googleSignUp(launcher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>) {
        when(val oneTapResponse: ApiResponse<BeginSignInResult> = authRepository.signUpWithGoogle())  {
            is ApiResponse.Success -> {
                val result = oneTapResponse.data!!
                val intent = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(intent)
            }
            is ApiResponse.Loading -> { /* no-op */ }
            else -> Log.d("TAG", "googleSignUp: failure")
        }
    }

    fun firebaseSignIn(authCredential: AuthCredential) {
        viewModelScope.launch {
            authRepository.firebaseSignInWithGoogle(authCredential)
                .also { authRepository.amityLogIn() }
        }
    }
}

sealed interface LoginUiState {
    object Loading : LoginUiState
    data class Unauthorized(val throwable: Throwable? = null) : LoginUiState
    object Authorized : LoginUiState
}
