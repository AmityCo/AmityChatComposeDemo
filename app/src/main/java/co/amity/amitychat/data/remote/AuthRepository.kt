package co.amity.amitychat.data.remote

import co.amity.amitychat.data.local.models.ApiResponse
import co.amity.amitychat.data.local.models.User
import com.amity.socialcloud.sdk.api.core.AmityCoreClient
import com.amity.socialcloud.sdk.api.core.AmityCoreClient.login
import com.amity.socialcloud.sdk.core.session.AccessTokenRenewal
import com.amity.socialcloud.sdk.core.session.model.SessionState
import com.amity.socialcloud.sdk.model.core.session.SessionHandler
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInResult
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


typealias SignInToFirebaseResponse = ApiResponse<Boolean>
typealias OneTapResponse = ApiResponse<BeginSignInResult>

interface AuthRepository {
    val currentUserId: String
    val isSignedIn: Flow<Boolean>
    val amitySession: Flow<SessionState>
    val amityTestSession: Flow<SessionState>
    suspend fun signInWithGoogle(): OneTapResponse
    suspend fun signUpWithGoogle(): OneTapResponse
    suspend fun firebaseSignInWithGoogle(googleCredential: AuthCredential): SignInToFirebaseResponse
    suspend fun amityLogIn()
}

class AuthRepositoryImp @Inject constructor(
    private val oneTapClient: SignInClient,
    private val signInRequest: BeginSignInRequest,
    private val signUpRequest: BeginSignInRequest,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    override val isSignedIn = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        auth.addAuthStateListener(authStateListener)
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithGoogle(): OneTapResponse {
        return try {
            val result = oneTapClient.beginSignIn(signInRequest).await()
            ApiResponse.Success(result)
        } catch (e: Exception) {
            ApiResponse.Failure(e)
        }
    }

    override suspend fun firebaseSignInWithGoogle(googleCredential: AuthCredential): SignInToFirebaseResponse {
        return try {
            val authResult = auth.signInWithCredential(googleCredential).await()
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            if (isNewUser) {
                addUserToFirestore()
            }
            ApiResponse.Success(true)
        } catch (e: Exception) {
            ApiResponse.Failure(e)
        }
    }

    private suspend fun addUserToFirestore() {
        auth.currentUser?.apply {
            val user = User(uid, this.displayName, this.email, this.photoUrl?.toString())
            firestore.collection("users").document(uid).set(user).await()
        }
    }

    override suspend fun signUpWithGoogle(): OneTapResponse {
        return try {
            val result = oneTapClient.beginSignIn(signUpRequest).await()
            ApiResponse.Success(result)
        } catch (e: Exception) {
            ApiResponse.Failure(e)
        }
    }

    override val amityTestSession = flow {
        delay(2000)
        emit(SessionState.Establishing)
        delay(2000)
        emit(SessionState.Established)
        delay(2000)
        emit(SessionState.NotLoggedIn)
    }

    override val amitySession = flow {
        emit(AmityCoreClient.currentSessionState)
        AmityCoreClient.observeSessionState().asFlow()
    }

    override suspend fun amityLogIn() =
        login(userId = currentUserId, sessionHandler = MySessionHandler())
            .build()
            .submit()
            .blockingAwait()

    class MySessionHandler : SessionHandler {
        override fun sessionWillRenewAccessToken(renewal: AccessTokenRenewal) {
            renewal.renew()
        }
    }
}