package com.busslina.main_lib.core.commons

import com.busslina.main_lib.core.modules.ForegroundServiceBase
import com.busslina.main_lib.core.modules.WebSocketBase

class CommonsModules {

    companion object {

//        var mainClass: Class<*>? = null

        var foregroundService: ForegroundServiceBase? = null
        var websocket: WebSocketBase? = null

        fun clear() {
            foregroundService = null
            websocket = null
        }

    }
}