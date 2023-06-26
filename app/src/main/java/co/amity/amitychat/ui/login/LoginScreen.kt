package co.amity.amitychat.ui.login

import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import co.amity.amitychat.R
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.GoogleAuthProvider.getCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    navigateToChats: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val authStatus by produceState<LoginUiState>(
        initialValue = LoginUiState.Unauthorized(),
        key1 = lifecycle,
        key2 = viewModel.uiState
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    val launcher = rememberFirebaseAuthLauncher(viewModel = viewModel)
    val scope = rememberCoroutineScope()
    val isProgressBarVisible by remember { mutableStateOf(authStatus == LoginUiState.Loading) }

    LaunchedEffect(key1 = authStatus, key2 = lifecycle) {
        if (authStatus == LoginUiState.Authorized) {
            navigateToChats.invoke()
        }
    }

    Column(modifier) {
        Button(onClick = { scope.launch { viewModel.googleSignIn(launcher) } }) {
            Text(text = stringResource(R.string.login_google_bt))
        }
        if (isProgressBarVisible) CircularProgressIndicator()
    }
}

@Composable
private fun rememberFirebaseAuthLauncher(viewModel: LoginViewModel): ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult> {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    return rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        result.data.let {
            try {
                scope.launch {
                    val credentials =
                        Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                    val googleIdToken = credentials.googleIdToken
                    val googleCredentials = getCredential(googleIdToken, null)
                    viewModel.firebaseSignIn(googleCredentials)
                }
            } catch (e: Exception) {
                Log.d("LOG", e.message.toString())
            }
        }
    }
}