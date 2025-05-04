package com.safework.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class User(
    @Contextual
    val _id: ObjectId? = null,
    val email: String,
    val username: String,
    val password: String,
    val role: Role? = Role.USER
) {
    @Serializable
    data class LoginUserModel(
        val email: String,
        val password: String
    )

    @Serializable
    data class SignUpUserModel(
        val username: String,
        val email: String,
        val password: String
    )

    @Serializable
    enum class Role {
        DEV,
        ADMIN,
        USER
    }
}
