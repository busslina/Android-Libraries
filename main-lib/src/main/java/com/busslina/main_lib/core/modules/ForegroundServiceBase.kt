package com.busslina.main_lib.core.modules

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.CommonsModules

abstract class ForegroundServiceBase: Service {

    companion object {

        const val NOTIFICATION_ID = 101
        const val STATE_STOPPED = 0
        const val STATE_STARTED = 1

        fun isStopped(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStopped()
        }

        fun isStarted(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStarted()
        }

        fun sessionKilled() {
            TODO("ADVICE FLUTTER THAT SESSION WAS KILLED")


//            Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_WEBSOCKET_SERVICE_CONNECTED, null)


            Commons.mainActivity.stopForegroundService()
        }
    }

    var state = STATE_STOPPED

    /**
     * Constructor.
     */
    constructor(): super() {
        CommonsModules.foregroundService = this
    }

    /**
     * Functions
     *
     * - 01 - Is stopped
     * - 02 - Is started
     */

    //region
    /**
     * 01 - Is stopped.
     */
    fun isStopped(): Boolean {
        return state == STATE_STOPPED
    }

    /**
     * 02 - Is started.
     */
    fun isStarted(): Boolean {
        return state == STATE_STARTED
    }
    //endregion

    /**
     * Inherited functions
     *
     * - 01 - On create
     * - 02 - On bind
     * - 03 - On start command
     * - 04 - On destroy
     */

    override fun onCreate() {
        state = STATE_STARTED
        super.onCreate()

        // To foreground with notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notification = getNotification("Aplicación iniciada")
            startForeground(NOTIFICATION_ID, notification)
        }

        // Websocket init
        if (WebSocketBase.enableWebsocketSubModule) {
            createWebsocketSubModule()
            CommonsModules.websocket!!.start()
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        // Websocket stop
        if (WebSocketBase.enableWebsocketSubModule) {
            CommonsModules.websocket!!.stop()
        }

        state = STATE_STOPPED
        super.onDestroy()
    }

    /**
     * Abstract functions
     *
     * - 01 - Get notification
     * - 02- Create websocket sub-module
     */

    /**
     * 01 - Get notification.
     */
    abstract fun getNotification(text: String = "This is running in background"): Notification

    /**
     * 02- Create websocket sub-module.
     */
    abstract fun createWebsocketSubModule()



}