package com.examper.chatyoxmpp.android.main.view.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.widget.EditText
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val userJID = ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER)
        if (!TextUtils.isEmpty(userJID)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        loginBt.setOnClickListener {
            val arr: Array<String> = data()
            if (checkInputAvailable(arr)) {
                val intent = Intent(this, MainActivity::class.java).apply {

                    /*need to move in connection*/
                    ChatYoApplication.saveStringPrefs(AppConstants.KEY_PREFS_NAME, arr[0])
                    ChatYoApplication.saveStringPrefs(AppConstants.KEY_PREFS_HOST, arr[1])
                    ChatYoApplication.saveStringPrefs(AppConstants.KEY_PREFS_JABBER, arr[2])
                    ChatYoApplication.saveStringPrefs(AppConstants.KEY_PREFS_PASSWORD, arr[3])
                    putExtra(AppConstants.BUNDLE_KEY_IS_NOTIF_CLICK, false)
                    putExtra(AppConstants.BUNDLE_KEY_NAME, arr[0])
                    putExtra(AppConstants.BUNDLE_KEY_HOST, arr[1])
                    putExtra(AppConstants.BUNDLE_KEY_JABBER, arr[2])
                    putExtra(AppConstants.BUNDLE_KEY_PASSWORD, arr[3])
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun data(): Array<String> {

        return arrayOf(
            getEditedText(nameEt),
            getEditedText(hostEt),
            getEditedText(jabberIdEt),
            getEditedText(passwordEt)
        )

    }

    private fun getEditedText(edt: EditText): String {
        var s = edt.text.toString()
        if (TextUtils.isEmpty(s)) {
            s = ""
        }
        return s
    }

    private fun checkInputAvailable(arr: Array<String>): Boolean {
        val name = arr[0]
        val host = arr[1]
        val jabberId = arr[2]
        val password = arr[3]
        if (TextUtils.isEmpty(name)) {
            ChatYoApplication.showSnack(this, this.resources.getString(R.string.errorNameEmpty), loginView)
            return false
        }

        if (TextUtils.isEmpty(host)) {
            ChatYoApplication.showSnack(this, this.resources.getString(R.string.errorHostEmpty), loginView)
            return false
        }
        if (TextUtils.isEmpty(jabberId)) {
            ChatYoApplication.showSnack(this, this.resources.getString(R.string.errorJabberIdEmpty), loginView)
            return false
        }
        if (TextUtils.isEmpty(password)) {
            ChatYoApplication.showSnack(this, this.resources.getString(R.string.errorPasswordEmpty), loginView)
            return false
        }
        return true
    }
}
