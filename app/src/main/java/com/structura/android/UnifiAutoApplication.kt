package com.structura.android

import android.app.Application
import com.structura.android.data.JobRepository

class UnifiAutoApplication : Application() {
    lateinit var repository: JobRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = JobRepository(this)
    }
}
