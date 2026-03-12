package com.gastosapp.data.repository

import com.gastosapp.data.local.UserDao
import com.gastosapp.data.local.UserEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    suspend fun login(email: String, password: String): UserEntity? {
        return userDao.login(email, password)
    }

    suspend fun register(user: UserEntity): Boolean {
        if (userDao.getUserByEmail(user.email) != null) {
            return false
        }
        userDao.insert(user)
        return true
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
}
