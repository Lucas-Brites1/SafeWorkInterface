package com.safework.models

import android.annotation.SuppressLint
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class IssueModel(
    val _id: String? = null,
    val user: IssueUser,
    val level: IssueLevel,
    val status: IssueStatus,
    val mapLocal: LocationModel,
    val title: String,
    val description: String,
    val comments: String? = null,
    val createdAt: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class IssueUser(
    val username: String,
    val email: String,
    val userId: String
)

@Serializable
enum class IssueStatus {
    ANALISE,
    PENDENTE,
    ANDAMENTO,
    FINALIZADA
}

@Serializable
enum class IssueLevel {
    GRAVE,
    MEDIO,
    LEVE
}

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)
