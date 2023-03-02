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

package co.amity.archdemo.data.local.models

import android.util.Log
import com.amity.socialcloud.sdk.core.file.AmityImage
import org.joda.time.DateTime
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DummyChat(
    val channelId: String,
    val displayName: String,
    val unreadCount: Int,
    val updatedAt: DateTime,
    val avatar: AmityImage?,
    val latestMessage: String
)

val dummyChats = listOf(
    DummyChat("0", "Darth Vader", 0, DateTime(2023, 3, 1, 10, 10), null, "Test 0: latest message"),
    DummyChat("1", "R2D2", 5, DateTime(2023, 3, 1, 10, 20), null, "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."),
    DummyChat("2", "Luke Skywalker", 23, DateTime(2023, 3, 1, 10, 30), null, "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa."),
    DummyChat("3", "Princess Leia", 8, DateTime(2023, 3, 1, 10, 40), null, "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis"),
    DummyChat("4", "Obi-Wan Kenobi", 0, DateTime(2023, 3, 1, 11, 10), null, "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt."),
    DummyChat("5", "Han Solo", 1, DateTime(2023, 3, 1, 12, 10), null, "Neque porro"),
)

/**
 * Return the chat's display name initials to be used instead if there is no avatar URL set.
 * If the name consists of 1 word, it will return the first 2 letters.
 * If the name consists of 2 or more words, it will return the first letter of the first 2 words.
 */
fun DummyChat.getInitials() : String {
    if (this.displayName.isBlank()) return ""
    val words = this.displayName.split(' ')
    if (words.size == 1) return words[0].take(2)
    val sb = StringBuilder()
    words.take(2).forEach { sb.append(it.first()) }
    return sb.toString()
}

fun DateTime.toReadableChatDate(): String {
    this.toDate()
    val sdfMsgToday = SimpleDateFormat("hh:mm", Locale.ROOT)
    val sdfMsgWeek = SimpleDateFormat("EEE", Locale.ROOT)
    val sdfMsgYear = SimpleDateFormat("dd/m", Locale.ROOT)
    val sdfMsgOlder = SimpleDateFormat("dd/mmmm/yyyy", Locale.ROOT)

    //TODO

    return ""
}