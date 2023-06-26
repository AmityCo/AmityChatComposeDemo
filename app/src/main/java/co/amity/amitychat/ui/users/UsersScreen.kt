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

package co.amity.amitychat.ui.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.repeatOnLifecycle
import co.amity.amitychat.R
import co.amity.amitychat.data.local.models.User
import co.amity.amitychat.data.remote.fakeUsers
import co.amity.amitychat.ui.components.UserAvatar
import co.amity.amitychat.ui.theme.MyApplicationTheme
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

@Composable
fun UsersScreen(
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = hiltViewModel(),
    navigateToConversation: () -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val items by produceState<UsersUiState>(
        initialValue = UsersUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }
    if (items is UsersUiState.Success) {
        UsersScreen(
            users = ((items as UsersUiState.Success).data ?: emptyList()),
            modifier = modifier,
            navigateToConversation = navigateToConversation,
            viewModel = viewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UsersScreen(
    users: List<User?>,
    modifier: Modifier = Modifier,
    navigateToConversation: () -> Unit,
    viewModel: UsersViewModel
) {
    Column(modifier = modifier) {
        TopAppBar(
            // TODO allow multiple user selection
            title = { Text(text = stringResource(id = R.string.contact_list_select_title)) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3)
        ) {
            items(users) { user ->
                user?.let {
                    UserItemView(
                        user = it,
                        navigateToConversation = navigateToConversation,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
internal fun UserItemView(
    user: User,
    navigateToConversation: () -> Unit,
    viewModel: UsersViewModel
) {
    val alertListener = Channel<Unit>()
    val displayChatInputText = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    if (showDialog.value) {
        AlertDialog(onDismissRequest = { showDialog.value = false }, confirmButton = {
            Button(
                onClick = { alertListener.trySend(Unit).isSuccess }
            ) {
                Text(text = stringResource(R.string.alert_ok))
            }
        }, title = {
            Text(text = stringResource(R.string.intut_channel_name_title))
        }, text = {
            TextField(
                value = displayChatInputText.value,
                onValueChange = { displayChatInputText.value = it },
                modifier = Modifier.padding(8.dp)
            )
        })
    }

    LaunchedEffect(key1 = alertListener) {
        this.launch {
            alertListener.consumeAsFlow().collect {
                viewModel.createConversation(
                    user = user,
                    displayName = displayChatInputText.value,
                    onError = {}).collect {
                    navigateToConversation.invoke()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { showDialog.value = true },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserAvatar(user, 68.dp)
        Spacer(modifier = Modifier.height(8.dp))
        UserInfo(user)
        Spacer(modifier = Modifier.height(34.dp))
    }
}

@Composable
internal fun UserInfo(user: User) {
    Text(
        text = user.displayName ?: stringResource(id = R.string.username_unknown),
        fontSize = 14.sp,
        textAlign = TextAlign.Center,
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MyApplicationTheme {
        UsersScreen(fakeUsers, navigateToConversation = { }, viewModel = hiltViewModel())
    }
}

@Preview(showBackground = true)
@Composable
private fun UserRowPreview() {
    MyApplicationTheme {
        UserItemView(
            user = User("0", "Jane Doe", null, null),
            navigateToConversation = { },
            viewModel = hiltViewModel()
        )
    }
}
