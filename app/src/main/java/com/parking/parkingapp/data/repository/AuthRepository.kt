package com.parking.parkingapp.data.repository

import com.parking.parkingapp.common.Resource
import com.parking.parkingapp.data.entity.FireUserEntity

interface AuthRepository {
    suspend fun <T> getCurrentUser(): Resource<T>
    suspend fun login(
        email: String,
        pass: String
    ): Resource<FireUserEntity>

    suspend fun logout(): Resource<Unit>
    suspend fun register(
        email: String,
        pass: String,
    ): Resource<FireUserEntity>

    suspend fun updatePass(pass: String): Resource<Unit>
    suspend fun updateProfile(
        name: String?,
        email: String?,
        image: String?
    ): Resource<Unit>
}