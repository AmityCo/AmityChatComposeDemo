package co.amity.archdemo.ui.chats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import co.amity.archdemo.data.local.models.DummyChat
import co.amity.archdemo.data.local.models.toReadableChatDate
import co.amity.archdemo.ui.components.ChatAvatar

@Composable
fun ChatsScreen(
    modifier: Modifier,
    navigateToUsers: () -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val items by produceState<ChatsUiState>(
        initialValue = ChatsUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

//    if (items is ChatsUiState.Success) {
//        val chats = (items as ChatsUiState.Success).data
//        if (chats.isEmpty()) { navigateToUsers.invoke() }
//        ChatsScreen(
//            chats = chats,
//            modifier = modifier.padding(8.dp)
//        )
//    }
}

@Composable
internal fun ChatsScreen(
    chats: List<DummyChat>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        this.items(chats) { chat ->
            ChatsRow(chat = chat)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun ChatsRow(chat: DummyChat) {
    Row(verticalAlignment = Alignment.CenterVertically) {
       ChatAvatar(chat = chat)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row {
                Text(text = chat.displayName, fontWeight = FontWeight.Bold)
                Text(text = chat.updatedAt.toReadableChatDate())
            }
            Text(text = chat.latestMessage, color = MaterialTheme.colorScheme.primary.copy(alpha = .6f), maxLines = 2, fontSize = 13.sp, lineHeight = 15.sp, overflow = TextOverflow.Ellipsis)
        }
        Spacer(modifier = Modifier.width(24.dp))
        
    }
}