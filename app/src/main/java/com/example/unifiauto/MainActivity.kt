package com.example.unifiauto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.unifiauto.auth.AuthManager

class MainActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val repository = (application as UnifiAutoApplication).repository
        authManager = AuthManager(this)

        val statusText = findViewById<TextView>(R.id.statusText)
        val baseUrlInput = findViewById<EditText>(R.id.baseUrlInput)
        findViewById<Button>(R.id.signInButton).setOnClickListener {
            authManager.signIn(this) { result ->
                runOnUiThread {
                    result.onSuccess { auth ->
                        repository.setAuthToken(auth.accessToken)
                        statusText.text = getString(
                            R.string.signed_in_as,
                            auth.account?.username ?: "Unknown"
                        )
                        repository.refreshDoors()
                    }.onFailure {
                        statusText.text = it.message ?: "Sign in failed"
                    }
                }
            }
        }

        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            val baseUrl = baseUrlInput.text.toString().trim()
            if (baseUrl.isNotBlank()) {
                repository.setBaseUrl(baseUrl)
            }
            repository.refreshDoors { result ->
                runOnUiThread {
                    statusText.text = result
                        .fold(
                            onSuccess = { "Loaded ${it.size} entries for car screen" },
                            onFailure = { it.message ?: "Refresh failed" }
                        )
                }
            }
        }
    }
}
