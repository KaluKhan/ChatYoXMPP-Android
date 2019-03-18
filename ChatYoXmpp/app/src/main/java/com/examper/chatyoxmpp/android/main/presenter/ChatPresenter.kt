package com.examper.chatyoxmpp.android.main.presenter

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.examper.chatyoxmpp.android.main.ChatScreenListener
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.constants.ChatListDbColumns
import com.examper.chatyoxmpp.android.main.constants.MessageConstants
import com.examper.chatyoxmpp.android.main.constants.UserChatDbColumns
import com.examper.chatyoxmpp.android.main.services.XMPPService
import com.examper.chatyoxmpp.android.main.xmpp.ChatProvider
import org.json.JSONException
import org.json.JSONObject

class ChatPresenter(
    private var mContext: Context, private var mListener: ChatScreenListener?,
    private var friendJId: String?, private var friendName: String?
) {

    private var mBounded: Boolean? = false
    private var xmppService: XMPPService? = null

    init {
        if (mListener != null) {
            //call method here to get on init
            initChatCursor(friendJId)
        }
    }

    fun onResume(friendJId: String?) {
        val intent = Intent(mContext, XMPPService::class.java)
        mContext.bindService(intent, mConnection, 0)
        ChatYoApplication.setCurrentChatFriendId(friendJId!!)
        setUnreadMsgCountStatus()
    }

    fun onPause() {
        ChatYoApplication.setCurrentChatFriendId("")
        setUnreadMsgCountStatus()
    }

    fun onStop() {
        if (mBounded!!) {
            mContext.unbindService(mConnection)
            mBounded = false
        }
    }

    fun onDestroy() {
        mListener = null
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(
            mMessageReceiver
        )
    }

    private fun initChatCursor(friendJId: String?) {
        val selection = (UserChatDbColumns.LOGIN_USER_JID + "='"
                + ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER) + "' and "
                + UserChatDbColumns.FRIEND_JID + "='" + friendJId + "'")

        val cursor = mContext.contentResolver.query(
            ChatProvider.CONTENT_URI_USER_CHAT,
            AppConstants.CHAT_PROJECTION_FROM, selection, null, null
        )
        // Register to receive flag from XMPPService.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "custom-event-name".
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(mMessageReceiver, IntentFilter("custom-event-name"))
        mListener?.setData(cursor)
    }

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "custom-event-name" is BROADCASTS.
    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mListener?.onBroadCastReceive()
        }
    }

    fun onMessageSend(textMessage: String) {
        sendMessageIfNotNull(AppConstants.TYPE_USER_CHAT, textMessage)
    }

    private var mConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceDisconnected(name: ComponentName) {

            mBounded = false
            xmppService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {

            mBounded = true
            val mLocalBinder = service as XMPPService.LocalBinder
            xmppService = mLocalBinder.serverInstance
            xmppService!!.notificationService.resetNotificationCounter(friendJId!!)
            xmppService!!.notificationService.clearNotification(friendJId!!)

        }
    }

    private fun sendMessageIfNotNull(type: String, message: String) {
        //We are sending/receiving message packet on JSON, so below are JSON keys.
        val json = JSONObject()
        try {
            json.put(MessageConstants.MESSAGE, message)
            json.put(MessageConstants.RECEIVER_JID, friendJId)
            json.put(MessageConstants.RECEIVER_NAME, friendName)
            json.put(MessageConstants.SENDER_JID, ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER))
            json.put(MessageConstants.SENDER_NAME, ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_NAME))
            json.put(MessageConstants.MESSAGE_TIME, "" + System.currentTimeMillis())

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        sendMessage(type, json.toString())
    }

    private fun sendMessage(type: String, message: String) {
        if (xmppService != null)
            xmppService!!.sendMessage(
                friendJId + "@" + ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_HOST),
                type,
                message
            )
    }

    private fun setUnreadMsgCountStatus() {
        val cursor11 = mContext.contentResolver.query(
            ChatProvider.CONTENT_URI_USER_LIST,
            AppConstants.CHAT_LIST_PROJECTION_FROM, null, null, null
        )
        try {
            if (cursor11!!.moveToFirst()) {
                do {
                    val fId = cursor11.getString(cursor11.getColumnIndex(ChatListDbColumns.FRIEND_JID))
                    val _ID = cursor11.getInt(
                        cursor11
                            .getColumnIndex(ChatListDbColumns._ID)
                    )
                    if (fId.equals(friendJId!!, ignoreCase = true)) {
                        updateUnreadMsgCount(_ID)
                        break
                    }

                } while (cursor11.moveToNext())
            }
            cursor11.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun updateUnreadMsgCount(id: Int) {
        val rowUri = Uri.parse(
            "content://" + ChatProvider.AUTHORITY + "/"
                    + ChatProvider.TABLE_NAME_USER_LIST + "/" + id
        )
        val values = ContentValues()
        values.put(ChatListDbColumns.UNREAD_MSG_COUNT, "0")
        mContext.contentResolver.update(rowUri, values, null, null)
    }
//we can delete chat message by long clicking list item
    @Throws(Exception::class)
    private fun removeChatHistoryForPacketID(position: Int) {

    val selection = (UserChatDbColumns.LOGIN_USER_JID + "='"
            + ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER) + "' and "
            + UserChatDbColumns.FRIEND_JID + "='" + friendJId + "'")

    val cursor = mContext.contentResolver.query(
        ChatProvider.CONTENT_URI_USER_CHAT,
        AppConstants.CHAT_PROJECTION_FROM, selection, null, null
    )
    cursor!!.moveToPosition(position)
        val packetId = cursor.getString(cursor.getColumnIndex(UserChatDbColumns.PACKET_ID))
        val qrDel = (UserChatDbColumns.FRIEND_JID + "='" + friendJId + "'" + " and "
                + UserChatDbColumns.PACKET_ID + "='" + packetId + "'")

        mContext.contentResolver.delete(ChatProvider.CONTENT_URI_USER_CHAT, qrDel, null)
    }
}