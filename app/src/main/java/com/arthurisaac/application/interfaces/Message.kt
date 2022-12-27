package com.arthurisaac.application.interfaces

import android.content.Context

interface Message {
    var context: Context

    fun getData(): MutableList<MutableMap<String, Any>>
}