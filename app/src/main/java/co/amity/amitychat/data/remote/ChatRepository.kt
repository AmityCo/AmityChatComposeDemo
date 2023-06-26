package co.amity.amitychat.data.remote

import android.util.Log
import androidx.paging.PagingData
import co.amity.amitychat.data.local.models.User
import com.amity.socialcloud.sdk.api.chat.AmityChatClient
import com.amity.socialcloud.sdk.api.core.AmityCoreClient
import com.amity.socialcloud.sdk.helper.core.coroutines.asFlow
import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.model.chat.message.AmityMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

interface ChatRepository {
    val chats: Flow<PagingData<AmityChannel>>
    fun createChannel(
        user: User,
        displayName: String,
        onError: (Throwable) -> Unit
    ): Flow<AmityChannel>

    fun getChannel(id: String, onError: (Throwable) -> Unit): Flow<AmityChannel>
    fun getHistory(id: String): Flow<PagingData<AmityMessage>>
    suspend fun postMessage(
        channelId: String,
        msg: String,
        onError: (Throwable) -> Unit
    )

    suspend fun leaveChannel(channelId: String)
}

class RemoteChatsRepository @Inject constructor() : ChatRepository {
    val amityChannelRepo = AmityChatClient.newChannelRepository()
    val amityMessageRepo = AmityChatClient.newMessageRepository()

    init {
        AmityCoreClient.registerPushNotification()
    }

    override val chats: Flow<PagingData<AmityChannel>> =
        amityChannelRepo.getChannels().all().build().query().asFlow()

    override fun createChannel(user: User, displayName: String, onError: (Throwable) -> Unit) =
        amityChannelRepo.createChannel(displayName)
            .conversation(userId = user.uid)
            .build()
            .create()
            .toFlowable().asFlow()
            .catch {
                Log.e(
                    "ChatRepository",
                    "createChannel exception: ${it.localizedMessage}",
                    it
                )
                onError(it)
            }

    override fun getChannel(id: String, onError: (Throwable) -> Unit) =
        amityChannelRepo.getChannel(id).asFlow()
            .catch {
                Log.e(
                    "ChatRepository",
                    "getChannel exception: ${it.localizedMessage}",
                    it
                )
                onError(it)
            }

    override fun getHistory(id: String) =
        amityMessageRepo.getMessages(subChannelId = id).build().query()
            .asFlow()
            .catch {
                Log.e(
                    "ChatRepository",
                    "getHistory exception: ${it.localizedMessage}",
                    it
                )
                // todo show to user
            }

    override suspend fun postMessage(
        channelId: String,
        msg: String,
        onError: (Throwable) -> Unit
    ) {
        try {
            amityMessageRepo.createMessage(subChannelId = channelId).with().text(text = msg).build()
                .send().subscribe()
        } catch (e: Exception) {
            Log.e("ChatRepository", "postMessage exception: ${e.localizedMessage}", e)
            onError(e)
        }
    }

    override suspend fun leaveChannel(channelId: String) {
        amityChannelRepo.leaveChannel(channelId = channelId).subscribe() // TODO Handle errors
    }
}

// TODO
//class FakeChatsRepository @Inject constructor() : ChatRepository {
//
//}

