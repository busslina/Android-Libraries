package com.busslina.main_lib.core.interfaces

import android.content.Intent

interface MainActivityI {

    companion object {

    }

    var foregroundServiceIntent: Intent

    fun initChannelMethod()

    fun startForegroundService()
    fun stopForegroundService()


}