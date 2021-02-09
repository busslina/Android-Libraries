package com.busslina.main_lib.core.interfaces

import android.content.Intent

interface MainActivityI {

    companion object {

    }

    var foregroundServiceIntent: Intent

    fun initMethodChannel()
    fun sendMessageMethodChannel(method: String, args: Any?)

}