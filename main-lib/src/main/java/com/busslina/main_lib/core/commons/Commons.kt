package com.busslina.main_lib.core.commons

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.busslina.main_lib.core.interfaces.MainActivityI
import com.busslina.main_lib.core.modules.ForegroundServiceBase

class Commons {

    companion object {

        const val MODE_DEBUG = true

        // Method channel
        //region
        const val DEFAULT_METHOD_CHANNEL_NAME                   = "myFlutterApp"
        // Flutter      TO      Android
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
         * - 04 - Start foreground service
         * - 05 - Stop foreground service
         * - 06 - Debug
         * - 07 - Check preinitied
         */

        //region
        /**
         * 01 - Pre-init.
         */
        fun preInit(mainActivity: MainActivityI,
                    foregroundServiceIntent: Intent,
                    mainActivityClass: Class<*>,
                    foregroundServiceClass: Class<*>,
        ) {
            if (preInitied) {
                return
            }

            this.mainActivity = mainActivity
            this.foregroundServiceIntent = foregroundServiceIntent
            this.mainActivityClass = mainActivityClass
            this.foregroundServiceClass = foregroundServiceClass


            preInitied = true
        }

        /**
         * 02 - Clear.
         */
        fun clear() {
            mainActivity = null
            PendingOperations.clear()
            preInitied = false
        }

        /**
         * 03 - Send message method channel.
         */
        fun sendMessageMethodChannel(method: String, args: Any? = null) {

            // TODO: MethodChannelQueue

//            Handler(Looper.getMainLooper()).post {
//                mainActivity!!.sendMessageMethodChannel(method, args)
//            }
            mainActivity!!.sendMessageMethodChannel(method, args)
        }

        /**
         * 04 - Start foreground service.
         */
        fun startForegroundService(): Boolean {
            checkPreinitied()
            if (ForegroundServiceBase.isStarted()) {
                return false
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return (mainActivity!! as Activity).startForegroundService(foregroundServiceIntent) != null
            } else {
                return (mainActivity!! as Activity).startService(foregroundServiceIntent) != null
            }
        }

        /**
         * 05 - Stop foreground service.
         */
        fun stopForegroundService(): Boolean {
            checkPreinitied()
            if (ForegroundServiceBase.isStopped()) {
                return false
            }
            return (mainActivity!! as Activity).stopService(foregroundServiceIntent)
        }

        /**
         * 06 - Debug.
         */
        fun debug(msg: String) {
            if (!MODE_DEBUG) {
                return
            }
            println("DEBUG: $msg")
        }

        /**
         * 07 - Check preinitied.
         */
        fun checkPreinitied() {
            if (!preInitied || !ForegroundServiceBase.preInitied) {
                throw Exception("Commons is not preinitied")
            }
        }
        //endregion

        /**
         * Permissions functions
         *
         * - 01 - Are permissions granted
         * - 02 - Permissions granted
         * - 03 - Advice permissions resolution
         * - 04 - Advice permissions granted
         * - 05 - Advice permissions not granted
         */

        //region
        fun arePermissionsGranted(): Boolean {
            return permissionsGranted
        }

        /**
         * 02 - Permissions granted.
         */
        fun permissionsGranted(granted: Boolean) {
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
            if (permissionsGranted) {
                return advicePermissionsGranted()
            }
            advicePermissionsNotGranted()
        }

        /**
         * 04 - After permissions granted.
         */
        private fun advicePermissionsGranted() {
            debug("Sending permissions granted")
            sendMessageMethodChannel(METHOD_CHANNEL_PERMISSIONS_GRANTED)
        }

        /**
         * 05 - After permissions not granted.
         */
        private fun advicePermissionsNotGranted() {
            debug("Sending permissions not granted")
            sendMessageMethodChannel(METHOD_CHANNEL_PERMISSIONS_NOT_GRANTED)
        }
        //endregion
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