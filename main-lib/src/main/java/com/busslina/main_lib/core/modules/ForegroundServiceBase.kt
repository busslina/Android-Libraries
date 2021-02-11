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
import java.lang.Exception

abstract class ForegroundServiceBase: Service {

    companion object {

        const val NOTIFICATION_ID = 101
        const val STATE_STOPPED = 0
        const val STATE_STARTED = 1

        var token: String? = null
        var acquireLock = false

        var preInitied = false


        fun isStopped(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStopped()
        }

        fun isStarted(): Boolean {
            return CommonsModules.foregroundService != null && CommonsModules.foregroundService!!.isStarted()
        }

        fun preInit(token: String?, acquireLock: Boolean) {
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
     * - 03 - Acquire lock
     * - 04 - Release lock
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

    fun acquireLock() {
        if (lockAcquired) {
            return
        }
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ForegroudService::lock").apply {
                acquire()
                lockAcquired = true
            }
        }
    }

    fun releaseLock() {
        if (!lockAcquired) {
            return
        }
        if (wakeLock != null && wakeLock!!.isHeld) {
            wakeLock!!.release()
        }
        lockAcquired = false
    }
    //endregion

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
            CommonsModules.websocket!!.start()
        }

        // Advice Flutter part
        Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_FOREGROUND_SERVICE_STARTED)
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

        // Acquire lock
        if (acquireLock) {
            acquireLock()
        }

        return START_STICKY
    }

    /**
     * 04 - On destroy.
     */
    override fun onDestroy() {

        // Websocket stop
        if (WebSocketBase.enableWebsocketSubModule) {
            CommonsModules.websocket!!.stop()
        }

        // Clear
        clear()
        Commons.clear()
        CommonsModules.clear()

        // Release lock
        releaseLock()

        state = STATE_STOPPED
        super.onDestroy()
    }

    /**
     * 05 - On task removed.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)

        val restartServiceIntent = Intent(applicationContext, Commons.foregroundServiceClass).also {
            it.setPackage(packageName)
        }

        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)

        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmService.set(AlarmManager.ELAPSED_REALTIME, elapsedRealtime() + 1000, restartServicePendingIntent)

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
    abstract fun getNotification(text: String = "This is running in background"): Notification

    /**
     * 02 - Update notification.
     */
    abstract fun updateNotification(text: String)

    /**
     * 03- Create websocket sub-module.
     */
    abstract fun createWebsocketSubModule()



}