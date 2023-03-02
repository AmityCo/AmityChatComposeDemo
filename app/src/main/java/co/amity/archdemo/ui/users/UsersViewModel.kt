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

package co.amity.archdemo.ui.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.amity.archdemo.data.local.models.ApiResponse
import co.amity.archdemo.data.local.models.User
import co.amity.archdemo.data.remote.UsersRepository
import co.amity.archdemo.ui.users.UsersUiState.Error
import co.amity.archdemo.ui.users.UsersUiState.Loading
import co.amity.archdemo.ui.users.UsersUiState.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    usersRepository: UsersRepository
) : ViewModel() {

    val uiState: StateFlow<UsersUiState> = usersRepository
        .users.map { if (it is ApiResponse.Success) Success(it.data) else Loading }
        .catch { Error(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)
}

sealed interface UsersUiState {
    object Loading : UsersUiState
    data class Error(val throwable: Throwable) : UsersUiState
    data class Success(val data: List<User?>?) : UsersUiState
}
