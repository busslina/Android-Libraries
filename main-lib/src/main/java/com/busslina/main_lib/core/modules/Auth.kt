package com.busslina.main_lib.core.modules

import com.busslina.main_lib.core.commons.Commons
import com.busslina.main_lib.core.commons.Commons.Companion.debug
import com.google.gson.Gson
import com.google.gson.JsonObject

class Auth {

    companion object {

        private const val TOKEN_KEY = "auth-token"

        // Method channel

        // Flutter      TO      Android
        private const val METHOD_CHANNEL_AUTH_CLEAR_DATA                    = "clearData"
        private const val METHOD_CHANNEL_AUTH_SET_TOKEN                     = "setToken"                            // Token
        private const val METHOD_CHANNEL_AUTH_GET_TOKEN                     = "getToken"
        private const val METHOD_CHANNEL_AUTH_SET_LOCALLY_LOGGED            = "setLocallyLogged"                    // Locally logged
        private const val METHOD_CHANNEL_AUTH_GET_LOCALLY_LOGGED            = "getLocallyLogged"
        private const val METHOD_CHANNEL_AUTH_SET_REMOTELY_LOGGED           = "setRemotelyLogged"                   // Remotely logged
        private const val METHOD_CHANNEL_AUTH_GET_REMOTELY_LOGGED           = "getRemotelyLogged"
        private const val METHOD_CHANNEL_AUTH_SET_CHECKED_LOGGED            = "setCheckedLogged"                    // Checked logged
        private const val METHOD_CHANNEL_AUTH_GET_CHECKED_LOGGED            = "getCheckedLogged"
        private const val METHOD_CHANNEL_AUTH_SET_USER_ID                   = "setUserId"                           // UserId
        private const val METHOD_CHANNEL_AUTH_GET_USER_ID                   = "getUserId"
        private const val METHOD_CHANNEL_AUTH_SET_IS_ROOT                   = "setIsRoot"                           // Is root
        private const val METHOD_CHANNEL_AUTH_GET_IS_ROOT                   = "getIsRoot"
        private const val METHOD_CHANNEL_AUTH_SET_IS_ADMIN                  = "setIsAdmin"                          // Is admin
        private const val METHOD_CHANNEL_AUTH_GET_IS_ADMIN                  = "getIsAdmin"
        private const val METHOD_CHANNEL_AUTH_SET_IS_CLIENT                 = "setIsClient"                         // Is client
        private const val METHOD_CHANNEL_AUTH_GET_IS_CLIENT                 = "getIsClient"
        private const val METHOD_CHANNEL_AUTH_SET_IS_RIDER                  = "setIsRider"                          // Is rider
        private const val METHOD_CHANNEL_AUTH_GET_IS_RIDER                  = "getIsRider"
        private const val METHOD_CHANNEL_AUTH_SET_IS_PARTNER                = "setIsPartner"                        // Is partner
        private const val METHOD_CHANNEL_AUTH_GET_IS_PARTNER                = "getIsPartner"

        // Android      TO      Flutter


        // General data
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

        /**
         * Functions
         *
         * - 01 - Init custom method channel
         * - 02 - Clear data
         * - 03 - Set token
         * - 04 - Get token
         * - 05 - Clear token
         */

        /**
         * 01 - Init custom method channel.
         */
        fun initCustomMethodChannel(method: String, arguments: Any? = null): Any? {

            when (method) {
                METHOD_CHANNEL_AUTH_CLEAR_DATA -> return clearData()
                METHOD_CHANNEL_AUTH_SET_TOKEN -> return setToken(arguments)
                METHOD_CHANNEL_AUTH_GET_TOKEN -> return getToken()
                METHOD_CHANNEL_AUTH_SET_LOCALLY_LOGGED -> {
                    debug(METHOD_CHANNEL_AUTH_SET_LOCALLY_LOGGED)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    locallyLogged = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_LOCALLY_LOGGED -> return locallyLogged
                METHOD_CHANNEL_AUTH_SET_REMOTELY_LOGGED -> {
                    debug(METHOD_CHANNEL_AUTH_SET_REMOTELY_LOGGED)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    remotellyLogged = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_REMOTELY_LOGGED -> return remotellyLogged
                METHOD_CHANNEL_AUTH_SET_CHECKED_LOGGED -> {
                    debug(METHOD_CHANNEL_AUTH_SET_CHECKED_LOGGED)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    checkedLogged = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_CHECKED_LOGGED -> return checkedLogged
                METHOD_CHANNEL_AUTH_SET_USER_ID -> {
                    debug(METHOD_CHANNEL_AUTH_SET_USER_ID)
                    if (arguments == null || arguments !is String) {
                        throw Exception("Bad arguments")
                    }
                    userId = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_USER_ID -> return userId
                METHOD_CHANNEL_AUTH_SET_IS_ROOT -> {
                    debug(METHOD_CHANNEL_AUTH_SET_IS_ROOT)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    isRoot = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_IS_ROOT -> return isRoot
                METHOD_CHANNEL_AUTH_SET_IS_ADMIN -> {
                    debug(METHOD_CHANNEL_AUTH_SET_IS_ADMIN)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    isAdmin = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_IS_ADMIN -> return isAdmin
                METHOD_CHANNEL_AUTH_SET_IS_CLIENT -> {
                    debug(METHOD_CHANNEL_AUTH_SET_IS_CLIENT)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    isClient = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_IS_CLIENT -> return isClient
                METHOD_CHANNEL_AUTH_SET_IS_RIDER -> {
                    debug(METHOD_CHANNEL_AUTH_SET_IS_RIDER)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    isRider = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_IS_RIDER -> return isRider
                METHOD_CHANNEL_AUTH_SET_IS_PARTNER -> {
                    debug(METHOD_CHANNEL_AUTH_SET_IS_PARTNER)
                    if (arguments == null || arguments !is Boolean) {
                        throw Exception("Bad arguments")
                    }
                    isPartner = arguments
                    return true
                }
                METHOD_CHANNEL_AUTH_GET_IS_PARTNER -> return isPartner
            }
            throw Exception("Not valid method channel message: $method")
        }

        /**
         * 02 - Clear data.
         */
        private fun clearData(): Boolean {
            clearToken()
            checkedLogged = false
            locallyLogged = false
            remotellyLogged = false

            userId = null
            isRoot = null
            isAdmin = null
            isClient = null
            isRider = null
            isPartner = null

            return true
        }

        /**
         * 03 - Set token.
         */
        private fun setToken(arguments: Any?): Boolean {
            debug(METHOD_CHANNEL_AUTH_SET_TOKEN)
            if (arguments == null || arguments !is String) {
                throw Exception("Bad arguments")
            }

            // Save token on Shared preferences
            Commons.sharedPreferences!!.edit().putString(TOKEN_KEY, arguments).apply()

            return true
        }

        /**
         * 04 - Get token.
         */
        private fun getToken(): String? {
            return Commons.sharedPreferences!!.getString(TOKEN_KEY, null)
        }

        /**
         * 05 - Clear token.
         */
        private fun clearToken() {
            // Clear token on Shared preferences
            Commons.sharedPreferences!!.edit().remove(TOKEN_KEY).apply()
        }
    }
}