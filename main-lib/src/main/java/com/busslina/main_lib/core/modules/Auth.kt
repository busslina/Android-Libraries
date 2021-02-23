package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.commons.Commons
import com.google.gson.Gson
import com.google.gson.JsonObject

class Auth {

    companion object {

        // Method channel

        // Flutter      TO      Android
        const val METHOD_CHANNEL_AUTH_CLEAR_DATA                    = "clearData"
        const val METHOD_CHANNEL_AUTH_SET_TOKEN                     = "setToken"                            // Token
        const val METHOD_CHANNEL_AUTH_GET_TOKEN                     = "getToken"
        const val METHOD_CHANNEL_AUTH_SET_LOCALLY_LOGGED            = "setLocallyLogged"                    // Locally logged
        const val METHOD_CHANNEL_AUTH_GET_LOCALLY_LOGGED            = "getLocallyLogged"
        const val METHOD_CHANNEL_AUTH_SET_REMOTELY_LOGGED           = "setRemotelyLogged"                   // Remotely logged
        const val METHOD_CHANNEL_AUTH_GET_REMOTELY_LOGGED           = "getRemotelyLogged"
        const val METHOD_CHANNEL_AUTH_SET_CHECKED_LOGGED            = "setCheckedLogged"                    // Checked logged
        const val METHOD_CHANNEL_AUTH_GET_CHECKED_LOGGED            = "getCheckedLogged"
        const val METHOD_CHANNEL_AUTH_SET_USER_ID                   = "setUserId"                           // UserId
        const val METHOD_CHANNEL_AUTH_GET_USER_ID                   = "getUserId"
        const val METHOD_CHANNEL_AUTH_SET_IS_ROOT                   = "setIsRoot"                           // Is root
        const val METHOD_CHANNEL_AUTH_GET_IS_ROOT                   = "getIsRoot"
        const val METHOD_CHANNEL_AUTH_SET_IS_ADMIN                  = "setIsAdmin"                          // Is admin
        const val METHOD_CHANNEL_AUTH_GET_IS_ADMIN                  = "getIsAdmin"
        const val METHOD_CHANNEL_AUTH_SET_IS_CLIENT                 = "setIsClient"                         // Is client
        const val METHOD_CHANNEL_AUTH_GET_IS_CLIENT                 = "getIsClient"
        const val METHOD_CHANNEL_AUTH_SET_IS_RIDER                  = "setIsRider"                          // Is rider
        const val METHOD_CHANNEL_AUTH_GET_IS_RIDER                  = "getIsRider"
        const val METHOD_CHANNEL_AUTH_SET_IS_PARTNER                = "setIsPartner"                        // Is partner
        const val METHOD_CHANNEL_AUTH_GET_IS_PARTNER                = "getIsPartner"

        // Android      TO      Flutter


        // General data
        var token: String? = null
        var checkedLogged = false
        var locallyLogged = false
        var remotellyLogged = false

        // User info data
        var userId: String? = null
        var isRoot: Boolean? = null
        var isAdmin: Boolean? = null
        var isClient: Boolean? = null
        var isRider: Boolean? = null
        var isPartner: Boolean? = null


        fun initCustomMethodChannel(method: String, arguments: Any? = null): Any? {

            when (method) {
                METHOD_CHANNEL_AUTH_CLEAR_DATA -> return clearData()
                METHOD_CHANNEL_AUTH_SET_TOKEN -> {
                    // Arguments
                    if (arguments !is String) {
                        throw Exception("Bad arguments")
                    }
                    token = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_TOKEN -> {
                    return token
                }
            }


            throw Exception("Not valid method channel message: $method")
        }

        private fun clearData() {
            checkedLogged = false
            locallyLogged = false
            remotellyLogged = false

            userId = null
            isRoot = null
            isAdmin = null
            isClient = null
            isRider = null
            isPartner = null
        }

    }
}