package com.examper.chatyoxmpp.android.main.constants

object AppConstants {
    /**Port of mine chat server, you can change as yours*/
    //val HOST = ""//you can put host name here, we are picking host name from SharedPreference
    //to open admin console over web http://your_host:9090/
    val PORT = 5222 //port may varies
    //shared preference keys
    const val KEY_PREFS_HOST = "host"
    const val KEY_PREFS_NAME = "name"
    const val KEY_PREFS_JABBER = "jabber"
    const val KEY_PREFS_PASSWORD = "password"
    const val TYPE_USER_CHAT = "type_user_chat"//type=group_chat or single user chat
    //bundle keys
    const val BUNDLE_KEY_HOST = "key_host"
    const val BUNDLE_KEY_JABBER = "key_jabber"
    const val BUNDLE_KEY_PASSWORD = "key_password"
    const val BUNDLE_KEY_NAME = "key_name"
    const val BUNDLE_KEY_IS_NOTIF_CLICK = "key_notif_click"
    const val BUNDLE_KEY_FRIEND_NAME = "key_friend_name"
    const val BUNDLE_KEY_FRIEND_JID = "key_friend_jid"

    val CHAT_LIST_PROJECTION_FROM = arrayOf(
        ChatListDbColumns._ID,
        ChatListDbColumns.LAST_MESSAGE,
        ChatListDbColumns.DATE,
        ChatListDbColumns.LOGIN_USER_JID,
        ChatListDbColumns.FRIEND_JID,
        ChatListDbColumns.FRIEND_NAME,
        ChatListDbColumns.UNREAD_MSG_COUNT
    )
    val CHAT_PROJECTION_FROM = arrayOf(
        UserChatDbColumns._ID,
        UserChatDbColumns.MESSAGE_SUBJECT,
        UserChatDbColumns.DATE,
        UserChatDbColumns.BODY,
        UserChatDbColumns.LOGIN_USER_JID,
        UserChatDbColumns.PACKET_ID,
        UserChatDbColumns.DIRECTION,
        UserChatDbColumns.FRIEND_JID,
        UserChatDbColumns.SENDER_NAME
    )
}