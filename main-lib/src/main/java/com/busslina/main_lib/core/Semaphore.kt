package com.busslina.main_lib.core

import kotlinx.coroutines.delay
import java.lang.Exception

class Semaphore {

    var enabled = true
    var value = 0
    var ticketCount = 0
    var ticketList: ArrayList<SemaphoreTicket> = ArrayList()

    /**
     * Functions
     *
     * - 01 - Get ticket
     * - 02 - Get ticket and wait
     * - 03 - Disable
     * - 04 - Enable
     */

    /**
     * 01 - Get ticket.
     */
    fun getTicket(): SemaphoreTicket {
        // TODO: check int overflow
        val index = ticketCount++
        val ticket = SemaphoreTicket(index, this)
        ticketList.add(ticket)
        return ticket
    }

    /**
     * 02 - Get ticket and wait.
     */
    suspend fun getTicketAndWait(): SemaphoreTicket {
        val ticket = getTicket()
        while (!ticket.isMyTurn()) {
            delay(10)
        }
        return ticket
    }

    /**
     * 03 - Disable.
     */
    fun disable() {
        enabled = false
    }

    /**
     * 04 - Enable.
     */
    fun enable() {
        enabled = true
    }
}

class SemaphoreTicket(var index: Int, var semaphore: Semaphore) {

    /**
     * Functions
     *
     * - 01 - Is my turn
     * - 02 - Release
     */

    /**
     * 01 - Is my turn.
     */
    fun isMyTurn(): Boolean {
        if (!semaphore.enabled) {
            return false
        }
        return index == semaphore.value
    }

    /**
     * 02 - Release.
     */
    fun release() {
        if (index != semaphore.value) {
            throw Exception("Unexpected error on Semaphore.release()")
        }
        semaphore.value++
    }

}