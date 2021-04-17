package com.silasonyango.ndma.util

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object Util {
    @Throws(Exception::class)
    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getNow(): String {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val currentTime = Calendar.getInstance().time
        return dateFormat.format(currentTime)
    }
}