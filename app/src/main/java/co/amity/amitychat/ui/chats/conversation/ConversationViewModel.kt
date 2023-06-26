package co.amity.amitychat.ui.chats.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import co.amity.amitychat.data.remote.AuthRepository
import co.amity.amitychat.data.remote.ChatRepository
import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationViewModel @Inject constructor(
    val chatRepository: ChatRepository,
    val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationUiState>(ConversationUiState.Loading)
    val uiState: StateFlow<ConversationUiState> = _uiState

    val currentUserId = authRepository.currentUserId

    suspend fun getConversation(id: String) = chatRepository.getChannel(id, onError = {
        _uiState.value = ConversationUiState.Error(it)
    })
        .collect {
            _uiState.value = ConversationUiState.Success(it)

//        return conversation.zip(history) { _conversation, _history ->
//            Conversation(_conversation, flowOf(_history))
//        }.catch { _uiState.value = ConversationUiState.Error(it) }.collect{
//            _uiState.value = ConversationUiState.Success(it)
//        }
        }

    fun getHistory(id: String) = chatRepository.getHistory(id).cachedIn(viewModelScope)

    fun sendMessage(channelId: String, msg: String, onError: (Throwable) -> Unit) =
        viewModelScope.launch {
            chatRepository.postMessage(
                channelId = channelId,
                msg = msg,
                onError = onError
            )
        }
}

sealed interface ConversationUiState {
    object Loading : ConversationUiState
    data class Error(val throwable: Throwable) : ConversationUiState
    data class Success(val data: AmityChannel) : ConversationUiState
}

//data class Conversation(
//    val channel: AmityChannel,
//    val messages: Flow<PagingData<AmityMessage>>
//)