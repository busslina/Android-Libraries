package com.busslina.main_lib.core.modules

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.busslina.main_lib.core.commons.Commons.Companion.debug
import com.busslina.main_lib.core.commons.CommonsModules
import com.busslina.main_lib.core.commons.DebugM
import com.busslina.main_lib.core.modules.NotificationsBase.Companion.NOTIFICATION_ID
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

abstract class ForegroundServiceBase: Service() {

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
        DebugM.send("ForegroundServiceBase", "constructor()", false)
    }

    /**
     * Functions
     *
     * - 01 - Is stopped
     * - 02 - Is started
     * - 03 - Acquire lock
     * - 04 - Release lock
     * - 05 - Check websocket submodule
     * - 06 - Initialize service
     */

    /**
     * 01 - Is stopped.
     */
    fun isStopped(): Boolean {
        DebugM.send("ForegroundServiceBase", "isStopped() -- > " + (state == STATE_STOPPED))
        return state == STATE_STOPPED
    }

    /**
     * 02 - Is started.
     */
    fun isStarted(): Boolean {
        DebugM.send("ForegroundServiceBase", "isStarted() -- > " + (state == STATE_STARTED))
        return state == STATE_STARTED
    }

    /**
     * 03 - Acquire lock.
     */
    fun acquireLock() {
        if (lockAcquired) {
            return
        }
        DebugM.send("ForegroundServiceBase", "acquireLock()",)
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
        DebugM.send("ForegroundServiceBase", "releaseLock()")
        if (wakeLock != null && wakeLock!!.isHeld) {
            CommonsModules.websocket!!.emit("message", "[INFO]: Releasing lock")
            wakeLock!!.release()
        }
        lockAcquired = false
    }

    /**
     * 05 - Check websocket submodule.
     */
    fun checkWebsocketSubModule() {
        if (CommonsModules.websocket == null) {
            throw Exception("WebSocket module not created")
        }
    }

    /**
     * 06 - Initialize service.
     */
    open fun initializeService(recoveryMode: Boolean) {
        state = STATE_STARTED

        // TESTING
        if (recoveryMode) {
            // Print variables state
            DebugM.send("ForegroundServiceBase", "WebSocket module is null? :${CommonsModules.websocket == null}")
            DebugM.send("ForegroundServiceBase", "WebSocket module is started? :${CommonsModules.websocket != null && CommonsModules.websocket!!.isStarted()}")
        }

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
        super.onCreate()
        CommonsModules.appContext = applicationContext
        DebugM.send("ForegroundServiceBase", "onCreate()")
        initializeService(false)
    }

    /**
     * 02 - On bind.
     */
    override fun onBind(p0: Intent?): IBinder? {
        DebugM.send("ForegroundServiceBase", "onBind()")
        return null
    }

    /**
     * 03 - On start command.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        DebugM.send("ForegroundServiceBase", "onStartCommand()")

        if (intent == null) {
            // Restarting service after being deleted by system
            DebugM.send("ForegroundServiceBase", "onStartCommand() --> intent = null")
            initializeService(true)
        } else {
            DebugM.send("ForegroundServiceBase", "onStartCommand() --> intent = $intent")
            DebugM.send("ForegroundServiceBase", "onStartCommand() --> action = ${intent.action}")
        }
        DebugM.send("ForegroundServiceBase", "onStartCommand() --> isStarted: ${isStarted()}")

        return START_STICKY
//        return START_NOT_STICKY
    }

    /**
     * 04 - On destroy.
     */
    override fun onDestroy() {
        DebugM.send("ForegroundServiceBase", "onDestroy()")

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
        DebugM.send("ForegroundServiceBase", "onTaskRemoved()")
        super.onTaskRemoved(rootIntent)
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