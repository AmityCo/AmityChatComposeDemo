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

package co.amity.archdemo.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.PagingData
import co.amity.archdemo.data.local.models.dummyChats
import co.amity.archdemo.data.remote.ChatRepository
import co.amity.archdemo.ui.chats.ChatsUiState.Error
import co.amity.archdemo.ui.chats.ChatsUiState.Loading
import co.amity.archdemo.ui.chats.ChatsUiState.Success
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    chatRepository: ChatRepository
) : ViewModel() {

    val dummyChatsFlow = flowOf(dummyChats)

    val uiState : StateFlow<ChatsUiState> = chatRepository.chats.map(::Success)
        .catch { Error(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)
}

sealed interface ChatsUiState {
    object Loading : ChatsUiState
    data class Error(val throwable: Throwable) : ChatsUiState
    data class Success(val data: PagedList<AmityChannel>) : ChatsUiState
}