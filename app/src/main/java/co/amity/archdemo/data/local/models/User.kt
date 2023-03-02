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

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.lang.StringBuilder

// Non argument constructor needed for deserializing Firebase's documents
@Entity
data class User(
    @PrimaryKey(autoGenerate = false)
    var uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

/**
 * Return the user's initials to be used instead of a profile picture if the user hasn't set one.
 * If the name consists of 1 word, it will return the first 2 letters.
 * If the name consists of 2 or more words, it will return the first letter of the first 2 words.
 */
fun User.getInitials(): String {
    if (this.displayName.isNullOrBlank()) return ""
    val words = this.displayName.split(' ')
    if (words.size == 1) return words[0].take(2)
    val sb = StringBuilder()
    words.take(2).forEach { sb.append(it.first()) }
    return sb.toString()
}

@Dao
interface UsersDao {
    @Query("SELECT * FROM user LIMIT 10")
    fun users(): Flow<List<User>>

    @Insert
    suspend fun insertUser(item: User)

    @Insert
    suspend fun insertAll(list: List<User>)
}
