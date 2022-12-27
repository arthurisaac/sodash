package com.arthurisaac.application.states

import android.content.ContentResolver
import android.content.Context
import android.provider.Telephony
//import android.util.Log
import com.arthurisaac.application.interfaces.Message

class SMSState(override var context: Context) : Message {
    init {
        getSmsList(context)
    }

    companion object {
        private val smsList: MutableList<MutableMap<String, Any>> = mutableListOf()
        private lateinit var contentResolver: ContentResolver
        private fun getSmsList(context: Context) {
            contentResolver = context.contentResolver

            val dateCol = Telephony.TextBasedSmsColumns.DATE
            val numberCol = Telephony.TextBasedSmsColumns.ADDRESS
            val textCol = Telephony.TextBasedSmsColumns.BODY
            val typeCol = Telephony.TextBasedSmsColumns.TYPE // 1 - Inbox, 2 - Sent

            val projection = arrayOf(dateCol, numberCol, textCol, typeCol)

            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection, null, null, null
            )

            val dateColIdx = cursor!!.getColumnIndex(dateCol)
            val numberColIdx = cursor.getColumnIndex(numberCol)
            val textColIdx = cursor.getColumnIndex(textCol)
            val typeColIdx = cursor.getColumnIndex(typeCol)

            while (cursor.moveToNext()) {
                val smsMap: MutableMap<String, Any> = mutableMapOf()

                val date = cursor.getString(dateColIdx)
                val number = cursor.getString(numberColIdx)
                val text = cursor.getString(textColIdx)
                val type = cursor.getString(typeColIdx)

                smsMap["date"] = date.toLong()
                smsMap["number"] = number
                smsMap["text"] = text
                smsMap["type"] = type.toInt()

                smsList.add(smsMap)

                //Log.d("AI", "$date $number $text $type")
            }

            cursor.close()
        }
    }

    override fun getData(): MutableList<MutableMap<String, Any>> {
            return smsList
    }
}