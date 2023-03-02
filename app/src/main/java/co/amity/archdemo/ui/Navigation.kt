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

package co.amity.archdemo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.amity.archdemo.R
import co.amity.archdemo.ui.chats.ChatsScreen
import co.amity.archdemo.ui.login.LoginScreen
import co.amity.archdemo.ui.users.UsersScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainNavigation(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val userBannedText = stringResource(id = R.string.login_user_banned)
    val userErrorText = stringResource(id = R.string.login_user_error)

    // TODO Monitor connectivity manager

    LaunchedEffect(lifecycleOwner) {
        viewModel.uiState.collect { state ->
            when (state) {
                MainUiState.Banned -> showSnackbar(scope, snackbarHostState, userBannedText)
                is MainUiState.Error -> showSnackbar(scope, snackbarHostState, userErrorText)
                MainUiState.Loading -> { /* no-op */
                }

                MainUiState.LoggedIn -> navController.navigate(Route.ChatsList.route) { popUpTo(0) }
                MainUiState.LoggedOut -> navController.navigate(Route.Login.route) { popUpTo(0) }
            }
        }
    }

    NavHost(navController = navController, startDestination = Route.Loading.route) {
        composable(Route.Loading.route) { LoadingScreen() }
        composable(Route.UsersList.route) { UsersScreen(modifier) }
        composable(Route.Login.route) {
            LoginScreen(
                modifier = modifier,
                navigateToChats = { navController.navigate(Route.ChatsList.route) { popUpTo(0) } })
        }
        composable(Route.ChatsList.route) {
            ChatsScreen(
                modifier = modifier,
                navigateToUsers = { navController.navigate(Route.ChatsList.route) })
        }
    }
}

private fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    text: String
) {
    scope.launch { snackbarHostState.showSnackbar(text) }
}

@Composable
fun LoadingScreen() {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Text("Loading", color = Color.Black)
    }
}