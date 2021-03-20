package com.silasonyango.ndma.util

import java.util.*

object Util {
    @Throws(Exception::class)
    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }
}