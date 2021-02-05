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
    var manuallyDisconnected = false

    /**
     * Constructor.
     */
    constructor(): super() {
        CommonsModules.websocket = this
    }

    /**
     * Inherited functions
     *
     * - 01 - Start
     * - 02 - Stop
     */

    //region
    /**
     * 01 - Start.
     */
    override fun start() {
        if (isStarted()) {
            return
        }
        resetState()

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
    //endregion

    /**
     * Functions
     *
     * - 01 - Connect
     * - 02 - Disconnect
     * - 03 - On socket connected
     * - 04 - Reset state
     */

    //region
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
            onSocketConnected(false)
        }

        socket.on("disconnect") {
            println("Websocket: disconnect")
            println("Manually disconnected: $manuallyDisconnected")
            if (manuallyDisconnected) {
                return@on
            }
            connected = false
            ruptureDisconnected = true
        }

        socket.on("reconnect") {
            println("Websocket: reconnect")
            ruptureDisconnected = false
            onSocketConnected(true)
        }

        socket.connect()
    }

    /**
     * 02 - Disconnect.
     */
    private fun disconnect() {
        manuallyDisconnected = true
        socket.disconnect()
        connected = false
        ruptureDisconnected = false
    }

    /**
     * 03 - On socket connected.
     */
    private fun onSocketConnected(reconnection: Boolean) {

    }

    /**
     * 04 - Reset state.
     */
    private fun resetState() {
        connected = false
        ruptureDisconnected = false
        manuallyDisconnected = false
    }
    //endregion


}