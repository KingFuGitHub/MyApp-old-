package com.bignerdranch.android.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.them_chat.view.*
import kotlinx.android.synthetic.main.me_chat.view.*

class ChatLog : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<ViewHolder>()
    var toUser: Register.User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatlog.adapter = adapter

        toUser = intent.getParcelableExtra<Register.User>(NewMessages.EMAIL_KEY)

        supportActionBar?.title = toUser?.email_info


        listenForMessages()

        sendButton_chat.setOnClickListener{
            Log.d(TAG, "trying to type")
            sendMessage()
        }
    }

    private fun listenForMessages(){
        val fromID = FirebaseAuth.getInstance().uid
        val toID = toUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/messages-store/$fromID/$toID")
        ref.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if(chatMessage != null) {
                    if(chatMessage.fromID == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatFromItem(chatMessage.text))
                    }
                    else {
                        adapter.add(ChatToItem(chatMessage.text))
                    }
                }
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
            }
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }
            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }


    private fun sendMessage(){
        val text = editText_chat.text.toString()
        val user = intent.getParcelableExtra<Register.User>(NewMessages.EMAIL_KEY)
        val fromID = FirebaseAuth.getInstance().uid
        val toID = user?.uid

        if(fromID == null) return

        val reference = FirebaseDatabase.getInstance().getReference("/messages-store/$fromID/$toID").push()

        val toReference = FirebaseDatabase.getInstance().getReference("/messages-store/$toID/$fromID").push()

        val chatMessage = ChatMessage(reference.key!!, text, fromID, toID!!, System.currentTimeMillis()/1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                editText_chat.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("messages-latest/$fromID/$toID")
        latestMessageRef.setValue(chatMessage)
        val latestMessageRefto = FirebaseDatabase.getInstance().getReference("messages-latest/$toID/$fromID")
        latestMessageRefto.setValue(chatMessage)
    }
}

class ChatFromItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.textView_from.text = text
    }
    override fun getLayout(): Int {
        return R.layout.me_chat
    }
}
class ChatToItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int){
        viewHolder.itemView.textView_to.text = text
    }
    override fun getLayout(): Int {
        return R.layout.them_chat
    }
}