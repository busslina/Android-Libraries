package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.CommonsModules

class EventsHandler {

    companion object {

        /**
         * Functions
         *
         * - 01 - Foreground Service initied
         * - 02 - Foreground Service closed
         */

        /**
         * 01 - Foreground Service initied.
         */
        fun foregroundServiceInitied() {
            // Advice Flutter part
            Commons.sendMessageMethodChannel(Commons.METHOD_CHANNEL_FOREGROUND_SERVICE_STARTED)

        }

        /**
         * 02 - Foreground Service closed.
         */
        fun foregroundServiceClosed() {
            // Websocket stop
            if (WebSocketBase.enableWebsocketSubModule) {
                CommonsModules.websocket!!.stop()
            }

            // Clear
            ForegroundServiceBase.clear()
            Commons.clear(deepClear = false)
            CommonsModules.clear()
        }
    }
}