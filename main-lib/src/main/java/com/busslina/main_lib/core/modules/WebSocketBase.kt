package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.commons.CommonsModules
import io.socket.client.IO
import io.socket.client.Socket
import java.lang.Exception

abstract class WebSocketBase: ModuleBase {

    companion object {
        var enableWebsocketSubModule = false
        lateinit var url: String

        private var preInitied = false

        fun preInit(enableWebsocketSubModule: Boolean, url: String) {
            if (preInitied) {
                return
            }
            this.enableWebsocketSubModule = enableWebsocketSubModule
            this.url = url
            preInitied = true
        }
    }

    lateinit var socket: Socket

    var connected = false
    var ruptureDisconnected = false

    constructor(): super() {
        CommonsModules.websocket = this
    }

    override fun start() {
        if (isStarted()) {
            return
        }

        connect()

        state = STATE_STARTED
    }

    override fun stop() {
        if (isStopped()) {
            return
        }

        disconnect()

        state = STATE_STOPPED
    }

    private fun connect() {
        if (!preInitied) {
            throw Exception("WebSocket not preinitied")
        }

        val options = IO.Options.builder()
                .build()

        socket = IO.socket(url, options)

        socket.once("connect") {
            println("Websocket: connect")
        }

        socket.on("disconnect") {
            println("Websocket: disconnect")
        }

        socket.on("reconnect") {
            println("Websocket: reconnect")
        }

        socket.connect()
    }

    private fun disconnect() {
        TODO()
    }


}