package com.arthurisaac.application.providers

import com.arthurisaac.application.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object Database {
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var user: FirebaseUser? = mAuth.currentUser

    init {
        mAuth.useEmulator("10.0.2.2", 9099)
    }

    fun DBReference(): DatabaseReference {
        val deviceName = Utils.getDeviceName()
        val database = FirebaseDatabase.getInstance()
        database.useEmulator("10.0.2.2", 9000)
        return database.reference
            .child(user!!.uid)
            .child(deviceName)
    }
}