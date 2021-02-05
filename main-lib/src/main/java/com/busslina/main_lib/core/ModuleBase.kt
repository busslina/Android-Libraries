package com.busslina.main_lib.core

import com.busslina.main_lib.core.modules.ForegroundServiceBase

abstract class ModuleBase {

    companion object {

        lateinit var factoryObj: ModuleBase

        const val STATE_STOPPED = 0
        const val STATE_STARTED = 1
    }

    var state = STATE_STOPPED

    fun isStopped(): Boolean {
        return state == STATE_STOPPED
    }

    fun isStarted(): Boolean {
        return state == STATE_STARTED
    }

    open fun start() {
        TODO("Optional implementation on child class")
    }

    open fun stop() {
        TODO("Optional implementation on child class")
    }
}