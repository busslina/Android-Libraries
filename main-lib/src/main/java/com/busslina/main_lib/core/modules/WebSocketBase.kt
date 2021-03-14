package com.busslina.main_lib.core.modules

import android.content.Intent
import android.util.Log
import com.busslina.main_lib.core.ModuleBase
import com.busslina.main_lib.core.Semaphore
import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.Commons.Companion.debug
import com.busslina.main_lib.core.commons.CommonsModules
import com.busslina.main_lib.core.commons.DebugM
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.Exception

abstract class WebSocketBase: ModuleBase() {

    companion object {
        var authenticationRequired = false
        var enableWebsocketSubModule = false
        lateinit var url: String

        private var preInitied = false

        fun preInit(enableWebsocketSubModule: Boolean, url: String, authenticationRequired: Boolean) {
            if (preInitied) {
                return
            }
            this.enableWebsocketSubModule = enableWebsocketSubModule
            this.url = url
            this.authenticationRequired = authenticationRequired
            preInitied = true
        }
    }

    private var socket: Socket? = null

    private var connected = false
    private var ruptureDisconnected = false

    private var websocketId = -1

    val semaphore = Semaphore()

    /**
     * Constructor.
     */
    init {
//        Log.i("WebSocketBase", "constructor()")
        DebugM.send("WebSocketBase", "constructor()")
        CommonsModules.websocket = this
    }

    /**
     * Inherited functions
     *
     * - 01 - Start
     * - 02 - Stop
     * - 03 - Clear
     */

    /**
     * 01 - Start.
     */
    override fun start() {
//        Log.i("WebSocketBase", "start()")
        DebugM.send("WebSocketBase", "start()")
        if (isStarted()) {
            return
        }
        debug("Websocket start")

        connect()

        state = STATE_STARTED
    }

    /**
     * 02 - Stop.
     */
    override fun stop() {
//        Log.i("WebSocketBase", "stop()")
        DebugM.send("WebSocketBase", "stop()")
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
//        Log.i("WebSocketBase", "clear()")
        DebugM.send("WebSocketBase", "clear()")
        connected = false
        ruptureDisconnected = false
        socket = null
    }

    /**
     * Functions
     *
     * - 01 - Connect
     * - 02 - Disconnect
     * - 03 - On socket connected
     * - 04 - Emit
     * - 05 - Is connected
     * - 06 - Is rupture connected
     * - 07 - Check Debug Manager pending messages
     */

    /**
     * 01 - Connect.
     */
    private fun connect() {
        DebugM.send("WebSocketBase", "connect()")
        if (!preInitied) {
            throw Exception("WebSocket not preinitied")
        }
//        debug("Websocket connect")

        val options = IO.Options.builder()
                .setForceNew(false)
//                .setReconnection(true)
                .setReconnection(false)
                .build()

        socket = IO.socket(url, options)

        // Connect event
        socket!!.once("connect") {
            DebugM.send("WebSocketBase", "connect event")

            onSocketConnected(false)
        }

        // Error event
        // TESTING
        socket!!.once("error") {
            DebugM.send("WebSocketBase", "error event")
        }

        // Connect error event
        // TESTING
        socket!!.once("connect_error") {
            DebugM.send("WebSocketBase", "connect error event")
        }

        // Disconnect event
        socket!!.on("disconnect") {
            DebugM.send("WebSocketBase", "disconnect event)")
//            debug("Websocket: disconnect")
            connected = false
            ruptureDisconnected = true

            // TODO: stuff
        }

        // Reconnect event
        socket!!.io().on("reconnect") {
            DebugM.send("WebSocketBase", "reconnect event")
//            debug("Websocket: reconnect")
            ruptureDisconnected = false
            onSocketConnected(true)
        }

        // Testing fake notification
        socket!!.on("fake-notification") {
//            debug("Websocket: fake-notification")
//            val ctx = CommonsModules.foregroundService!!
//            val intent = Intent(ctx, Commons.mainActivityClass)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            ctx.startActivity(intent)
            messageReceived("Pedro", "Hola mundo!")
        }

        socket!!.connect()
    }

