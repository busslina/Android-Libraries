package com.busslina.main_lib.core.commons

import android.app.Activity
import android.content.Context
import com.busslina.main_lib.core.modules.ForegroundServiceBase
import com.busslina.main_lib.core.modules.WebSocketBase

class CommonsModules {

    companion object {

//        var mainActivity: Activity? = null

        var appContext: Context? = null
        var foregroundService: ForegroundServiceBase? = null
        var websocket: WebSocketBase? = null

        fun clear() {
            foregroundService = null
            websocket = null
        }

    }
}