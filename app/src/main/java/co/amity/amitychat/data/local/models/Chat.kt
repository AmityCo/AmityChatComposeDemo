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

package co.amity.amitychat.data.local.models

import com.amity.socialcloud.sdk.model.chat.channel.AmityChannel
import com.amity.socialcloud.sdk.model.core.file.AmityImage
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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
    DummyChat(
        "3",
        "Princess Leia",
        8,
        DateTime(2023, 3, 1, 10, 40),
        null,
        "Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis"
    ),
    DummyChat(
        "4",
        "Obi-Wan Kenobi",
        0,
        DateTime(2023, 3, 1, 11, 10),
        null,
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt."
    ),
    DummyChat("5", "Han Solo", 1, DateTime(2023, 3, 1, 12, 10), null, "Neque porro"),
)

/**
 * Return the chat's display name initials to be used instead if there is no avatar URL set.
 * If the name consists of 1 word, it will return the first 2 letters.
 * If the name consists of 2 or more words, it will return the first letter of the first 2 words.
 */
fun AmityChannel.getInitials(): String {
    if (this.getDisplayName().isBlank()) return ""
    val words = this.getDisplayName().split(' ')
    if (words.size == 1) return words[0].take(2)
    val sb = StringBuilder()
    words.take(2).forEach { sb.append(it.first()) }
    return sb.toString()
}

fun DummyChat.getInitials(): String {
    if (this.displayName.isBlank()) return ""
    val words = this.displayName.split(' ')
    if (words.size == 1) return words[0].take(2)
    val sb = StringBuilder()
    words.take(2).forEach { sb.append(it.first()) }
    return sb.toString()
}

fun DateTime.toReadableChatDate(): String {
    this.toDate()
    val serverDate = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        Locale.ROOT
    ) //example : 2023-06-12T10:10:17.091+02:00
    val sdfMsgToday = SimpleDateFormat("HH:mm", Locale.ROOT) // example: 10:10
    val sdfMsgWeek = SimpleDateFormat("EEE", Locale.ROOT) // example: Mon
    val sdfMsgYear = SimpleDateFormat("dd/MM", Locale.ROOT) // example: 12/06
    val sdfMsgOlder = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT) // example: 12/06/2023

    val now = Calendar.getInstance().time
    val parsedDate: Date = serverDate.parse(this.toString()) ?: return ""

    val calendarNow = Calendar.getInstance()
    val calendarMsgDate = Calendar.getInstance().apply { time = parsedDate }

    return when {
        isSameYear(calendar1 = calendarNow, calendar2 = calendarMsgDate) -> {
            if (isSameWeek(calendar1 = calendarNow, calendar2 = calendarMsgDate)) {
                if (isSameDay(calendar1 = calendarNow, calendar2 = calendarMsgDate)) {
                    sdfMsgToday.format(parsedDate)
                } else {
                    sdfMsgWeek.format(parsedDate)
                }
            } else {
                sdfMsgYear.format(parsedDate)
            }
        }

        else -> sdfMsgOlder.format(parsedDate)
    }
}

internal fun isSameYear(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
}

internal fun isSameWeek(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.WEEK_OF_YEAR) == calendar2.get(Calendar.WEEK_OF_YEAR)
}

internal fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
}