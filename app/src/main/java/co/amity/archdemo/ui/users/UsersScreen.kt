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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.repeatOnLifecycle
import co.amity.archdemo.R
import co.amity.archdemo.data.local.models.User
import co.amity.archdemo.data.remote.fakeUsers
import co.amity.archdemo.ui.components.UserAvatar
import co.amity.archdemo.ui.theme.MyApplicationTheme

@Composable
fun UsersScreen(modifier: Modifier = Modifier, viewModel: UsersViewModel = hiltViewModel()) {
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
            items = ((items as UsersUiState.Success).data ?: emptyList()),
            modifier = modifier
        )
    }
}

@Composable
internal fun UsersScreen(
    items: List<User?>,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        items.forEach {
            it?.let { UserRow(user = it) }
        }
    }
}

@Composable
internal fun UserRow(user: User) {
    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(user)
        Spacer(modifier = Modifier.width(8.dp))
        UserInfo(user)
    }
}

@Composable
internal fun UserInfo(user: User) {
    Text(
        text = user.displayName ?: stringResource(id = R.string.username_unknown),
        fontSize = 18.sp
    )
}

// Previews

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    MyApplicationTheme {
        UsersScreen(fakeUsers)
    }
}

@Preview(showBackground = true)
@Composable
private fun UserRowPreview() {
    MyApplicationTheme {
        UserRow(user = User("0", "Jane Doe", null, null))
    }
}
