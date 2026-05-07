package com.assetcoach.data.repo

import com.assetcoach.data.db.dao.UserProfileDao
import com.assetcoach.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val dao: UserProfileDao) {

    fun observe(): Flow<UserProfileEntity?> = dao.observe()

    suspend fun get(): UserProfileEntity? = dao.get()

    suspend fun save(profile: UserProfileEntity) = dao.upsert(profile)

    suspend fun reset() = dao.deleteAll()
}
