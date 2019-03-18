package com.examper.chatyoxmpp.android.main.xmpp

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.constants.ChatListDbColumns
import com.examper.chatyoxmpp.android.main.constants.MessageConstants
import com.examper.chatyoxmpp.android.main.constants.UserChatDbColumns
import com.examper.chatyoxmpp.android.main.utils.ConnectionDetector
import org.jetbrains.anko.doAsync
import org.jivesoftware.smack.*
import org.jivesoftware.smack.SmackException.NotConnectedException
import org.jivesoftware.smack.packet.IQ
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.provider.ProviderManager
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.ping.packet.Ping
import org.jivesoftware.smackx.ping.provider.PingProvider
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


/**@XmppConnection class have all necessary functions of XMPP, Like
 * Authenticate a user, send a message, process received Packet, connect/disconnect service,
 * generate a notification for unread message
 * as well as store messages in database*/
class XmppConnection(
    context: Context?,
    private val userName: String?,//here 'userName' is jabberId
    private val password: String?,
    private val reconnectedListener: XMPPReconnectedListener
) : StanzaListener, ConnectionListener {

    private var notificationService: NotificationService
    private var configBuilder: XMPPTCPConnectionConfiguration.Builder
    private var connection: XMPPTCPConnection? = null
    private var context: Context? = null

    private var cd: ConnectionDetector

    companion object {
        internal var isConnecting: Boolean? = false
        private var currentXmppResource = ""
        private val TAG = XmppConnection::class.java.simpleName
        private const val RECONNECT_TIME_INTERVAL: Long = 10000


    }

    init {

        this.context = context
        configBuilder = XMPPTCPConnectionConfiguration.builder()
        configBuilder.setUsernameAndPassword(this.userName, this.password)
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
        configBuilder.setServiceName(ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_HOST))
        configBuilder.setPort(AppConstants.PORT)
        configBuilder.setHost(ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_HOST))
        configBuilder.setDebuggerEnabled(true)
        currentXmppResource = ChatYoApplication.getResource()
        configBuilder.setResource(currentXmppResource)
        configBuilder.setSendPresence(true)
        cd = ConnectionDetector(this.context)
        notificationService = NotificationService(this.context)
    }

    fun doConnect() {
        //by Anko Lib
        doAsync {
            try {
                connect("XmppConnection constructor")
            } catch (e: XMPPException) {
                Log.e(TAG, "INIT XMPP EXCEPTION:" + e.message)
            } catch (e: IOException) {
                Log.e(TAG, "INIT IO EXCEPTION:" + e.message)
            } catch (e: SmackException) {
                Log.e(TAG, "INIT SMACK EXCEPTION:" + e.message)
            }
            /**To return on main thread
             *  uiThread {
            toast(result)
            }*/
        }

    }


    @Throws(XMPPException::class, IOException::class, SmackException::class)
    private fun connect(tag: String) {
        Log.i(TAG, "----call connect from $tag")
        Log.d(TAG, "----user jid:" + this.userName)
        Log.d(TAG, "----user Password:" + this.password)


        //removing the listener
        if (connection != null) {
            connection!!.removeAsyncStanzaListener(this@XmppConnection)
            connection!!.removeConnectionListener(this@XmppConnection)
            connection = null
        }
        connection = XMPPTCPConnection(configBuilder.build())
        connection!!.packetReplyTimeout = 30000
        connection!!.addConnectionListener(this@XmppConnection)
        connection!!.addAsyncStanzaListener(this@XmppConnection, null)

        try {
            connection!!.connect()
            if (tag == "reconnect") {
                reconnectedListener.onXMPPReConnected()
            }
        } catch (e: Exception) {
            Log.e(TAG, "--Connect Exception:" + e.message)
            reconnectedListener.onXMPPConnectionError(e.message)
        }

        val objRoster = Roster.getInstanceFor(connection)
        objRoster.isRosterLoadedAtLogin = false
        Log.i(TAG, "----xmpp connected")
        connection!!.login()


        if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
            Log.i(TAG, "----login succesful....")
            //We'll save login user's credentials (jabberId, password etc.)here,
            // currently I'm saving on login button click, which is not recommended.
        }

        ProviderManager.addIQProvider("ping", "urn:xmpp:ping", PingProvider())
        sendOnlinePresence()

    }

    fun reConnect() {
        if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
            Log.i(TAG, "Xmpp already connected and authenticated")
            return
        }
        Log.i(TAG, "Xmpp reconnect attempt")
        //here we send flag to disconnect to stop wait bar in chat window
        try {
            this.connection!!.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Xmpp reconnect attempt error " + e.message)
        }

        doAsync {
            while (true) {
                try {
                    Thread.sleep(RECONNECT_TIME_INTERVAL)
                    connect("reconnect")
                } catch (e: XMPPException) {
                    Log.e(TAG, "Xmpp reconnecting error " + e.message)
                    if (isConflictError(e.message)) {
                        Log.d(TAG, "is XMPP conflict error occurred")
                    }
                } catch (e: SmackException.NoResponseException) {
                    Log.e(TAG, "NO RESPONSE EXCEPTION")
                } catch (e: SmackException) {
                    Log.e(TAG, "Smack reconnecting error " + e.message)
                    //PdDriver.sleep(RECONNECT_TIME_INTERVAL);
                } catch (e: IOException) {
                    Log.e(TAG, "Smack reconnecting error " + e.message)
                }
            }
        }
    }


    private fun isConflictError(errorMessage: String?): Boolean {
        var isError = false
        Log.d(TAG, "is XMPP conflict error: $errorMessage")
        when (errorMessage?.contains("<conflict")) {

            true -> isError = true
            false -> isError = false
        }
        return isError
    }

    //below methods used to send online/offline status of user.
    private fun sendOnlinePresence() {

        val presence = Presence(Presence.Type.available)
        presence.status = "Online"
        presence.priority = 1
        try {
            //            connection.sendPacket(presence);
            connection!!.sendStanza(presence)
        } catch (e: NotConnectedException) {
            Log.e(TAG, "Error in sending online presence:" + e.message)
        }

    }

    private fun sendOfflinePresence(from: String) {

        val presence = Presence(Presence.Type.unavailable)
        presence.priority = 1
        presence.from = from
        presence.to = ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_HOST)
        try {
            connection!!.sendStanza(presence)
        } catch (e: NotConnectedException) {
            Log.e(TAG, "Error in sending offline presence:" + e.message)
        }

    }

    //@sendServerPing() method used to send ping
    fun sendServerPing() {

        val ping = Ping()
        ping.type = IQ.Type.get
        ping.to = ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_HOST)
        try {
            connection!!.sendStanza(ping)
        } catch (e: NotConnectedException) {
            e.printStackTrace()
        }

    }

    fun isAuthenticated(): Boolean? {

        return (connection != null && connection!!.isAuthenticated)
    }

    fun isConnected(): Boolean? {

        return (connection != null && connection!!.isConnected)
    }

    fun getActiveConnection(): XMPPTCPConnection? {
        return connection
    }

    fun sendMessage(ToJid: String, Subject: String, msg_body: String): Boolean {

        val message = Message()
        message.body = msg_body
        message.type = Message.Type.chat
        message.to = ToJid
        message.subject = Subject

        try {
            if (connection != null && connection!!.isConnected && connection!!.isAuthenticated) {
                connection!!.sendStanza(message)
                try {
                    addChatMessageToDB(
                        ChatTableColumns.OUTGOING, ToJid,
                        msg_body, Subject,
                        System.currentTimeMillis(),
                        message.stanzaId
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

                return true
            } else {
                Log.i(TAG, "---- not connected ")
                return false
            }
        } catch (e: NotConnectedException) {
            e.printStackTrace()
            return false
        }

    }

    fun getNotificationService(): NotificationService {
        return notificationService
    }

    //add incoming/outgoing message into local database
    @Throws(JSONException::class)
    private fun addChatMessageToDB(
        direction: Int,
        JID: String,
        messageJSON: String,
        messageType: String,
        ts: Long,
        packetID: String
    ) {
        //checking 'messageType'. This can be 'single user chat' or group chat etc.
        when (messageType) {
            AppConstants.TYPE_USER_CHAT -> {

                val jsonObject = JSONObject(messageJSON)
                val values = ContentValues()
                val message = getValueFromJSON(jsonObject, MessageConstants.MESSAGE)
                val receiverJId = getValueFromJSON(jsonObject, MessageConstants.RECEIVER_JID)
                val receiverName = getValueFromJSON(jsonObject, MessageConstants.RECEIVER_NAME)
                val senderName = getValueFromJSON(jsonObject, MessageConstants.SENDER_NAME)
                //unique id here is jabberId of user
                val senderId = getValueFromJSON(jsonObject, MessageConstants.SENDER_JID)
                val senderJId = JID.split("@".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

                values.put(UserChatDbColumns.PACKET_ID, packetID)
                values.put(UserChatDbColumns.BODY, message)
                values.put(UserChatDbColumns.MESSAGE_SUBJECT, messageType)
                values.put(
                    UserChatDbColumns.LOGIN_USER_JID,
                    ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER)
                )
                values.put(UserChatDbColumns.DATE, ts)
                values.put(UserChatDbColumns.SENDER_NAME, senderName)
                values.put(UserChatDbColumns.DIRECTION, direction)
                val frId: String?
                val frName: String?
                if (direction == UserChatDbColumns.INCOMING) {
                    frId = senderJId
                    frName = senderName
                    values.put(UserChatDbColumns.FRIEND_JID, senderJId)
                    if (!senderId.equals(ChatYoApplication.getCurrentChatFriendId(), ignoreCase = true)) {
                        //fromJid: String, senderName: String, message: String, showNotification: Boolean
                        notificationService.notifyClient(
                            senderId,
                            senderName,
                            message,
                            true
                        )
                    }
                } else {
                    frId = receiverJId
                    frName = receiverName
                    values.put(UserChatDbColumns.FRIEND_JID, receiverJId)
                }
                this.context?.contentResolver?.insert(ChatProvider.CONTENT_URI_USER_CHAT, values)

                updateChatList(direction, message, ts, frId, frName)
            }
        }
    }

    //Update data in chat list
    private fun updateChatList(direction: Int, message: String, ts: Long, friendJId: String, friendName: String) {
        val selection = (ChatListDbColumns.LOGIN_USER_JID + "='"
                + ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER) + "'")
        val cursor1 = this.context?.contentResolver?.query(
            ChatProvider.CONTENT_URI_USER_LIST,
            AppConstants.CHAT_LIST_PROJECTION_FROM,
            selection, null, null
        )

        var isUserExist: Boolean? = false
        if (cursor1!!.count > 0) {
            cursor1.moveToFirst()

            do {
                val fJId = cursor1.getString(cursor1.getColumnIndex(ChatListDbColumns.FRIEND_JID))
                if (fJId.equals(friendJId, ignoreCase = true)) {
                    isUserExist = true
                    val values = ContentValues()
                    val _ID = cursor1.getString(
                        cursor1.getColumnIndex(ChatListDbColumns._ID)
                    )
                    val unReadCount = cursor1
                        .getInt(cursor1.getColumnIndex(ChatListDbColumns.UNREAD_MSG_COUNT))
                    if (direction == UserChatDbColumns.INCOMING) {
                        values.put(
                            ChatListDbColumns.UNREAD_MSG_COUNT,
                            unReadCount + 1
                        )
                    }
                    values.put(ChatListDbColumns.LAST_MESSAGE, message)
                    values.put(ChatListDbColumns.DATE, ts)

                    val rowUri = Uri.parse(
                        "content://"
                                + ChatProvider.AUTHORITY + "/"
                                + ChatProvider.TABLE_NAME_USER_LIST + "/"
                                + _ID
                    )

                    this.context?.contentResolver?.update(rowUri, values, null, null)
                    break
                }

            } while (cursor1.moveToNext())
            cursor1.close()
        }


        if (!isUserExist!!) {
            addToUserInbox(direction, message, ts, friendJId, friendName)
        }

    }

    private fun addToUserInbox(direction: Int, message: String, ts: Long, friendJId: String, friendName: String) {
        val values = ContentValues()
        values.put(ChatListDbColumns.FRIEND_JID, friendJId)
        values.put(ChatListDbColumns.FRIEND_NAME, friendName)
        values.put(ChatListDbColumns.LAST_MESSAGE, message)
        values.put(ChatListDbColumns.LOGIN_USER_JID, ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER))
        values.put(ChatListDbColumns.DATE, ts)
        if (direction == UserChatDbColumns.INCOMING)
            values.put(ChatListDbColumns.UNREAD_MSG_COUNT, 1)
        else
            values.put(ChatListDbColumns.UNREAD_MSG_COUNT, 0)


        this.context?.contentResolver?.insert(ChatProvider.CONTENT_URI_USER_LIST, values)

    }

    private fun getValueFromJSON(jo: JSONObject, tag: String): String {
        var keyValue = ""

        if (jo.has(tag)) {
            try {
                keyValue = jo.getString(tag)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        return keyValue
    }

    //
    fun disconnectXmpp(tag: String) {

        Log.i(TAG, "----call disconnecting XMPP:$tag")
        if (connection != null) {
            //            ProviderManager.removeIQProvider("ping", "urn:xmpp:ping");
            sendOfflinePresence(currentXmppResource)

            connection!!.removeAsyncStanzaListener(this@XmppConnection)
            connection!!.removeConnectionListener(this@XmppConnection)
            connection!!.disconnect()
            connection = null
            isConnecting = false
        }
    }

    /**Any incoming packet received in processPacket() function*/
    @Throws(NotConnectedException::class)
    override fun processPacket(packet: Stanza) {
        Log.d(TAG, "----Packet:" + packet.toXML())

        if (packet is Message) {

            val body = packet.body
            val subject = packet.subject
            val fromJID = getJabberID(packet.from)
            // Message.Type msgType = message.getType();
            if (packet.type == Message.Type.chat) {
                if (subject.equals(AppConstants.TYPE_USER_CHAT, ignoreCase = true)) {
                    try {
                        //temporary subject removed
                        addChatMessageToDB(
                            ChatTableColumns.INCOMING, fromJID,
                            body, subject,
                            System.currentTimeMillis(),
                            packet.stanzaId
                        )
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            } else if (packet.type == Message.Type.error) {
                Log.e(TAG, "StanzaTypeFilter receive message:" + packet.body)
                // error message
            }

        }
    }

    private fun getJabberID(from: String): String {
        try {
            val res = from.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            return res[0].toLowerCase()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return from
    }

    override fun connected(mconnection: XMPPConnection) {
        Log.i(TAG, "connected to xmpp: " + mconnection.toString())
    }

    override fun authenticated(connection: XMPPConnection, resumed: Boolean) {
        Log.i(TAG, "connection authenticated")

    }

    override fun connectionClosed() {
        Log.i(TAG, "connection closed.")

        if (cd.isConnectedToInternet) {
            reConnect()
        }
    }

    override fun connectionClosedOnError(e: Exception) {
        Log.e(TAG, "connection closed on error:" + e.stackTrace)

        if (isConflictError(e.message)) {
            return
        }

        Log.i(TAG, "reconnect after closed on error")
        reConnect()
    }

    override fun reconnectionSuccessful() {
        Log.i(TAG, "XMPP Reconnected successfully")
    }

    override fun reconnectingIn(seconds: Int) {
        Log.i(TAG, "XMPP Reconnecting in")
    }

    override fun reconnectionFailed(e: Exception) {
        Log.i(TAG, "XMPP Reconnecting failed: " + e.message)
        //reconnect();
    }


}
