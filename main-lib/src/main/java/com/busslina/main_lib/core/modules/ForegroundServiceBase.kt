package com.busslina.main_lib.core.modules

import android.app.Service
import com.busslina.main_lib.core.commons.CommonsModules

abstract class ForegroundServiceBase: Service {

    constructor(): super() {
        CommonsModules.foregroundService = this
    }
}