package co.amity.archdemo.ui

sealed class Route(val route: String) {
    object Login: Route("login")
    object UsersList: Route("users")
    object Loading: Route("loading")
    object ChatsList: Route("chats")
}