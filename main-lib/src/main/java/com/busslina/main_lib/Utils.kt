package com.busslina.main_lib

import java.util.*

class Utils {

    companion object {

        ///
        fun getElapsedTime(initialDate: Date): String {
//            val diff = Date().time - initialDate.time
//            val secondsTotal = (diff / 1000)
            val secondsTotal = getDateDiff(initialDate)
            val seconds = secondsTotal % 60
            val minutesTotal = (secondsTotal / 60)
            val minutes = minutesTotal % 60
            val hours = minutesTotal / 60
            val days = hours / 24

            val secondsF = seconds.toString().padStart(2, '0')
            val minutesF = minutes.toString().padStart(2, '0')
            val hoursF = hours.toString().padStart(2, '0')

            return "$hoursF:$minutesF:$secondsF"
        }

        /// Difference between two dates in seconds.
        fun getDateDiff(initialDate: Date, finalDate: Date = Date()): Int {
            val diff = finalDate.time - initialDate.time
            return (diff / 1000).toInt()
        }
    }

}