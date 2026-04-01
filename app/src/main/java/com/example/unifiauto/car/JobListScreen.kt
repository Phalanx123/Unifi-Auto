package com.example.unifiauto.car

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.unifiauto.UnifiAutoApplication

class JobListScreen(carContext: CarContext) : Screen(carContext) {
    private val repository = (carContext.applicationContext as UnifiAutoApplication).repository

    override fun onGetTemplate(): Template {
        if (repository.authToken.value.isNullOrBlank()) {
            return MessageTemplate.Builder("Please sign in via the phone app to view jobs.")
                .setTitle("IMS Jobs")
                .addAction(Action.APP_ICON)
                .build()
        }

        val jobs = repository.jobs.value
        val listBuilder = ItemList.Builder()

        if (jobs.isEmpty()) {
            listBuilder.addItem(
                Row.Builder().setTitle("No jobs found. Tap Refresh.").build()
            )
        } else {
            jobs.forEach { job ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(job.displayName)
                        .addText(job.address)
                        .setOnClickListener { navigateToAddress(job.address) }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle("IMS Jobs")
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Refresh")
                            .setOnClickListener {
                                repository.refreshJobs { invalidate() }
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun navigateToAddress(address: String) {
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        carContext.startCarApp(Intent(CarContext.ACTION_NAVIGATE, uri))
    }
}
