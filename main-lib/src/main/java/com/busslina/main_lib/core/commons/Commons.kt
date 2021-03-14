package com.busslina.main_lib.core.commons

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.busslina.main_lib.Utils
import com.busslina.main_lib.core.Semaphore
import com.busslina.main_lib.core.interfaces.MainActivityI
import com.busslina.main_lib.core.modules.Auth
import com.busslina.main_lib.core.modules.ForegroundServiceBase
import com.busslina.main_lib.core.modules.WebSocketBase
import com.busslina.main_lib.core.modules.Events
import com.busslina.main_lib.core.modules.Events.Companion.WS_SIGNAL_DEBUG_M_MESSAGE
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class Commons {

    companion object {

        const val MODE_DEBUG = true
        const val MODE_DEBUG_ALL = 1
        const val MODE_DEBUG_INFO = 2

        const val MODE_DEBUG_USED = MODE_DEBUG_ALL



        val methodChannelSemaphore = Semaphore()

        // Method channel
        //region
        const val DEFAULT_METHOD_CHANNEL_NAME                   = "myFlutterApp"
        // Flutter      TO      Android
        const val METHOD_CHANNEL_METHOD_CHANNEL_INITIED         = "methodChannelInitied"
        const val METHOD_CHANNEL_START_FOREGROUND_SERVICE       = "startForegoundService"
        const val METHOD_CHANNEL_STOP_FOREGROUND_SERVICE        = "stopForegoundService"
        const val METHOD_CHANNEL_IS_FOREGROUND_SERVICE_STARTED  = "isForegroundServiceStarted"
        const val METHOD_CHANNEL_GET_HP_PENDING_OPERATION       = "getHighPriorityPendingOperation"
        const val METHOD_CHANNEL_GET_LP_PENDING_OPERATION       = "getLowPriorityPendingOperation"
        const val METHOD_CHANNEL_ENABLE_SCREEN_LOCK             = "enableScreenLock"
        const val METHOD_CHANNEL_DISABLE_SCREEN_LOCK            = "disableScreenLock"

        // Android      TO      Flutter
        const val METHOD_CHANNEL_PERMISSIONS_GRANTED            = "permissionsGranted"
        const val METHOD_CHANNEL_PERMISSIONS_NOT_GRANTED        = "permissionsNotGranted"
        const val METHOD_CHANNEL_FOREGROUND_SERVICE_STARTED     = "foregroundServiceStarted"
        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED    = "websocketServiceConnected"
        const val METHOD_CHANNEL_SCREEN_LOCK_ENABLED            = "screenLockEnabled"
        const val METHOD_CHANNEL_SCREEN_LOCK_DISABLED           = "screenLockDisabled"



        // Flutter      TO      Android (dev)

//        const val METHOD_CHANNEL_MAIN_ACTIVITY_STARTED          = "mainActivityStarted"
        const val METHOD_CHANNEL_MAIN_ACTIVITY_AUTHENTICATED    = "mainActivityAuthenticated"

        // Android      TO      Flutter (dev)
        const val METHOD_CHANNEL_CONTINUE                       = "continue"
        const val METHOD_CHANNEL_SESSION_KILLED                 = "sessionKilled"
        const val METHOD_CHANNEL_CLOSE_APP                      = "closeApp"
        //endregion


        // Pending operations
        //region
        const val NO_PENDING_OPERATION                          = -1

        // High priority
        const val PENDING_OPERATION_HP_SESSION_KILLED           = 0
        const val PENDING_OPERATION_HP_CLOSE_APP                = 1

        // Low priority
        // TODO:
        // Chat message
        // New delivery
        // etc ...
        // One option: add flag that means Low priority pending message
        // Or other alternative: predefined list
        //endregion

        private var permissionsResolved = false
        private var permissionsGranted = false

        var sharedPreferences: SharedPreferences? = null
        var mainActivity: MainActivityI? = null
        var foregroundServiceIntent: Intent? = null
        var mainActivityClass: Class<*>? = null
        var foregroundServiceClass: Class<*>? = null

        var preInitied = false

        /**
         * Functions
         *
         * - 01 - Pre-init
         * - 02 - Clear
         * - 03 - Send message method channel
         * - 04 - Init base method channel
         * - 05 - Start foreground service
         * - 06 - Stop foreground service
         * - 07 - Debug
         * - 08 - Get debug prefix
         * - 09 - Check preinitied
         * - 10 - Enable screen lock
         * - 11 - Disable screen lock
         */

        /**
         * 01 - Pre-init.
         */
        fun preInit(
                    sharedPreferences: SharedPreferences,
                    mainActivity: MainActivityI,
                    foregroundServiceIntent: Intent,
                    mainActivityClass: Class<*>,
                    foregroundServiceClass: Class<*>,
        ) {
            if (preInitied) {
                return
            }

            this.sharedPreferences = sharedPreferences
            this.mainActivity = mainActivity
            this.foregroundServiceIntent = foregroundServiceIntent
            this.mainActivityClass = mainActivityClass
            this.foregroundServiceClass = foregroundServiceClass


            preInitied = true
        }

        /**
         * 02 - Clear.
         */
        fun clear(deepClear: Boolean) {

            // Deep clear
            if (deepClear) {
                sharedPreferences = null
                mainActivity = null
                foregroundServiceIntent = null
                mainActivityClass = null
                foregroundServiceClass = null

                preInitied = false
            }

            // Soft clear
            PendingOperations.clear()
        }

        /**
         * 03 - Send message method channel.
         */
        fun sendMessageMethodChannel(method: String, args: Any? = null) {
            if (MainActivityState.isUnset()) {
                debug("Cannot send message: $method with arguments: $args because activity is unset")
                return
            }
            GlobalScope.launch {
                val ticket = methodChannelSemaphore.getTicketAndWait()
                (mainActivity as MainActivityI).sendMessageMethodChannel(method, args)
                ticket.release()
            }
        }

        /**
         * 04 - Init base method channel.
         */
        fun initBaseMethodChannel(method: String, arguments: Any? = null): Any? {
            when (method) {

                // Method channel initied
                METHOD_CHANNEL_METHOD_CHANNEL_INITIED -> {
                    (mainActivity as MainActivityI).afterMethodChannelInitied()
                    return true
                }

                // Start Foreground Service
                METHOD_CHANNEL_START_FOREGROUND_SERVICE -> {
                    if (ForegroundServiceBase.isStarted()) {
                        debug("Foreground Service already started")
                        return false
                    }

                    // Arguments
                    if (arguments !is String) {
                        debug("Bad arguments")
                        return false
                    }
                    val args = arguments.toString()
                    val jsonArgs = Gson().fromJson(args, JsonObject::class.java)
                    val acquireLock = jsonArgs.get("acquireLock").asBoolean
                    val authToken = jsonArgs.get("authToken").asString
                    val enableWebsocketSubModule = jsonArgs.get("enableWebsocketSubModule").asBoolean
                    val websocketUrl = jsonArgs.get("websocketUrl").asString
                    val authenticationRequired = jsonArgs.get("authenticationRequired").asBoolean

                    // Modules pre-init (only first time)
                    ForegroundServiceBase.preInit(acquireLock = acquireLock, token = authToken)
                    WebSocketBase.preInit(enableWebsocketSubModule, websocketUrl, authenticationRequired)

                    return startForegroundService()
                }

                // Stop Foreground Service
                METHOD_CHANNEL_STOP_FOREGROUND_SERVICE -> {
                    debug("Trying to stop foreground service")
                    if (ForegroundServiceBase.isStopped()) {
                        debug("Already stopped")
                        return false
                    }
                    debug("Stopping foreground service")
                    val status = stopForegroundService()
                    return status
                }

                // Is Foreground Service started
                METHOD_CHANNEL_IS_FOREGROUND_SERVICE_STARTED -> {
                    return ForegroundServiceBase.isStarted()
                }

                // Get High Priority pending operation
                METHOD_CHANNEL_GET_HP_PENDING_OPERATION -> {
                    return PendingOperations.highPriorityPendingOperation
                }

                // Get Low Priority pending operations
                METHOD_CHANNEL_GET_LP_PENDING_OPERATION -> {

                    // TODO
                    return NO_PENDING_OPERATION
                }

                METHOD_CHANNEL_ENABLE_SCREEN_LOCK -> {
                    enableScreenLock()
                    return true
                }
                METHOD_CHANNEL_DISABLE_SCREEN_LOCK -> {
                    disableScreenLock()
                    return true
                }

                // Others, redirected to auth handler
                else -> return Auth.initCustomMethodChannel(method, arguments)
            }
        }

        /**
         * 05 - Start foreground service.
         */
        fun startForegroundService(): Boolean {
            checkPreinitied()
            if (ForegroundServiceBase.isStarted()) {
                return false
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                (mainActivity as Activity).startForegroundService(foregroundServiceIntent) != null
            } else {
                (mainActivity as Activity).startService(foregroundServiceIntent) != null
            }
        }

        /**
         * 06 - Stop foreground service.
         */
        fun stopForegroundService(): Boolean {
            checkPreinitied()
            if (ForegroundServiceBase.isStopped()) {
                return false
            }
            return (mainActivity as Activity).stopService(foregroundServiceIntent)
        }

        /**
         * 07 - Debug.
         */
        fun debug(msg: String, debugLevel: Int = MODE_DEBUG_INFO) {
            if (!MODE_DEBUG) {
                return
            }
            if (MODE_DEBUG_USED > debugLevel) {
                return
            }
            val prefix = getDebugPrefix(debugLevel)
            val message = "$prefix: $msg"
            println(message)
        }

        /**
         * 08 - Get debug prefix
         */
        private fun getDebugPrefix(debugLevel: Int): String {
            when (debugLevel) {
                MODE_DEBUG_ALL -> return "DEBUG ALL"
                MODE_DEBUG_INFO -> return "DEBUG INFO"
            }
            throw Exception("Invalid debug level")
        }

        /**
         * 09 - Check preinitied.
         */
        fun checkPreinitied() {
            if (!preInitied || !ForegroundServiceBase.preInitied) {
                throw Exception("Commons is not preinitied")
            }
        }

        /**
         * 10 - Enable screen lock.
         */
        fun enableScreenLock() {
            (mainActivity as Activity).window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            sendMessageMethodChannel(METHOD_CHANNEL_SCREEN_LOCK_ENABLED)
        }

        /**
         * 11 - Disable screen lock.
         */
        fun disableScreenLock() {
            (mainActivity as Activity).window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            sendMessageMethodChannel(METHOD_CHANNEL_SCREEN_LOCK_DISABLED)
        }

        /**
         * Permissions functions
         *
         * - 01 - Are permissions granted
         * - 02 - Permissions granted
         * - 03 - Advice permissions resolution
         * - 04 - Advice permissions granted
         * - 05 - Advice permissions not granted
         */

        fun arePermissionsGranted(): Boolean {
            return permissionsGranted
        }

        /**
         * 02 - Permissions granted.
         */
        fun permissionsGranted(granted: Boolean) {
            debug("permissionsGranted(): $granted", MODE_DEBUG_ALL)
            permissionsGranted = granted
            permissionsResolved = true
            advicePermissionsResolution()
        }

        /**
         * 03 - Advice permissions resolution.
         */
        fun advicePermissionsResolution() {
            if (!permissionsResolved) {
                throw java.lang.Exception("Permissions not resolved yet")
            }
            debug("advicePermissionsResolution()", MODE_DEBUG_ALL)
            if (permissionsGranted) {
                return advicePermissionsGranted()
            }
            advicePermissionsNotGranted()
        }

        /**
         * 04 - After permissions granted.
         */
        private fun advicePermissionsGranted() {
            debug("advicePermissionsGranted()", MODE_DEBUG_ALL)
            sendMessageMethodChannel(METHOD_CHANNEL_PERMISSIONS_GRANTED)
        }

        /**
         * 05 - After permissions not granted.
         */
        private fun advicePermissionsNotGranted() {
            debug("advicePermissionsNotGranted()", MODE_DEBUG_ALL)
            sendMessageMethodChannel(METHOD_CHANNEL_PERMISSIONS_NOT_GRANTED)
        }
    }
}

