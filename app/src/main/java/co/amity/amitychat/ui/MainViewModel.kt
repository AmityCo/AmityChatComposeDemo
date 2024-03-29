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

package co.amity.amitychat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.amity.amitychat.data.remote.AuthRepository
import co.amity.amitychat.ui.MainUiState.Error
import com.amity.socialcloud.sdk.core.session.model.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = authRepository
        .amitySession.map {
            when (it) {
                SessionState.NotLoggedIn,
                SessionState.Establishing -> MainUiState.LoggedOut

                SessionState.Established -> MainUiState.LoggedIn
                SessionState.TokenExpired -> MainUiState.Loading

                is SessionState.Terminated -> MainUiState.Banned
            }
        }
        .catch { Error(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MainUiState.Loading)
}

sealed interface MainUiState {
    object Loading : MainUiState
    object LoggedIn : MainUiState
    object LoggedOut : MainUiState
    object Banned : MainUiState
    data class Error(val throwable: Throwable?) : MainUiState
}
