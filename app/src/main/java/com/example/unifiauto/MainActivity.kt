package com.example.unifiauto

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.unifiauto.auth.AuthManager

// NOTE: AuthManager.kt requests scope "api://unifi/.default".
// Before deploying, update that scope in AuthManager.kt to:
//   api://<IMS_AZURE_AD_CLIENT_ID>/access_as_user
// where IMS_AZURE_AD_CLIENT_ID is the ClientId from IMS appsettings.json.

class MainActivity : AppCompatActivity() {
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val app = application as UnifiAutoApplication
        authManager = AuthManager(this)

        val statusText = findViewById<TextView>(R.id.statusText)
        val baseUrlInput = findViewById<EditText>(R.id.baseUrlInput)
        val signInButton = findViewById<Button>(R.id.signInButton)
        val refreshButton = findViewById<Button>(R.id.refreshButton)

        baseUrlInput.setText(app.repository.currentBaseUrl())

        signInButton.setOnClickListener {
            statusText.text = "Signing in..."
            authManager.signIn(this) { result ->
                result.fold(
                    onSuccess = { authResult ->
                        app.repository.setAuthToken(authResult.accessToken)
                        app.repository.setBaseUrl(baseUrlInput.text.toString().trimEnd('/') + "/")
                        statusText.text = "Signed in. Loading jobs..."
                        app.repository.refreshJobs { jobResult ->
                            runOnUiThread {
                                jobResult.fold(
                                    onSuccess = { jobs ->
                                        statusText.text = "Loaded ${jobs.size} jobs. Open Android Auto to navigate."
                                    },
                                    onFailure = { e ->
                                        statusText.text = "Error loading jobs: ${e.message}"
                                    }
                                )
                            }
                        }
                    },
                    onFailure = { e ->
                        runOnUiThread { statusText.text = "Sign in failed: ${e.message}" }
                    }
                )
            }
        }

        refreshButton.setOnClickListener {
            app.repository.setBaseUrl(baseUrlInput.text.toString().trimEnd('/') + "/")
            statusText.text = "Refreshing..."
            app.repository.refreshJobs { result ->
                runOnUiThread {
                    result.fold(
                        onSuccess = { jobs -> statusText.text = "Loaded ${jobs.size} jobs." },
                        onFailure = { e -> statusText.text = "Error: ${e.message}" }
                    )
                }
            }
        }
    }
}
