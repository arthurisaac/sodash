package com.arthurisaac.application.states

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.arthurisaac.application.interfaces.Message


class CallLogState(override var context: Context) : Message {
    init {
        getCallLogList(context)
    }
    override fun getData(): MutableList<MutableMap<String, Any>> {
        return callLogList
    }

    companion object {
        private val callLogList: MutableList<MutableMap<String, Any>> = mutableListOf()
        private lateinit var contentResolver: ContentResolver
        private fun getCallLogList(context: Context) {
            contentResolver = context.contentResolver

            val numberCol = CallLog.Calls.NUMBER
            val typeCol: String = CallLog.Calls.TYPE // 1 - Incoming, 2 - Outgoing, 3 - Missed
            val dateCol: String = CallLog.Calls.DATE
            val durationCol: String = CallLog.Calls.DURATION

            val projection = arrayOf(numberCol, typeCol, dateCol, durationCol)
            val cursor = contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                projection, null, null, null, null
            )

            val numberColIdx = cursor!!.getColumnIndex(numberCol)
            val typeColIdx = cursor.getColumnIndex(typeCol)
            val dateColIdx = cursor.getColumnIndex(dateCol)
            val durationColIdx = cursor.getColumnIndex(durationCol)

            while (cursor.moveToNext()) {
                val callLogMap: MutableMap<String, Any> = mutableMapOf()

                val number = cursor.getString(numberColIdx)
                val duration = cursor.getString(durationColIdx)
                val type = cursor.getString(typeColIdx)
                val date = cursor.getString(dateColIdx)

                callLogMap["date"] = date.toLong()
                callLogMap["number"] = number
                callLogMap["type"] = type.toInt()
                callLogMap["duration"] = duration.toLong()

                callLogList.add(callLogMap)
            }

        }
    }
}