/**
 * Debig Manager
 */
class DebugM {

    class Message(private val prefix: String?, private val message: String) {
        private val date = Date()

        fun getWsText(): String {
            return  "[DebugM] - [${Utils.getHourFormatted(date)}] --> ${if (prefix != null) "$prefix: " else ""} $message"
        }

        /**
         * Prints message via Log
         */
        fun logInfo() {
            Log.i(prefix, message)
        }

        /**
         * Send message via WebSocket
         */
        fun sendOverWebsocket(): Boolean {
            return CommonsModules.websocket != null && CommonsModules.websocket!!.emit(WS_SIGNAL_DEBUG_M_MESSAGE, getWsText())
        }
    }

    companion object {

        private var list = mutableListOf<Message>()

        /**
         * Return list and clear it
         */
        fun getStoredMessages(): List<Message>? {
            val retList = list
            list = mutableListOf()
            return retList
        }

        /**
         * Send debug message via Log and via WebSocket.
         */
        fun send(prefix: String? = null, message: String) {
            val messageObj = Message(prefix, message)

            // Send via Log
            messageObj.logInfo()

            // Send via WebSocket. If not possible the store it on list
            if (!messageObj.sendOverWebsocket()) {
                list.add(messageObj)
            }
        }

        /**
         * Tries to send via WebSocket and remove from list every stored message.
         */
        fun resolveStoredMessages() {
            val clone = mutableListOf<Message>()
            list.forEach {
                clone.add(it)
            }

            clone.forEach {
                if (it.sendOverWebsocket()) {
                    list.remove(it)
                }
            }
        }
    }
}

class PendingOperations {

    companion object {

        var highPriorityPendingOperation = Commons.NO_PENDING_OPERATION


        fun clear() {
            highPriorityPendingOperation = Commons.NO_PENDING_OPERATION
        }

        fun setHighPriorityPendingOperationn(opCode: Int) {
            highPriorityPendingOperation = opCode
        }
    }
}

class MainActivityState {

    companion object {
        const val STATE_UNSET = 1
        const val STATE_STARTED = 2
        const val STATE_PAUSED = 3

        private var state = STATE_UNSET


        /**
         * Functions
         *
         * - 01 - Set state
         * - 02 - Is unset
         * - 03 - Is started
         * - 04 - Is paused
         */

        /**
         * 01 - Set state.
         */
        fun setState(state: Int) {
            this.state = state
        }

        /**
         * 02 - Is unset.
         */
        fun isUnset(): Boolean {
            return state == STATE_UNSET
        }

        /**
         * 03 - Is started.
         */
        fun isStarted(): Boolean {
            return state == STATE_STARTED
        }

        /**
         * 04 - Is paused.
         */
        fun isPaused(): Boolean {
            return state == STATE_PAUSED
        }
    }
}