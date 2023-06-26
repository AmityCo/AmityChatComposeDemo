package co.amity.amitychat.ui.chats.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import co.amity.amitychat.R
import co.amity.amitychat.data.local.models.toReadableChatDate
import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel

@Composable
fun ChatsScreen(
    modifier: Modifier,
    navigateToUsers: () -> Unit,
    onError: (String) -> Unit,
    navigateToConversation: (String) -> Unit,
    viewModel: ChatsViewModel = hiltViewModel()
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by produceState<ChatsUiState>(
        initialValue = ChatsUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    if (uiState is ChatsUiState.Success) {
        val chats: LazyPagingItems<AmityChannel> =
            (uiState as ChatsUiState.Success).data.collectAsLazyPagingItems()
        ChatsScreen(
            chats = chats,
            navigateToUsers = navigateToUsers,
            navigateToConversation = navigateToConversation,
            leaveChannel = viewModel::leaveChannel,
            modifier = modifier.padding(8.dp)
        )
    } else if (uiState is ChatsUiState.Error) {
        (uiState as ChatsUiState.Error).throwable.localizedMessage?.let {
            onError(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatsScreen(
    chats: LazyPagingItems<AmityChannel>,
    modifier: Modifier = Modifier,
    navigateToUsers: () -> Unit,
    navigateToConversation: (String) -> Unit,
    leaveChannel: (String) -> Unit,
    state: LazyListState = rememberLazyListState(),
) {
    if (chats.itemCount == 0 && chats.loadState.refresh is LoadState.NotLoading && chats.loadState.refresh.endOfPaginationReached) {
        EmptyChannelList(modifier = modifier, navigateToUsers = navigateToUsers)
    }

    chats.apply {
        when {
            loadState.refresh is LoadState.Loading
                    || loadState.append is LoadState.Loading
                    || loadState.prepend is LoadState.Loading -> {
                LoadingChannels()
            }
        }
    }

    Column {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
        )
        LazyColumn(modifier = modifier, state = state) {
            items(
                count = chats.itemCount,
                key = chats.itemKey { it.getChannelId() },
                contentType = chats.itemContentType { it.getChannelType() }
            ) { index ->
                chats[index]?.let {
                    ChatsRow(
                        chat = it,
                        navigateToConversation = navigateToConversation,
                        leaveChannel = leaveChannel
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatsRow(
    chat: AmityChannel,
    navigateToConversation: (String) -> Unit,
    leaveChannel: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .combinedClickable(
                onClick = { navigateToConversation(chat.getChannelId()) },
                onLongClick = { leaveChannel.invoke(chat.getChannelId()) }
            )
            .padding(8.dp)
    ) {
//        ChatAvatar(chat = chat)
        Image(
            painter = painterResource(id = R.drawable.amity_glyph),
            contentDescription = "amity glyph"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row {
                Text(
                    text = chat.getDisplayName(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = chat.getLastActivity().toReadableChatDate(),
                    modifier = Modifier.alpha(.4f),
                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                )
            }
        }
        Spacer(modifier = Modifier.width(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EmptyChannelList(modifier: Modifier, navigateToUsers: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_name)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
        )
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.bg_empty),
                contentDescription = "",
                modifier = Modifier.size(900.dp)
            )
            Text(
                text = stringResource(id = R.string.empty_channels),
                modifier = modifier.clickable { navigateToUsers.invoke() },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
internal fun LoadingChannels() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}