package com.examper.chatyoxmpp.android.main.model
/**@ChatWrapper is data holder class, used to populate chat of clicked user or group*/
data class ChatWrapper (var jabberId :String, var name:String,
                        var username:String, var lastMessage :String, var unreadMessageCount:String)