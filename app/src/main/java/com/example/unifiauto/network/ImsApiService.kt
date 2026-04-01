package com.example.unifiauto.network

import com.example.unifiauto.data.Job
import retrofit2.http.GET
import retrofit2.http.Header

interface ImsApiService {
    @GET("api/jobs")
    suspend fun getJobs(
        @Header("Authorization") bearerToken: String
    ): List<Job>
}
