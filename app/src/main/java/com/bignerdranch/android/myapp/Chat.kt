package com.bignerdranch.android.myapp


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.bignerdranch.android.myapp.chat.ChatAdapter
import com.bignerdranch.android.myapp.chat.Conversation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.chat.*
import kotlinx.android.synthetic.main.nav_left_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

var TAG = "Printing..."

class Chat : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    var curFile: Uri? = null
    private val imageRef = Firebase.storage.reference
    private lateinit var auth: FirebaseAuth
    private lateinit var conversationDatabaseReference : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        auth = FirebaseAuth.getInstance()
        conversationDatabaseReference = FirebaseDatabase.getInstance()
        toggle = ActionBarDrawerToggle(this, drawerChat, R.string.open, R.string.close)

        drawerChat.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadImage("profilePicture")


        navView.setNavigationItemSelectedListener {

            val toast = Toast.makeText(this, "You're currently in Chat", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miHome -> Intent(this, Home::class.java).also {
                    startActivity(it)
                    finish()
                }

                R.id.miCapture -> Intent(this, Capture::class.java).also {
                    startActivity(it)
                    finish()
                }

                R.id.miChat -> toast.show()

                R.id.miGames -> Intent(this, Games::class.java).also {
                    startActivity(it)
                    finish()
                }
                R.id.miMusic -> Intent(this, Music::class.java).also {
                    startActivity(it)
                    finish()
                }

                R.id.miSetting -> Intent(this, Settings::class.java).also {
                    startActivity(it)
                    finish()
                }

                R.id.miLogout -> Intent(this, Login::class.java).also {
                    startActivity(it)
                    finish()
                }
            }
            true
        }

        loadingChat()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun chooseImage(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(it, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 0) {

            data?.data?.let {
                curFile = it
                ivImage.setImageURI(it)
                uploadImageToStorage("profilePicture") // to upload profile picture to Firebase Cloud Storage
            }
        }
    }

    private fun uploadImageToStorage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            curFile?.let {
                imageRef.child("images/$filename").putFile(it).await()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Chat, "Successfully uploaded image", Toast.LENGTH_LONG)
                        .show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Chat, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun downloadImage(filename: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val maxDownloadSize = 5L * 1024 * 1024
            val bytes = imageRef.child("images/$filename").getBytes(maxDownloadSize).await()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            withContext(Dispatchers.Main) {
                ivImage.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Chat, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadingChat() {
        val currentUID : String= auth.currentUser.uid
        Log.d(TAG, "Current User ID: $currentUID")
        var chatList : ArrayList<Conversation> = ArrayList()

        conversationDatabaseReference
            .getReference(currentUID)
            .child("conversations")
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { it ->
                    var key = it.key.toString()
                    val value = it.value as ArrayList<String>
                    value.removeAt(0)
                    Log.d(TAG, "Value is: $key + $value")
                    chatList.add(Conversation(key, value))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })

        listChat.adapter = ChatAdapter(this@Chat, chatList)
    }
}