package com.example.unifiauto.data

import android.content.Context
import com.example.unifiauto.network.ImsApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class JobRepository(private val context: Context) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var api: ImsApiService = createApi(currentBaseUrl())

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    fun setAuthToken(token: String) {
        _authToken.value = token
    }

    fun setBaseUrl(baseUrl: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_BASE_URL, baseUrl)
            .apply()
        api = createApi(baseUrl)
    }

    fun currentBaseUrl(): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun refreshJobs(onComplete: ((Result<List<Job>>) -> Unit)? = null) {
        coroutineScope.launch {
            val token = _authToken.value
            if (token.isNullOrBlank()) {
                onComplete?.invoke(Result.failure(IllegalStateException("Not signed in")))
                return@launch
            }
            runCatching {
                api.getJobs("Bearer $token")
            }.onSuccess { jobs ->
                _jobs.value = jobs
                onComplete?.invoke(Result.success(jobs))
            }.onFailure {
                onComplete?.invoke(Result.failure(it))
            }
        }
    }

    private fun createApi(baseUrl: String): ImsApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }
                    .asConverterFactory("application/json".toMediaType())
            )
            .build()
        return retrofit.create(ImsApiService::class.java)
    }

    companion object {
        private const val PREFS = "ims_settings"
        private const val KEY_BASE_URL = "base_url"
        private const val DEFAULT_BASE_URL = "https://ims.example.com/"
    }
}
