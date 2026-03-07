package com.example.unifiauto.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccessPoint(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("type") val type: String
)

@Serializable
data class AccessPointResponse(
    @SerialName("data") val data: List<AccessPoint>
)
