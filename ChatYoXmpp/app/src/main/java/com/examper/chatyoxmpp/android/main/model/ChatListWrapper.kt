package com.examper.chatyoxmpp.android.main.model
/**@ChatListWrapper data holder class which used to populate chat list data in @MainActivity.kt*/
data class ChatListWrapper (var jabberId :String, var name:String,
                            var username:String, var lastMessage :String, var unreadMessageCount:String)