package com.examper.chatyoxmpp.android.main.xmpp

import android.provider.BaseColumns

class ChatListTableColumns : BaseColumns {
    companion object : BaseColumnsKotlin() {

        val LAST_MESSAGE = "last_message"
        val UNREAD_MSG_COUNT = "unread_msg_count"
        val USER_J_ID = "USER_J_IDS"// this is always other user's id who is chatting with me
        val NAME = "name"// this is always other user's name who is chatting with me
        val DATE = "date"
        val USER_IMAGE = "user_image"
    }
}