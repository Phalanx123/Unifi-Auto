package com.structura.android.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AuthManager(context: Context) {
    private var accountApp: ISingleAccountPublicClientApplication? = null

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            com.structura.android.R.raw.auth_config_single_account,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    accountApp = application
                }

                override fun onError(exception: MsalException) {
                    Log.e("AuthManager", "MSAL init failed", exception)
                }
            }
        )
    }

    fun signIn(activity: Activity, callback: (Result<IAuthenticationResult>) -> Unit) {
        val application = accountApp ?: run {
            callback(Result.failure(IllegalStateException("MSAL not initialized")))
            return
        }
        val params = AcquireTokenParameters.Builder()
            .startAuthorizationFromActivity(activity)
            .withScopes(listOf("User.Read", "api://unifi/.default"))
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    callback(Result.success(authenticationResult))
                }

                override fun onError(exception: MsalException) {
                    callback(Result.failure(exception))
                }

                override fun onCancel() {
                    callback(Result.failure(IllegalStateException("User cancelled sign in")))
                }
            })
            .build()
        application.signIn(params)
    }

    suspend fun acquireTokenSilently(): String? = suspendCancellableCoroutine { continuation ->
        val application = accountApp
        if (application == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }
        application.getCurrentAccountAsync(object : ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: com.microsoft.identity.client.IAccount?) {
                if (activeAccount == null) {
                    continuation.resume(null)
                    return
                }
                application.acquireTokenSilentAsync(
                    arrayOf("api://unifi/.default"),
                    activeAccount.authority,
                    object : SilentAuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            continuation.resume(authenticationResult.accessToken)
                        }

                        override fun onError(exception: MsalException) {
                            continuation.resume(null)
                        }
                    }
                )
            }

            override fun onAccountChanged(
                priorAccount: com.microsoft.identity.client.IAccount?,
                currentAccount: com.microsoft.identity.client.IAccount?
            ) = Unit

            override fun onError(exception: MsalException) {
                continuation.resume(null)
            }
        })
    }
}
