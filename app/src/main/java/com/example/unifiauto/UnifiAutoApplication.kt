package com.example.unifiauto

import android.app.Application
import com.example.unifiauto.data.UnifiRepository

class UnifiAutoApplication : Application() {
    lateinit var repository: UnifiRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = UnifiRepository(this)
    }
}
