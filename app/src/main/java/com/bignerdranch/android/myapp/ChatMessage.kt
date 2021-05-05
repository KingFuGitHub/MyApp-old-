package com.bignerdranch.android.myapp

class ChatMessage(val id: String, val text: String, val fromID: String, val toID: String, val timestamp: Long){
    constructor() : this("","","","",-1)
}