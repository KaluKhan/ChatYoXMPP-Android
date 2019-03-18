package com.examper.chatyoxmpp.android.main.view.activity

import android.animation.ObjectAnimator
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Toast
import com.examper.chatyoxmpp.android.main.ChatScreenListener
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.presenter.ChatPresenter
import com.examper.chatyoxmpp.android.main.utils.ConnectionDetector
import com.examper.chatyoxmpp.android.main.view.adapter.UserChatWindowAdapter
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tool_bar.*

/***@UserChatActivity is chat screen with other user(here mentioning as 'friend') */
class UserChatActivity : AppCompatActivity(), ChatScreenListener {

    private var friendJId: String? = ""
    private var friendName: String? = ""
    private var cd: ConnectionDetector? = null
    private var viewWidth = 0
    private var sendBtn: Button? = null
    private val projectionTo = intArrayOf(R.id.friendName)
    private lateinit var presenter: ChatPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val toolbar = findViewById<View>(R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)
        sendBtn = findViewById<View>(R.id.send_btn) as Button
        val actionbar = supportActionBar
        if (actionbar != null) {
            actionbar.title = ""
            actionbar.setDisplayHomeAsUpEnabled(true)
            // actionbar.setHomeAsUpIndicator(R.drawable.icn_back);
        }
        ChatYoApplication.setChatViewed(true)
        viewWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
        cd = ConnectionDetector(this)
        val b = intent.extras
        if (b != null) {
            friendName = b.getString(AppConstants.BUNDLE_KEY_FRIEND_NAME)
            friendJId = b.getString(AppConstants.BUNDLE_KEY_FRIEND_JID)
        }
        toolbar_title.text = friendName
        chat_list.cacheColorHint = Color.TRANSPARENT
        chat_list.divider = null

        presenter = ChatPresenter(this, this,friendJId,friendName)

        sendBtn!!.setOnClickListener(View.OnClickListener {
            val textMessage = edtMessage!!.text.toString()
            if (TextUtils.isEmpty(textMessage)) {
                Toast.makeText(this@UserChatActivity, R.string.error_message_empty, Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (!cd!!.isConnectedToInternet) {
                //show progress bar here
                connectionWaitView!!.visibility = View.VISIBLE
                sendBtn!!.isEnabled = false
                showSettings(connectionWaitView, viewWidth)
                return@OnClickListener
            }
            presenter.onMessageSend(textMessage)
            edtMessage!!.setText("")
        })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menu.findItem(R.id.action_logout).isVisible = false
        menu.findItem(R.id.action_send_message).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume(friendJId)
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()

    }

    override fun onStop() {
        super.onStop()
        presenter.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    // show from bottom views animation
    private fun showSettings(v: LinearLayout?, dip: Int) {
        // hiddenView.setVisibility(View.VISIBLE);
        val animation = ObjectAnimator.ofFloat(v, "translationY", dip.toFloat(), 0f)
        animation.duration = 500
        animation.start()
    }

    private fun hideSettings(v: LinearLayout?, dip: Int) {
        val animation = ObjectAnimator.ofFloat(
            v, "translationY", 0f,
            dip.toFloat()
        )
        animation.duration = 500
        animation.start()
    }

    override fun setData(c: Cursor?) {
        val chatWindowAdapter = UserChatWindowAdapter(
            this@UserChatActivity, c,
            AppConstants.CHAT_PROJECTION_FROM,
            projectionTo
        )
        chat_list.adapter = chatWindowAdapter
    }

    override fun onBroadCastReceive() {
        sendBtn!!.isEnabled = true
        hideSettings(connectionWaitView, viewWidth)
    }
}
