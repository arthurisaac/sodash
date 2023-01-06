package com.arthurisaac.application.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.arthurisaac.application.MainActivity
import com.arthurisaac.application.R
import com.arthurisaac.application.providers.Auth
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginActivity : AppCompatActivity() {

    private lateinit var editEmail: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var btnLogin: Button

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        editEmail = findViewById(R.id.edit_email)
        editPassword = findViewById(R.id.edit_password)
        btnLogin = findViewById(R.id.btn_login)

        mAuth = Auth.getInstance()
        if (mAuth.currentUser != null) {
            openMainActivity()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = editEmail.text.toString()
        val password = editPassword.text.toString()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    openMainActivity()
                }
            }
            .addOnFailureListener {
                val errorCode = (it as FirebaseAuthException).errorCode
                Log.w("AI", "signInWithEmail:failure error code ${errorCode.toString()}")
                if (errorCode == "ERROR_USER_NOT_FOUND") {
                    createUser(email, password)
                }
            }
    }

    private fun createUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("AI", "createUserWithEmail:success")
                    openMainActivity()
                } else {
                    Log.w("", "createUserWithEmail:failure", task.exception)
                    toast(task.exception.toString())
                }
            }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        if (mAuth.currentUser != null) {
            openMainActivity()
        }
    }

}