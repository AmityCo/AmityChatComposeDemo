package co.amity.archdemo.data.remote

import androidx.paging.PagedList
import androidx.paging.PagingData
import co.amity.archdemo.data.local.models.ApiResponse
import co.amity.archdemo.data.local.models.User
import com.amity.socialcloud.sdk.AmityCoreClient
import com.amity.socialcloud.sdk.chat.AmityChatClient
import com.amity.socialcloud.sdk.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.chat.channel.AmityChannelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import javax.inject.Inject

interface ChatRepository {
    val chats: Flow<PagedList<AmityChannel>>
}

class RemoteChatsRepository @Inject constructor() : ChatRepository {
    val amityChannelRepo = AmityChatClient.newChannelRepository()
    init {
        AmityCoreClient.registerDeviceForPushNotification()
    }

    override val chats = amityChannelRepo.getChannels().all().build().query().asFlow()
}

// TODO
//class FakeChatsRepository @Inject constructor() : ChatRepository {
//
//}

