package com.busslina.main_lib.core.commons

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.busslina.main_lib.core.interfaces.MainActivityI
import com.busslina.main_lib.core.modules.ForegroundServiceBase

class Commons {

    companion object {

        const val DEFAULT_METHOD_CHANNEL_NAME                   = "myFlutterApp"
        const val METHOD_CHANNEL_START_FOREGROUND_SERVICE       = "startForegoundService"
        const val METHOD_CHANNEL_STOP_FOREGROUND_SERVICE        = "stopForegoundService"
        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED    = "websocketServiceConnected"
//        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_DISCONNECTED = "websocketServiceDisconnected"
//        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_RESCONNECTED = "websocketServiceReconnected"

        var token: String? = null
        var mainActivity: MainActivityI? = null

        var preInitied = false

        fun preInit(mainActivity: MainActivityI, token: String) {
            if (preInitied) {
                return
            }

            this.mainActivity = mainActivity
            this.token = token

            preInitied = true
        }

        fun clear() {
            token = null
            mainActivity = null
        }

        fun sendMessageMethodChannel(method: String, args: Any?) {
            Handler(Looper.getMainLooper()).post {
                mainActivity!!.sendMessageMethodChannel(method, args)
            }
        }






    }
}