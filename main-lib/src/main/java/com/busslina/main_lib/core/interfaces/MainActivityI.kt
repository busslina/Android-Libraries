package com.busslina.main_lib.core.interfaces

import android.content.Intent

interface MainActivityI {

    var foregroundServiceIntent: Intent
    var lastIntentHash: Int?

    /**
     * Abstract functions
     *
     * - 01 - Request permissions
     * - 02 - Init method channel
     * - 03 - Send message method channel
     * - 04 - After method channel initied
     */


    /**
     * 01 - Request permissions.
     */
    fun requestPermissions()

    /**
     * 02 - Init method channel.
     */
    fun initMethodChannel()

    /**
     * 03 - Send message method channel.
     */
    fun sendMessageMethodChannel(method: String, args: Any?)

    /**
     * 04 - After method channel initied.
     */
    fun afterMethodChannelInitied()

}