package com.busslina.main_lib.core.commons

import android.content.Context
import android.content.Intent
import com.busslina.main_lib.core.modules.ForegroundServiceBase

class Commons {

    companion object {

        const val DEFAULT_METHOD_CHANNEL_NAME               = "myFlutterApp"
        const val METHOD_CHANNEL_START_FOREGROUND_SERVICE   = "startForegoundService"
        const val METHOD_CHANNEL_STOP_FOREGROUND_SERVICE    = "stopForegoundService"
        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_STARTED  = "websocketServiceStarted"
        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_STOPPED  = "websocketServiceStopped"


        var preInitied = false

        lateinit var token: String

        fun preInit(token: String) {
            if (preInitied) {
                return
            }

            this.token = token

            preInitied = true
        }
    }
}