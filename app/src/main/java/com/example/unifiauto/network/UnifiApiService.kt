package com.example.unifiauto.network

import com.example.unifiauto.data.AccessPointResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface UnifiApiService {
    @GET("api/v1/developer/doors")
    suspend fun getDoors(
        @Header("Authorization") bearerToken: String
    ): AccessPointResponse

    @POST("api/v1/developer/doors/{id}/open")
    suspend fun openDoor(
        @Path("id") id: String,
        @Header("Authorization") bearerToken: String,
        @Body payload: Map<String, String> = mapOf("source" to "android-auto")
    )
}
