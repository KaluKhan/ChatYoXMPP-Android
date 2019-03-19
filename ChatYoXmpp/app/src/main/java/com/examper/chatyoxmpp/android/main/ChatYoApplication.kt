package com.examper.chatyoxmpp.android.main

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.design.widget.Snackbar
import android.view.View


class ChatYoApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        myApplication = ChatYoApplication()
        sp = applicationContext!!.getSharedPreferences(CHATYOXMPP, 0)
    }

    companion object {
        private val CHATYOXMPP = "chatyoxmpp_prefs"
        lateinit var sp: SharedPreferences
        val TAG = ChatYoApplication::class.java.simpleName
        var checkForeground = false
        var ctViewed = false
        var frJid = ""
        lateinit var activity: Activity

        //shared Preference methods starts here

        fun getStringPrefs(type: String): String? {
            return sp.getString(type, "")
        }


        fun saveStringPrefs(type: String, value: String) {

            val editor = sp.edit()

            editor.putString(type, value)

            editor.apply()
        }

        fun showSnack(context: Context, error: String, parentView: View) {
            val snackBar = Snackbar
                .make(parentView, error, Snackbar.LENGTH_LONG)
                .setAction(context.resources.getString(R.string.close), View.OnClickListener { })

            snackBar.show()
        }

        fun logOutMe(context: Context) {
            val sp = context.getSharedPreferences(
                CHATYOXMPP, 0
            )
            val editor = sp.edit()
            editor.clear().apply()
        }

        //SharedPrefs methods ends here

        fun getResource(): String {
            return "ChatYoXmpp_" + "_V_" + BuildConfig.VERSION_CODE + "_" + android.os.Build.VERSION.SDK_INT + "_" + System.currentTimeMillis()
        }

        ///Utility methods
        fun setIsForeground(flag: Boolean) {
            this.checkForeground = flag
        }

        fun isAppForeground(): Boolean {
            return checkForeground
        }


        fun isChatViewed(): Boolean {
            return ctViewed
        }

        fun setChatViewed(chatViewedStatus: Boolean) {
            ctViewed = chatViewedStatus
        }

        fun getCurrentActivity(): Activity {
            return activity
        }

        fun setCurrentActivity(currentActivity: Activity) {
            this.activity = currentActivity
        }

        fun getCurrentChatFriendId(): String {
            return frJid
        }

        fun setCurrentChatFriendId(friendJid: String) {
            this.frJid = friendJid
        }


    }

    private lateinit var myApplication: ChatYoApplication
    val instance: ChatYoApplication
        get() {
            myApplication = ChatYoApplication()
            return myApplication
        }


}
