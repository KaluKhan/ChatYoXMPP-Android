package com.examper.chatyoxmpp.android.main.xmpp

import android.provider.BaseColumns

class ChatTableColumns : BaseColumns{
    companion object : BaseColumnsKotlin(){
        // boolean mappings
        val INCOMING = 0
        val OUTGOING = 1

        // by
        // auto-id
        val USER_JID = "chatting_userJ_id"
        val DATE = "date"
        val DIRECTION = "direction"
        val MESSAGE_SUBJECT = "subject" //Subject means type of message.
        // For ex. 'message' or 'file' etc.
        val BODY = "body"
        val PACKET_ID = "packetId"//every stanza has unique is
        val SENDER_NAME = "sender_name"
        val SENDER_IMAGE_URL = "sender_image"// if available in your app
    }
}