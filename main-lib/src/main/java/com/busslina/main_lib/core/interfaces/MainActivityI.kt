package com.busslina.main_lib.core.interfaces

import android.content.Intent

interface MainActivityI {

    var foregroundServiceIntent: Intent

    fun startForegroundService()
    fun stopForegroundService()


}