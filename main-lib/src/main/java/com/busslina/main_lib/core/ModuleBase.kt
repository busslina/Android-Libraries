package com.busslina.main_lib.core

abstract class ModuleBase {

    companion object {

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