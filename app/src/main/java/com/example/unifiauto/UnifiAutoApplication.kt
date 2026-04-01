package com.example.unifiauto

import android.app.Application
import com.example.unifiauto.data.JobRepository

class UnifiAutoApplication : Application() {
    lateinit var repository: JobRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = JobRepository(this)
    }
}
