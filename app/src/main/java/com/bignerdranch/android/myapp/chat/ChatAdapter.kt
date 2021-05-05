package com.bignerdranch.android.myapp.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.bignerdranch.android.myapp.R

class ChatAdapter (var context : Context, var chatList : ArrayList<Conversation>) : BaseAdapter() {
    class ChatHolder(row : View) {
        var chatName : TextView = row.findViewById(R.id.nameChat) as TextView
        var currentMessage : TextView = row.findViewById(R.id.currentMessage) as TextView
    }

    override fun getCount(): Int {
        return chatList.size
    }

    override fun getItem(position: Int): Any {
        return chatList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view : View?
        var chatHolder : ChatHolder
        if (convertView == null) {
            var layoutInflater : LayoutInflater = LayoutInflater.from(context)
            view = layoutInflater.inflate(R.layout.conversation, null)
            chatHolder = ChatHolder(view)
            view.tag = chatHolder
        } else {
            view = convertView
            chatHolder = convertView.tag as ChatHolder
        }

        var conversation : Conversation = getItem(position) as Conversation
        chatHolder.chatName.text = conversation.name
        chatHolder.currentMessage.text = conversation.messages.last()

        return view as View
    }
}