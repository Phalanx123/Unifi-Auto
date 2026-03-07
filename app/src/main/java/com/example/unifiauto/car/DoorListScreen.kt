package com.example.unifiauto.car

import android.widget.Toast
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.unifiauto.R
import com.example.unifiauto.UnifiAutoApplication

class DoorListScreen(carContext: CarContext) : Screen(carContext) {
    private val repository = (carContext.applicationContext as UnifiAutoApplication).repository

    override fun onGetTemplate(): Template {
        val authToken = repository.authToken.value
        if (authToken.isNullOrBlank()) {
            return MessageTemplate.Builder(carContext.getString(R.string.login_required))
                .setTitle(carContext.getString(R.string.car_screen_title))
                .addAction(Action.APP_ICON)
                .build()
        }

        val entries = repository.accessPoints.value
        val listBuilder = ItemList.Builder()
        if (entries.isEmpty()) {
            listBuilder.addItem(Row.Builder().setTitle(carContext.getString(R.string.empty_doors)).build())
        } else {
            entries.forEach { door ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(door.name)
                        .addText(door.type)
                        .setOnClickListener {
                            repository.openDoor(door.id) { result ->
                                val message = result.fold(
                                    onSuccess = {
                                        carContext.getString(R.string.door_open_requested, door.name)
                                    },
                                    onFailure = {
                                        carContext.getString(R.string.door_open_failed, door.name)
                                    }
                                )
                                carContext.mainExecutor.execute {
                                    Toast.makeText(carContext, message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.car_screen_title))
            .setSingleList(listBuilder.build())
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle("Refresh")
                            .setOnClickListener {
                                repository.refreshDoors {
                                    invalidate()
                                }
                            }
                            .build()
                    )
                    .build()
            )
            .build()
    }
}
