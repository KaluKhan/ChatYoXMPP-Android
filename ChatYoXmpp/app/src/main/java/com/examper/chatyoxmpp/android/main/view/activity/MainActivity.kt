package com.examper.chatyoxmpp.android.main.view.activity

import android.database.Cursor
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import com.examper.chatyoxmpp.android.main.AddJabberListener
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.MainChatListListener
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.presenter.MainListPresenter
import com.examper.chatyoxmpp.android.main.view.adapter.ChatUserListAdapter
import com.examper.chatyoxmpp.android.main.view.dialog.SendDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.tool_bar.*

/**@MainActivity holding list of all users registered over your Openfire chat server.
Also you can create chat group using @MultiUserChat.
For more details about Openfire you can visit the official documentation page:
https://www.igniterealtime.org/
and the github repository: https://github.com/igniterealtime/Openfire
 * */
class MainActivity : AppCompatActivity(), MainChatListListener, AdapterView.OnItemClickListener, AddJabberListener {

    private lateinit var adapter: ChatUserListAdapter
    private val projectionTo = intArrayOf(R.id.friendName)
    private lateinit var presenter: MainListPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as android.support.v7.widget.Toolbar
        setSupportActionBar(toolbar)
        toolbar_title.setText(R.string.chat_list)
        if (supportActionBar != null)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
        presenter = MainListPresenter(this, this)

        val myName: String?
        val myJId: String?
        val password: String?
        val host: String?
        var isRedirectedFromNotifClick: Boolean? = false
        val b = intent.extras
        if (b != null) {
            myName = b.getString(AppConstants.BUNDLE_KEY_NAME)
            myJId = b.getString(AppConstants.BUNDLE_KEY_JABBER)
            password = b.getString(AppConstants.BUNDLE_KEY_PASSWORD)
            host = b.getString(AppConstants.BUNDLE_KEY_HOST)
            isRedirectedFromNotifClick = b.getBoolean(AppConstants.BUNDLE_KEY_IS_NOTIF_CLICK)
        }else{
            myJId = ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_JABBER)
            password=ChatYoApplication.getStringPrefs(AppConstants.KEY_PREFS_PASSWORD)
        }
        //init chat service only once after login.
        if (!isRedirectedFromNotifClick!!)
            presenter.initChatService(myJId, password)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        // menu.findItem(R.id.action_logout).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }else if(id == R.id.action_send_message){
            SendDialog(this,this).show()
        } else if (id == R.id.action_logout) {
            presenter.logout()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onStart() {
        super.onStart()
        presenter.onStart()
    }
    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }

    override fun setData(c: Cursor?) {
        adapter = ChatUserListAdapter(
            this@MainActivity, c, AppConstants.CHAT_LIST_PROJECTION_FROM,
            projectionTo
        )
        chatList.adapter = adapter
        chatList.onItemClickListener = this@MainActivity
    }

    override fun onResponseFailure(throwable: Throwable) {}

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        presenter.onChatItemClick(adapter.getItem(position))
    }
    override fun onAddJabber(jabber: String, fName: String) {
        //init chat by other user's jabber id
        presenter.openChatWindow(jabber,fName)
    }
}
