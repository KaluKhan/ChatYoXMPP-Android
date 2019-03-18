package com.examper.chatyoxmpp.android.main.view.adapter


import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import android.widget.TextView
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.ChatListDbColumns

class ChatUserListAdapter(
   private val context: Context, cursor: Cursor?, from: Array<String>,
    to: IntArray
) : SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, from, to) {




    override fun getItem(position: Int): Cursor {
        val cursor = this.cursor
        cursor.moveToPosition(position)
        return cursor
    }

    override fun getView(position: Int, convertView_: View?, parent: ViewGroup): View {
        var convertView = convertView_
        val holder: VHolder
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView != null) {
            holder = convertView.tag as VHolder
        } else {
            convertView = inflater.inflate(R.layout.item_chat_user_list, parent, false)
            holder = VHolder()
            convertView!!.tag = holder
            holder.friendName = convertView.findViewById(R.id.friendName)
            holder.lastMessage = convertView.findViewById(R.id.lastMessage)
            holder.unreadCount = convertView.findViewById(R.id.unreadCount)
        }
        val cursor = this.cursor
        cursor.moveToPosition(position)

        val unreadMsgCount = cursor.getInt(cursor.getColumnIndex(ChatListDbColumns.UNREAD_MSG_COUNT))
        if (unreadMsgCount > 0) {
            holder.unreadCount!!.visibility = View.VISIBLE
            holder.unreadCount!!.text = "" + unreadMsgCount
        } else {
            holder.unreadCount!!.visibility = View.GONE
        }
        holder.friendName!!.text = cursor.getString(cursor.getColumnIndex(ChatListDbColumns.FRIEND_NAME))
        holder.lastMessage!!.text = cursor.getString(cursor.getColumnIndex(ChatListDbColumns.LAST_MESSAGE))

        return convertView
    }

    internal class VHolder {
        var friendName: TextView? = null
        var lastMessage: TextView? = null
        var unreadCount: TextView? = null
    }


}
