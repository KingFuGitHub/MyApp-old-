package com.bignerdranch.android.myapp

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await


class Register : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        auth = FirebaseAuth.getInstance()

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnGoToLogin.setOnClickListener {
            Intent(this@Register, Login::class.java).also {
                startActivity(it)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }
        }
    }

    private fun registerUser() {
        val email = etEmailRegister.text.toString()
        val password = etPasswordRegister.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(this@Register, "Registered Successfully!", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        Intent(this@Register, Login::class.java).also {
                            startActivity(it)
                            finish()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Register, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun saveUserToFirebaseDatabase() {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, etEmailRegister.text.toString())
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")
            }
            .addOnFailureListener {
                Log.d("Failed", "Failed to set value to database: ${it.message}")
            }
    }

    @Parcelize
    class User(val uid: String, val email_info: String): Parcelable {
        constructor() : this("","")
    }


}