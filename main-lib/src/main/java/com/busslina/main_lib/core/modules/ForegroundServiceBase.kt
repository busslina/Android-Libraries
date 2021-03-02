package com.busslina.main_lib.core.modules

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.os.SystemClock.elapsedRealtime
import androidx.core.app.NotificationCompat
import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.Commons.Companion.debug
import com.busslina.main_lib.core.commons.CommonsModules
import com.busslina.main_lib.core.commons.PendingOperations
import com.busslina.main_lib.core.modules.NotificationsBase.Companion.NOTIFICATION_ID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class ForegroundServiceBase
/**
 * Constructor.
 */() : Service() {

    companion object {


        const val STATE_STOPPED = 0
        const val STATE_STARTED = 1

        var notificationText: String? = null

        var token: String? = null
        var acquireLock = false

        var preInitied = false


        fun isStopped(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStopped()
        }

        fun isStarted(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStarted()
        }

        fun preInit(token: String? = null, acquireLock: Boolean = false) {
            if (preInitied) {
                return
            }

            this.token = token
            this.acquireLock = acquireLock

            preInitied = true
        }

        fun clear() {
            token = null
            acquireLock = false

            preInitied = false
        }

        fun sessionKilled() {

            // TODO:

            TODO()

//            PendingOperations.setHighPriorityPendingOperationn(Commons.PENDING_OPERATION_HP_SESSION_KILLED)
//            Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_SESSION_KILLED, null)


//            CommonsModules.foregroundService!!.stopSelf()
        }
    }

    private var state = STATE_STOPPED

    private var wakeLock: PowerManager.WakeLock? = null
    private var lockAcquired = false

    // TESTING
    var onStartCommandCount = 0

    /**
     * Constructor
     */
    init {
        CommonsModules.foregroundService = this
    }

    /**
     * Functions
     *
     * - 01 - Is stopped
     * - 02 - Is started
     * - 03 - Acquire lock
     * - 04 - Release lock
     */

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

    /**
     * 03 - Acquire lock.
     */
    fun acquireLock() {
        if (lockAcquired) {
            return
        }
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ForegroudService::lock").apply {
                CommonsModules.websocket!!.emit("message", "[INFO]: Acquiring lock")
                acquire()
                lockAcquired = true
            }
        }
    }

    /**
     * 04 - Release lock.
     */
    fun releaseLock() {
        if (!lockAcquired) {
            return
        }
        if (wakeLock != null && wakeLock!!.isHeld) {
            CommonsModules.websocket!!.emit("message", "[INFO]: Releasing lock")
            wakeLock!!.release()
        }
        lockAcquired = false
    }

    fun checkWebsocketSubModule() {
        if (CommonsModules.websocket == null) {
            throw Exception("WebSocket module not created")
        }
    }

    /**
     * Inherited functions
     *
     * - 01 - On create
     * - 02 - On bind
     * - 03 - On start command
     * - 04 - On destroy
     * - 05 - On task removed
     */

    /**
     * 01 - On create.
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
            debug("Websocket init")
            createWebsocketSubModule()
            checkWebsocketSubModule()
            CommonsModules.websocket!!.start()
        }

        // Event handler
        EventsHandler.foregroundServiceInitied()

        // Acquire lock
        if (acquireLock) {
            acquireLock()
        }
    }

    /**
     * 02 - On bind.
     */
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    /**
     * 03 - On start command.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        onStartCommandCount++

        CommonsModules.websocket!!.emit("message", "[INFO]: Foreground Service -- onStartCommand() -- count: $onStartCommandCount")

//        return START_STICKY
        return START_NOT_STICKY
    }

    /**
     * 04 - On destroy.
     */
    override fun onDestroy() {

        val semaphore = CommonsModules.websocket!!.semaphore

        CommonsModules.websocket!!.emit("message", "[INFO]: Foreground Service -- onDestroy()")

        // Event handler (Websocket semaphore coroutine)
        EventsHandler.foregroundServiceClosed()

        // (Websocket semaphore coroutine)
        GlobalScope.launch {
            val ticket = semaphore.getTicketAndWait()
            // Release lock
            releaseLock()

            state = STATE_STOPPED
            super.onDestroy()
            ticket.release()
        }
    }

    /**
     * 05 - On task removed.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {

        CommonsModules.websocket!!.emit("message", "[INFO]: Foreground Service -- onTaskRemoved()")

//        super.onTaskRemoved(rootIntent)

        debug("onTaskRemoved()")

//        val restartServiceIntent = Intent(applicationContext, Commons.foregroundServiceClass).also {
//            it.setPackage(packageName)
//        }
//
//        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
//
//        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        alarmService.set(AlarmManager.ELAPSED_REALTIME, elapsedRealtime() + 1000, restartServicePendingIntent)

    }

    /**
     * Abstract functions
     *
     * - 01 - Get notification
     * - 02 - Update notification
     * - 03 - Create websocket sub-module
     */

    /**
     * 01 - Get notification.
     */
    abstract fun getNotification(text: String? = notificationText, actionName: String? = null, actionValue: String? = null): Notification

    /**
     * 02 - Update notification.
     */
    abstract fun updateNotification(text: String? = notificationText, actionName: String? = null, actionValue: String? = null)

    /**
     * 03- Create websocket sub-module.
     */
    abstract fun createWebsocketSubModule()
}