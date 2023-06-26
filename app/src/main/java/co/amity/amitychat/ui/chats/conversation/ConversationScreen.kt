package co.amity.amitychat.ui.chats.conversation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import co.amity.amitychat.R
import co.amity.amitychat.ui.theme.Green100
import co.amity.amitychat.ui.theme.Green300
import co.amity.amitychat.ui.theme.Green500
import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.model.chat.message.AmityMessage
import kotlinx.coroutines.launch

@Composable
fun ConversationScreen(
    modifier: Modifier,
    onError: (String) -> Unit,
    navigateBack: () -> Unit,
    channelId: String?,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    BackHandler { navigateBack() } // don't show the user list when back is pressed

    if (channelId == null) {
        Log.e("ConversationScreen", "ConversationScreen: channel id was null")
        navigateBack.invoke()
    }
    requireNotNull(channelId)

    LaunchedEffect(key1 = lifecycle, key2 = viewModel.uiState) {
        viewModel.getConversation(channelId)
    }

    val uiState by produceState<ConversationUiState>(
        initialValue = ConversationUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    when (uiState) {
        is ConversationUiState.Error -> {
            Log.e(
                "ConversationScreen",
                "ConversationScreen: ${(uiState as ConversationUiState.Error).throwable.localizedMessage}"
            )
            (uiState as ConversationUiState.Error).throwable.localizedMessage?.let(onError)
            navigateBack.invoke()
        }

        ConversationUiState.Loading -> {
            Log.d("ConversationScreen", "ConversationScreen: loading")
        } //TODO()
        is ConversationUiState.Success -> {
            ConversationScreen(
                channel = (uiState as ConversationUiState.Success).data,
                modifier = modifier,
                navigateBack = navigateBack
            )
        }
    }
}

@Composable
internal fun ConversationScreen(
    channel: AmityChannel,
    modifier: Modifier = Modifier,
    navigateBack: () -> Unit,
    viewModel: ConversationViewModel = hiltViewModel()
) {

    Box(modifier = modifier.fillMaxSize()) {
        //todo add topbar
        Scaffold(modifier = modifier.fillMaxSize(), bottomBar = {
            ComposeMessageBox(channelId = channel.getChannelId())
        }) { paddingValues ->
            MessageHistory(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                currentUserId = viewModel.currentUserId,
                channelId = channel.getChannelId()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ComposeMessageBox(
    channelId: String,
    viewModel: ConversationViewModel = hiltViewModel()
) {
    var msg by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        BasicTextField(
            value = msg,
            onValueChange = { msg = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = MaterialTheme.colorScheme.primary),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(size = 16.dp)
                        )
                        .padding(8.dp)
                ) {
                    innerTextField()
                }
            }
        )
        IconButton(
            onClick = {
                viewModel.sendMessage(
                    channelId = channelId,
                    msg = msg,
                    onError = {
                        Log.d(
                            "TAG",
                            "ComposeMessageBox: error sending the message"
                        )// todo show to user
                    })
                msg = ""
            },
            enabled = msg.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(id = R.string.chat_send_message)
            )
        }
    }
}

@Composable
internal fun MessageHistory(
    modifier: Modifier,
    currentUserId: String,
    channelId: String,
    state: LazyListState = rememberLazyListState(),
    viewModel: ConversationViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val messages = viewModel.getHistory(channelId).collectAsLazyPagingItems()

    LazyColumn(
        modifier = modifier,
        state = state,
        horizontalAlignment = Alignment.Start,
        reverseLayout = true
    ) {
        // always scroll to show the latest message
        scope.launch {
            state.scrollToItem(0)
        }

        items(
            count = messages.itemCount,
            key = messages.itemKey { it.getMessageId() },
            contentType = messages.itemContentType { it.getDataType() }
        ) { index ->
            messages[index]?.let {
                MessageRow(it, it.getCreatorId() == currentUserId)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // TODO check loading state
    }
}

@Composable
internal fun MessageRow(msg: AmityMessage, isMe: Boolean) {
    val backgroundColor = if (isMe) Green300 else Green500
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val textColor = if (isMe) Green500 else Green100

    when (val data = msg.getData()) {
        is AmityMessage.Data.TEXT -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(.6F)
                        .wrapContentSize(align = alignment)
                        .padding(8.dp)
                        .background(backgroundColor, shape = RoundedCornerShape(8.dp)),
                ) {
                    Column {
                        if (isMe) {
                            Text(
                                text = msg.getCreator()?.getDisplayName() ?: "unknown",
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Text(
                            text = data.getText(),
                            modifier = Modifier
                                .padding(8.dp)
                                .wrapContentSize(),
                            style = TextStyle(color = textColor),
                        )
                    }
                }
            }
        }

        else -> {
            Log.d("ConversationScreen", "MessageRow: message type not supported yet")
        }
    }
}


