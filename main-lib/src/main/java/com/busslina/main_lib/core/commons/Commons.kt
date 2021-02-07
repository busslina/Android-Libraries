package com.busslina.main_lib.core.commons

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.busslina.main_lib.core.interfaces.MainActivityI
import com.busslina.main_lib.core.modules.ForegroundServiceBase

class Commons {

    companion object {

        const val MODE_DEBUG = true


        // Flutter      TO      Android
        // Android      TO      Flutter
        // Flutter      TO      Android (dev)
        // Android      TO      Flutter (dev)


        // Method channel
        const val DEFAULT_METHOD_CHANNEL_NAME                   = "myFlutterApp"
        // Flutter      TO      Android
        const val METHOD_CHANNEL_START_FOREGROUND_SERVICE       = "startForegoundService"
        const val METHOD_CHANNEL_STOP_FOREGROUND_SERVICE        = "stopForegoundService"

        // Android      TO      Flutter
        const val METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED    = "websocketServiceConnected"




        // Flutter      TO      Android (dev)
        const val METHOD_CHANNEL_IS_FOREGROUND_SERVICE_STARTED  = "isForegroundServiceStarted"
        const val METHOD_CHANNEL_GET_HP_PENDING_OPERATION       = "getHighPriorityPendingOperation";
        const val METHOD_CHANNEL_MAIN_ACTIVITY_STARTED          = "mainActivityStarted"
        const val METHOD_CHANNEL_MAIN_ACTIVITY_AUTHENTICATED    = "mainActivityAuthenticated"

        // Android      TO      Flutter (dev)
        const val METHOD_CHANNEL_CONTINUE                       = "continue"
        const val METHOD_CHANNEL_SESSION_KILLED                 = "sessionKilled"
        const val METHOD_CHANNEL_CLOSE_APP                      = "closeApp"



        // Pending operations
        const val NO_PENDING_OPERATION                          = -1

        // High priority
        const val PENDING_OPERATION_HP_SESSION_KILLED           = 0
        const val PENDING_OPERATION_HP_CLOSE_APP                = 1

        // Low priority


        var token: String? = null
        var mainActivity: MainActivityI? = null

        var preInitied = false

        /**
         * Functions
         *
         * - 01 - Pre-init
         * - 02 - Clear
         * - 03 - Send message method channel
         * - 04 - Debug
         */

        /**
         * 01 - Pre-init.
         */
        fun preInit(mainActivity: MainActivityI, token: String) {
            if (preInitied) {
                return
            }

            this.mainActivity = mainActivity
            this.token = token

            preInitied = true
        }

        /**
         * 02 - Clear.
         */
        fun clear() {
            token = null
            mainActivity = null
            PendingOperations.clear()
        }

        /**
         * 03 - Send message method channel.
         */
        fun sendMessageMethodChannel(method: String, args: Any?) {

            // TODO: MethodChannelQueue

            Handler(Looper.getMainLooper()).post {
                mainActivity!!.sendMessageMethodChannel(method, args)
            }
        }

        /**
         * 04 - Debug.
         */
        fun debug(msg: String) {
            if (!MODE_DEBUG) {
                return
            }
            println(msg)
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