    /**
     * 02 - Disconnect.
     */
    private fun disconnect() {
        DebugM.send("WebSocketBase", "disconnect()")
        socket!!.off()
        socket!!.disconnect()
    }

    /**
     * 03 - On socket connected.
     */
    private fun onSocketConnected(reconnection: Boolean) {
        DebugM.send("WebSocketBase", "onSocketConnected()")
//        debug("Websocket onSocketConnected")

        if (authenticationRequired) {
            // Authentication required
            // 1. Sending token & device type
            val mapData: Map<String, Any> = mapOf("token" to ForegroundServiceBase.token!!, "deviceType" to Utils.getDeviceType())
            val data = JSONObject(mapData)
            socket!!.emit(Events.WS_SIGNAL_AUTH_TOKEN, data)

            // 2. Receive assigned id
            socket!!.once(Events.WS_SIGNAL_ID_ASSIGNATION) { (id) ->
                debug("Assignated id: $id")
                websocketId = id as Int
                connected = true

                // Advice Flutter part
                Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED)

                // Debug Manager pending messages
                checkDebugManagerPendingMessages()

                // Custom event handler
                initCustomEventHandler(socket!!)

                // After socket connected
                afterSocketConnected()

                // TODO: stuff
            }

            // 3. Managing session killed
            socket!!.once(Events.WS_SIGNAL_SESSION_KILLED) {
                debug("Session killed")

                stop()
                ForegroundServiceBase.sessionKilled()
            }
        } else {
            // Authentication not required
            connected = true
            if (!reconnection) {
//                CommonsModules.websocket!!.emit("message", "[INFO]: Websocket -- connect (first time)")
                DebugM.send(message = "[INFO]: Websocket -- connect (first time)")
            }

            // Advice Flutter part
            Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED)

            // Debug Manager pending messages
            checkDebugManagerPendingMessages()

            // Custom event handler
            initCustomEventHandler(socket!!)

            // After socket connected
            afterSocketConnected()
        }

        // Managing close app (CLOSE APP AND/OR FOREGROUND SERVICE)
        socket!!.once(Events.WS_SIGNAL_CLOSE_APP) {
            TODO()
        }
    }

    /**
     * 04 - Emit.
     */
    fun emit(event: String, data: Any? = "", lockSemaphore: Boolean = true): Boolean {
//        Log.v("WebSocketBase", "emit()")
        if (!connected) {
//            debug("Cannot emit because is disconnected")
            return false
        }
        GlobalScope.launch {
            if (lockSemaphore) {
                val ticket = semaphore.getTicketAndWait()
                socket!!.emit(event, data)
                ticket.release()
            } else {
                socket!!.emit(event, data)
            }
        }
        return true
    }

    /**
     * 05 - Is connected.
     */
    fun isConnected(): Boolean {
        return connected
    }

    /**
     * 06 - Is rupture connected.
     */
    fun isRuptureConnected(): Boolean {
        return ruptureDisconnected
    }

    /**
     * 07 - Check Debug Manager pending messages.
     */
    fun checkDebugManagerPendingMessages() {
        DebugM.send("WebSocketBase", "checkDebugManagerPendingMessages(")
        DebugM.resolveStoredMessages()
    }
    /**
     * Abstract functions
     *
     * - 01 - Init custom event handler
     * - 02 - After socket connected
     * - 03 - Message received
     */

    /**
     * 01 - Init custom event handler.
     */
    abstract fun initCustomEventHandler(socket: Socket)

    /**
     * 02 - After socket connected.
     */
    abstract fun afterSocketConnected()

    /**
     * 03 - Message received.
     */
    abstract fun messageReceived(sender: String, msg: String)
}

class Events {
    companion object {

        // Core
        const val WS_SIGNAL_AUTH_TOKEN      = "auth-token"
        const val WS_SIGNAL_ID_ASSIGNATION  = "assigned-id"
        const val WS_SIGNAL_SESSION_KILLED  = "session-killed"

        // Debug module
        const val WS_SIGNAL_DEBUG_M_MESSAGE = "debug-module-message"

        // TODO
        const val WS_SIGNAL_CLOSE_APP       = "close-app"
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