package com.structura.android.data

import kotlinx.serialization.Serializable

@Serializable
data class Job(
    val keyId: Long,
    val job: String,
    val description: String,
    val displayName: String,
    val address: String
)
