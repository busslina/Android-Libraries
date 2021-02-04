package com.busslina.main_lib.core.commons

import com.busslina.main_lib.core.modules.ForegroundServiceBase
import com.busslina.main_lib.core.modules.WebSocketBase

class CommonsModules {

    companion object {

        lateinit var foregroundService: ForegroundServiceBase
        lateinit var websocket: WebSocketBase

    }
}