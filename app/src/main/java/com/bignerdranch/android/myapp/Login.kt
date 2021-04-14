package com.bignerdranch.android.myapp

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.register.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class Login : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        auth = FirebaseAuth.getInstance()

        btnGoToRegister.setOnClickListener {
            Intent(this, Register::class.java).also {
                startActivity(it)
            }
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
    }


    private fun loginUser() {
        val email = etEmailLogin.text.toString()
        val password = etPasswordLogin.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if(email.isEmpty() && password.isEmpty()){
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@Login, "Please enter email and password", Toast.LENGTH_LONG).show()
                        }
                    }
                    auth.signInWithEmailAndPassword(email, password).await()
                    withContext(Dispatchers.Main) {
                        val toast = Toast.makeText(this@Login, "Login Successfully!", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        Intent(this@Login, Home::class.java).also {
                            startActivity(it)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Login, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

    }

}