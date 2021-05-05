package com.bignerdranch.android.myapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.internal.HideFirstParty
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_new_messages.*
import kotlinx.android.synthetic.main.user_row_new_m.view.*

class NewMessages : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_messages)

        supportActionBar?.title = "Pick someone to chat with"
        fetchUser()
    }

    companion object {
        val EMAIL_KEY = "USER_KEY"
    }

    private fun fetchUser(){
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(p0: DataSnapshot){
                val adapter = GroupAdapter<ViewHolder>()
                p0.children.forEach{

                    val user = it.getValue(Register.User::class.java)
                    if(user != null && user.uid != FirebaseAuth.getInstance().uid ){
                        adapter.add(UserItem(user))
                    }
                }

                adapter.setOnItemClickListener{ item, view ->
                    val userItem = item as UserItem
                    val intent = Intent(view.context, ChatLog::class.java)
                    intent.putExtra(EMAIL_KEY, userItem.user)
                    startActivity(intent)
                    finish()
                }

                recyclerview_newMessages.adapter = adapter
            }
            override fun onCancelled(p0: DatabaseError){

            }
        })
    }
}

class UserItem(val user: Register.User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_view.text = user.email_info
    }
    override fun getLayout(): Int {
        return R.layout.user_row_new_m
    }
}
