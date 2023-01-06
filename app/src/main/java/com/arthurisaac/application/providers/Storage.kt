package com.arthurisaac.application.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

object Storage {
    private var storage : FirebaseStorage = Firebase.storage

    init {
        storage.useEmulator("10.0.2.2", 9199)
    }

    fun storageRef(): StorageReference {
        return storage.reference
    }
}