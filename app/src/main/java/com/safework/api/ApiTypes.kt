package com.safework.api

data class LoginResponse(
    val mensagem: String,
    val infos: UserInfo
) {
    data class UserInfo(
        val userId: String,
        val username: String,
        val email: String
    )
}

data class SingUpResponse(
    val mensagem: String? = null,
    val erro: String? = null
)

    data class RegisterIssueResponse(
    val id: String? = null,
    val erro: String? = null
)