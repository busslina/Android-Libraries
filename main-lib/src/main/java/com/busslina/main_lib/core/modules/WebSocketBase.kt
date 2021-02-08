package com.busslina.main_lib.core.modules

import android.content.Intent
import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.CommonsModules
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
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

    private var socket: Socket? = null

    private var connected = false
    private var ruptureDisconnected = false

    private var websocketId = -1

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
     * - 03 - Clear
     */

    //region
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
        clear()

        state = STATE_STOPPED
    }

    /**
     * 03 - Clear.
     */
    override fun clear() {
        connected = false
        ruptureDisconnected = false
        socket = null
    }
    //endregion

    /**
     * Functions
     *
     * - 01 - Connect
     * - 02 - Disconnect
     * - 03 - On socket connected
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


        socket!!.once("connect") {
            Commons.debug("Websocket: connect")
            onSocketConnected(false)
        }

        socket!!.on("disconnect") {
            Commons.debug("Websocket: disconnect")
            connected = false
            ruptureDisconnected = true

            // TODO: stuff
        }

        socket!!.io().on("reconnect") {
            Commons.debug("Websocket: reconnect")
            ruptureDisconnected = false
            onSocketConnected(true)
        }

        // Testing fake notification
        socket!!.on("fake-notification") {
            Commons.debug("Websocket: fake-notification")
            val ctx = CommonsModules.foregroundService!!
            val mainClass = Commons.mainActivity!!::class.java
            val intent = Intent(ctx, mainClass)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }

        socket!!.connect()
    }

    /**
     * 02 - Disconnect.
     */
    private fun disconnect() {
        socket!!.off()
        socket!!.disconnect()
    }

    /**
     * 03 - On socket connected.
     */
    private fun onSocketConnected(reconnection: Boolean) {

        // 1. Sending token & device type
        val mapData: Map<String, Any> = mapOf("token" to Commons.token!!, "deviceType" to Utils.getDeviceType())
        val data = JSONObject(mapData)
        socket!!.emit(Events.WS_SIGNAL_AUTH_TOKEN, data)

        // 2. Receive assigned id
        socket!!.once(Events.WS_SIGNAL_ID_ASSIGNATION) { (id) ->
            Commons.debug("Assignated id: $id")
            websocketId = id as Int
            connected = true

            // Advice Flutter part
            Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED, null)

            // TODO: stuff
        }

        // 3. Managing session killed
        socket!!.once(Events.WS_SIGNAL_SESSION_KILLED) {
            Commons.debug("Session killed")

            stop()
            ForegroundServiceBase.sessionKilled()
        }
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