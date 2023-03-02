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

package co.amity.archdemo.data.remote

import co.amity.archdemo.data.local.models.ApiResponse
import co.amity.archdemo.data.local.models.User
import co.amity.archdemo.data.local.models.UsersDao
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

interface UsersRepository {
    val users: Flow<ApiResponse<List<User?>>>
    suspend fun add(user: User)
}

class DefaultUsersRepository @Inject constructor(
    private val usersDao: UsersDao
): UsersRepository {
    val firestore = Firebase.firestore
    val usersCollection = "users"

        override val users = flow {
        emit(ApiResponse.Loading)
        val list = firestore.collection(usersCollection).get().await().documents.map { it.toObject(User::class.java) }
        emit(ApiResponse.Success(list))
    }.catch {
        emit(ApiResponse.Failure(Exception(it.localizedMessage)))
    }

    override suspend fun add(user: User) {
        firestore.collection(usersCollection).document(user.uid).set(user).await()
    }
}

class FakeUsersRepository @Inject constructor() : UsersRepository {
    override val users = flowOf(ApiResponse.Success(fakeUsers))

    override suspend fun add(user: User) {
        throw NotImplementedError()
    }
}

val fakeUsers = listOf(
    User("0", "fake", "fake@fake.com", null),
    User("1", "Jane Doe", null, null)
)

