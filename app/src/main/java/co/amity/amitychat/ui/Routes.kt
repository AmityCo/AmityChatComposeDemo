package co.amity.amitychat.ui

sealed class Route(val route: String) {
    object Login : Route("login")
    object UsersList : Route("users")
    object Loading : Route("loading")
    object ChatsList : Route("chats")
    object Conversation : Route("conversation/{channelId}") {
        fun createRoute(channelId: String): String = "conversation/$channelId"
    }
}