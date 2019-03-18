package com.examper.chatyoxmpp.android.main.presenter

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.text.TextUtils
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.MainChatListListener
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.constants.ChatListDbColumns
import com.examper.chatyoxmpp.android.main.services.XMPPService
import com.examper.chatyoxmpp.android.main.view.activity.LoginActivity
import com.examper.chatyoxmpp.android.main.view.activity.UserChatActivity
import com.examper.chatyoxmpp.android.main.xmpp.ChatProvider

class MainListPresenter(private var mContext: Context, private var listener: MainChatListListener?) {
    init {
        if (listener != null) {
            //call method here to get on init
            initChatListCursor()
        }
    }

    fun onDestroy() {
        listener = null
        ChatYoApplication.setIsForeground(false)
    }

    fun onResume() {
        ChatYoApplication.setCurrentActivity(mContext as Activity)
    }

    fun onStart() {
        ChatYoApplication.setIsForeground(true)
    }

    private fun isMyServiceRunning(serviceName: Class<*>): Boolean {
        val manager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager
            .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun initChatService(jabberId: String?, password: String?) {
        if (!isMyServiceRunning(XMPPService::class.java) && !TextUtils.isEmpty(jabberId)
            && !TextUtils.isEmpty(password)
        ) {
            mContext.startService(Intent(mContext, XMPPService::class.java))
        }
    }

    private fun initChatListCursor() {
        val selection = (ChatListDbColumns.LOGIN_USER_JID + "='"
                + ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER) + "'")

        val cursor = mContext.contentResolver?.query(
            ChatProvider.CONTENT_URI_USER_LIST,
            AppConstants.CHAT_LIST_PROJECTION_FROM, selection, null, null
        )

        listener?.setData(cursor)
    }

    fun onChatItemClick(c: Cursor) {
        openChatWindow(c.getString(c.getColumnIndex(ChatListDbColumns.FRIEND_JID)),
            c.getString(c.getColumnIndex(ChatListDbColumns.FRIEND_NAME)))
    }

    fun openChatWindow(friendJId: String?, friendName: String?) {
        val intent = Intent(mContext, UserChatActivity::class.java).apply {
            putExtra(AppConstants.BUNDLE_KEY_FRIEND_JID, friendJId)
            putExtra(AppConstants.BUNDLE_KEY_FRIEND_NAME, friendName)
        }
        mContext.startActivity(intent)
    }

    fun logout() {
        //clear data
        ChatYoApplication.logOutMe(mContext)
        // Stop services when logging out
        if (isMyServiceRunning(XMPPService::class.java)) {
            mContext.stopService(Intent(mContext, XMPPService::class.java))
        }
        mContext.startActivity(Intent(mContext, LoginActivity::class.java))
        (mContext as Activity).finish()
    }
}