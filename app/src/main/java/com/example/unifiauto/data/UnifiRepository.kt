package com.example.unifiauto.data

import android.content.Context
import com.example.unifiauto.network.UnifiApiService
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

class UnifiRepository(private val context: Context) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var api: UnifiApiService = createApi(currentBaseUrl())

    private val _accessPoints = MutableStateFlow<List<AccessPoint>>(emptyList())
    val accessPoints: StateFlow<List<AccessPoint>> = _accessPoints.asStateFlow()

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

    fun refreshDoors(onComplete: ((Result<List<AccessPoint>>) -> Unit)? = null) {
        coroutineScope.launch {
            val token = _authToken.value
            if (token.isNullOrBlank()) {
                onComplete?.invoke(Result.failure(IllegalStateException("No token available")))
                return@launch
            }
            runCatching {
                api.getDoors("Bearer $token").data
                    .filter { it.type.equals("door", true) || it.type.equals("gate", true) }
            }.onSuccess {
                _accessPoints.value = it
                onComplete?.invoke(Result.success(it))
            }.onFailure {
                onComplete?.invoke(Result.failure(it))
            }
        }
    }

    fun openDoor(id: String, onComplete: ((Result<Unit>) -> Unit)? = null) {
        coroutineScope.launch {
            val token = _authToken.value
            if (token.isNullOrBlank()) {
                onComplete?.invoke(Result.failure(IllegalStateException("No token available")))
                return@launch
            }
            runCatching {
                api.openDoor(id, "Bearer $token")
            }.onSuccess {
                onComplete?.invoke(Result.success(Unit))
            }.onFailure {
                onComplete?.invoke(Result.failure(it))
            }
        }
    }

    private fun currentBaseUrl(): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_BASE_URL, DEFAULT_BASE_URL)
            ?: DEFAULT_BASE_URL
    }

    private fun createApi(baseUrl: String): UnifiApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }
                    .asConverterFactory("application/json".toMediaType())
            )
            .build()
        return retrofit.create(UnifiApiService::class.java)
    }

    companion object {
        private const val PREFS = "unifi_settings"
        private const val KEY_BASE_URL = "base_url"
        private const val DEFAULT_BASE_URL = "https://unifi.example.com/"
    }
}
