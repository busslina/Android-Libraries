package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.commons.CommonsModules

abstract class WebSocketBase: ModuleBase {

    constructor(): super() {
        CommonsModules.websocket = this
    }
}