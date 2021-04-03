package com.silasonyango.ndma.util

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNow(): String {
        val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val now: LocalDateTime = LocalDateTime.now()
        return dtf.format(now)
    }
}