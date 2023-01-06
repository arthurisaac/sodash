package com.arthurisaac.application.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import com.arthurisaac.application.providers.Auth
import com.arthurisaac.application.providers.Database
import com.arthurisaac.application.providers.Storage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class CallReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        try {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d("AI", "Ringing State Number is -$incomingNumber")
            }
            if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
                Log.d("AI", "Received State")
            }
            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d("AI", "Idle State")
                getRecordsLists(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRecordsLists(context: Context) {
        val callsDir =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/records/calls/"
        File(callsDir).walk().forEach {
            Log.d("AI", it.path)
            val file = File(it.path)
            if (file.isFile) {
                uploadCallFile(file)
            }
        }
    }

    private fun uploadCallFile(file: File) {
        val mAuth: FirebaseAuth = Auth.getInstance()
        val user: FirebaseUser? = mAuth.currentUser
        val uri = Uri.fromFile(file)
        Storage.storageRef()
            .child("calls")
            .child("${user!!.uid}/${uri.lastPathSegment}")
            .putFile(uri)
            .addOnSuccessListener { taskSnapshot ->
                val callMap: MutableMap<String, Any> = mutableMapOf()
                callMap["file"] = taskSnapshot.metadata?.path.toString()
                callMap["size"] = taskSnapshot.metadata?.sizeBytes.toString()
                callMap["date"] = taskSnapshot.metadata?.creationTimeMillis.toString()

                val name = uri.lastPathSegment.toString().replace(".", "")
                saveLink(callMap, name)
            }

    }

    private fun saveLink(callMap: MutableMap<String, Any>, lastPathSegment: String) {
        Database.DBReference()
            .child("CALL_RECORDS")
            .child(lastPathSegment)
            .updateChildren(callMap)
    }
}