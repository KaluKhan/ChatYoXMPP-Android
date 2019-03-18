package com.examper.chatyoxmpp.android.main.constants

object UserChatDbColumns {

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.yaxim.userchat"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.yaxim.userchats"
    const val DEFAULT_SORT_ORDER = "date ASC" // sort

    // boolean mappings
    const val INCOMING = 0
    const val OUTGOING = 1

    const val _ID = "_id"
    const val LOGIN_USER_JID = "login_user_jid"
    const val DATE = "date"
    const val DIRECTION = "direction"
    const val MESSAGE_SUBJECT = "subject"
    const val BODY = "Body"
    const val PACKET_ID = "packetId"
    const val FRIEND_JID = "friend_jid"
    const val SENDER_NAME = "sender_name"

}