package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.commons.Commons
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

    var websocketId = -1

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
                .setForceNew(false)
                .setReconnection(true)
                .build()

        socket = IO.socket(url, options)


        socket.once("connect") {
            println("Websocket: connect")
            onSocketConnected(false)
        }

        socket.on("disconnect") {
            println("Websocket: disconnect")
            connected = false
            ruptureDisconnected = true

            // TODO: stuff
        }

        socket.io().on("reconnect") {
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
        socket.off("disconnect")
        socket.disconnect()
        resetState()
    }

    /**
     * 03 - On socket connected.
     */
    private fun onSocketConnected(reconnection: Boolean) {

        // 1. Sending token & device type
        val data: Map<String, Any> = mapOf("token" to Commons.token, "deviceType" to Utils.getDeviceType())
        socket.emit(Events.WS_SIGNAL_AUTH_TOKEN, data)

        // 2. Receive assigned id
        socket.once(Events.WS_SIGNAL_ID_ASSIGNATION) { (id) ->
            println("Assignated id: $id")
            websocketId = id as Int
            connected = true

            // TODO: stuff
        }

        // 3. Managing session killed
        socket.once(Events.WS_SIGNAL_SESSION_KILLED) {
            println("Session killed")

            // TODO: stuff
        }


    }

    /**
     * 04 - Reset state.
     */
    private fun resetState() {
        connected = false
        ruptureDisconnected = false
    }
    //endregion


}

private class Events {

    companion object {

        const val WS_SIGNAL_AUTH_TOKEN      = "auth-token"
        const val WS_SIGNAL_ID_ASSIGNATION  = "assigned-id"
        const val WS_SIGNAL_SESSION_KILLED  = "session-killed"
    }
}

private class Utils {

    companion object {

        const val DEVICE_ANGULAR: Byte  = 0
        const val DEVICE_ANDROID: Byte  = 1
        const val DEVICE_IOS: Byte      = 2
        const val DEVICE_OTHER: Byte    = 3

        fun getDeviceType(): Byte {
            return DEVICE_ANDROID
        }
    }
}