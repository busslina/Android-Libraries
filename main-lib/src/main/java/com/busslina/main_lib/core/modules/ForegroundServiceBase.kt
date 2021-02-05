package com.busslina.main_lib.core.modules

import android.app.Service
import com.busslina.main_lib.core.commons.CommonsModules

abstract class ForegroundServiceBase: Service {

    companion object {

        const val NOTIFICATION_ID = 101
        const val STATE_STOPPED = 0
        const val STATE_STARTED = 1

        fun isStopped(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStopped()
        }

        fun isStarted(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStarted()
        }
    }

    var state = STATE_STOPPED

    /**
     * Constructor.
     */
    constructor(): super() {
        CommonsModules.foregroundService = this
    }

    /**
     * Functions
     *
     * - 01 - Is stopped
     * - 02 - Is started
     */

    //region
    /**
     * 01 - Is stopped.
     */
    fun isStopped(): Boolean {
        return state == STATE_STOPPED
    }

    /**
     * 02 - Is started.
     */
    fun isStarted(): Boolean {
        return state == STATE_STARTED
    }
    //endregion
}