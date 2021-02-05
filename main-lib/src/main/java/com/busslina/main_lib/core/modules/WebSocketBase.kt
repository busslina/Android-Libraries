package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.commons.CommonsModules
import io.socket.client.IO
import io.socket.client.Socket
import java.lang.Exception

abstract class WebSocketBase: ModuleBase() {

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

    /**
     * Constructor.
     */
//    constructor(): super() {
//        CommonsModules.websocket = this
//    }

    /**
     * Inherited functions
     *
     * - 01 - Start
     * - 02 - Stop
     */

    /**
     * 01 - Start.
     */
    override fun start() {
        if (isStarted()) {
            return
        }

        connect()

        state = STATE_STARTED
    }

    /**
     * 02 - Stop.
     */
    override fun stop() {
        if (isStopped()) {
            return
        }

        disconnect()

        state = STATE_STOPPED
    }

    /**
     * Functions
     *
     * - 01 - Connect
     * - 02 - Disconnect
     */

    /**
     * 01 - Connect.
     */
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

    /**
     * 02 - Disconnect.
     */
    private fun disconnect() {
        TODO()
    }


}