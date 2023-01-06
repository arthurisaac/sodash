package com.arthurisaac.application.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object Auth {
    private lateinit var mAuth: FirebaseAuth

    init {
        mAuth = FirebaseAuth.getInstance()
        mAuth.useEmulator("10.0.2.2", 9099)
    }

    fun getInstance(): FirebaseAuth {
        mAuth = FirebaseAuth.getInstance()
        return mAuth
    }
}