package co.amity.amitychat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.amity.amitychat.R
import co.amity.amitychat.data.local.models.User
import co.amity.amitychat.data.local.models.getInitials
import coil.compose.AsyncImage
import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel


@Composable
fun UserAvatar(user: User, size: Dp = 38.dp) {
    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
    if (user.photoUrl.isNullOrBlank()) UserInitialsAvatar(
        user,
        modifier
    ) else PhotoAvatar(user.photoUrl, modifier)
}

@Composable
private fun UserInitialsAvatar(user: User, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Text(text = user.getInitials(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ChatAvatar(chat: AmityChannel?) {
    val modifier = Modifier
        .size(64.dp)
        .clip(CircleShape)
    if (chat == null) return
    if (chat.getAvatar()?.getUrl().isNullOrBlank())
        ChatInitialsAvatar(chat, modifier)
    else PhotoAvatar(chat.getAvatar()!!.getUrl()!!, modifier)
}

@Composable
private fun ChatInitialsAvatar(chat: AmityChannel, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Text(text = chat.getInitials(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PhotoAvatar(photoUrl: String, modifier: Modifier) {
    AsyncImage(
        model = photoUrl,
        contentDescription = stringResource(id = R.string.cd_avatar),
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}