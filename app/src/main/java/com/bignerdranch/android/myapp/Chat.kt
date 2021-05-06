package com.bignerdranch.android.myapp


import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.chat.*
import kotlinx.android.synthetic.main.chat.view.*
import kotlinx.android.synthetic.main.chat_main_row.view.*
import kotlinx.android.synthetic.main.nav_left_header.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class Chat : AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    var curFile: Uri? = null
    val imageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chat)

        recyclerView_chat_main.adapter = adapter
        recyclerView_chat_main.addItemDecoration((DividerItemDecoration(this, DividerItemDecoration.VERTICAL)))

        adapter.setOnItemClickListener { item, view ->
            val intent = Intent(this, ChatLog::class.java)
            val row = item as MainMessageRow
            intent.putExtra(NewMessages.EMAIL_KEY, row.chatWithUser )
            startActivity(intent)
        }

        toggle = ActionBarDrawerToggle(this, drawerChat, R.string.open, R.string.close)

        drawerChat.addDrawerListener(toggle)
        toggle.syncState()

        listenForLatestMessages()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        downloadImage("profilePicture")

        toolbar.setOnMenuItemClickListener{
            when(it.itemId){
                R.id.menu_new_message -> {
                    val intent = Intent(this, NewMessages::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        navViewLeftChat.setNavigationItemSelectedListener {

            val toast = Toast.makeText(this, "You're currently in Chat", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.TOP, 0, 0)

            when (it.itemId) {
                R.id.miHome -> Intent(this, Home::class.java).also {
                    startActivity(it)
                    finish()
                }

                R.id.miChat -> toast.show()

                R.id.miGames -> Intent(this, Game::class.java).also {
                    startActivity(it)
                    finish()
                }
                R.id.miMusic -> Intent(this, Music::class.java).also {
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

    }

    class MainMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
        var chatWithUser: Register.User? = null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textView_latest.text = chatMessage.text
            val chatwithID: String
            if(chatMessage.fromID == FirebaseAuth.getInstance().uid){
                chatwithID = chatMessage.toID
            }
            else{
                chatwithID = chatMessage.fromID
            }
            val ref = FirebaseDatabase.getInstance().getReference("/users/$chatwithID")
            ref.addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatWithUser = snapshot.getValue(Register.User::class.java)
                    viewHolder.itemView.username_main.text =  chatWithUser?.email_info
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        }
        override fun getLayout(): Int {
            return R.layout.chat_main_row
        }
    }

    val latestMessageMap = HashMap<String, ChatMessage>()
    private fun refresh(){
        adapter.clear()
        latestMessageMap.values.forEach {
            adapter.add(MainMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/messages-latest/$fromID")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refresh()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refresh()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    val adapter = GroupAdapter<ViewHolder>()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu_search, menu)
        return super.onCreateOptionsMenu(menu)
    }

    fun chooseImage(view: View) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            startActivityForResult(it, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 0) {

            data?.data?.let {
                curFile = it
                ivProfilePicture.setImageURI(it)
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
                ivProfilePicture.setImageBitmap(bmp)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@Chat, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }
}