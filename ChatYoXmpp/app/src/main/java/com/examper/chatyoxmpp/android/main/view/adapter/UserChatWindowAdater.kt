package com.examper.chatyoxmpp.android.main.view.adapter

import android.content.Context
import android.database.Cursor
import android.support.v4.widget.SimpleCursorAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.UserChatDbColumns
import java.util.*

class UserChatWindowAdapter(private val context: Context, cursor: Cursor?, from: Array<String>, to: IntArray) :
    SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, cursor, from, to) {


    override fun getView(position: Int, convertVw: View?, parent: ViewGroup): View {
        var convertView = convertVw
        val holder:ViewHolder
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_chatlist, parent, false)
            holder = ViewHolder()
            convertView!!.tag = holder
            holder.txtNameMe = convertView.findViewById<View>(R.id.txtNameMe) as TextView
            holder.txtNameOther = convertView.findViewById<View>(R.id.txtNameOther) as TextView
            holder.message = convertView.findViewById<View>(R.id.message) as TextView
            holder.fromotherlayout = convertView.findViewById<View>(R.id.fromotherlayout) as LinearLayout
            holder.frommelayout = convertView.findViewById<View>(R.id.frommelayout) as LinearLayout
            holder.messageOther = convertView.findViewById<View>(R.id.message_other) as TextView
            holder.dateTx = convertView.findViewById<View>(R.id.date_) as TextView
            holder.dateOtherTx = convertView.findViewById<View>(R.id.date_other) as TextView
        } else {
            holder = convertView.tag as ViewHolder
        }

        val cursor = this.cursor
        cursor.moveToPosition(position)
        val senderName = cursor.getString(cursor.getColumnIndex(UserChatDbColumns.SENDER_NAME))
        val message = cursor.getString(cursor.getColumnIndex(UserChatDbColumns.BODY))
        val date = cursor.getLong(cursor.getColumnIndex(UserChatDbColumns.DATE))
        val direction = cursor.getInt(cursor.getColumnIndex(UserChatDbColumns.DIRECTION))
        if (direction == 0) {
            holder.fromotherlayout!!.visibility = View.VISIBLE
            holder.frommelayout!!.visibility = View.GONE
            holder.txtNameOther!!.text = senderName
            holder.messageOther!!.text = message
            holder.dateOtherTx!!.text = getTimeAgo(date, context)

        } else {
            holder.frommelayout!!.visibility = View.VISIBLE
            holder.fromotherlayout!!.visibility = View.GONE
            holder.txtNameMe!!.text = senderName
            holder.message!!.text = message
            holder.dateTx!!.text = getTimeAgo(date, context)

        }

        return convertView
    }

    inner class ViewHolder {
        internal var message: TextView? = null
        internal var dateTx: TextView? = null
        internal var messageOther: TextView? = null
        internal var dateOtherTx: TextView? = null
        internal var txtNameMe: TextView? = null
        internal var txtNameOther: TextView? = null
        internal var fromotherlayout: LinearLayout? = null
        internal var frommelayout: LinearLayout? = null
    }

    private fun getTimeAgo(time: Long, context: Context): String {
        var timeMillis = time
        if (timeMillis < 1000000000000L)
        // if timestamp given in seconds, convert to millis
            timeMillis *= 1000

        val now = Date().time// getCurrentTime(context);
        if (timeMillis > now || timeMillis <= 0)
            return ""

        val res = context.resources
        val timeDifference = now - timeMillis
        return if (timeDifference < A_MINUTE)
            res.getString(R.string.just_now)
        else if (timeDifference < 50 * A_MINUTE)
            res.getString(
                R.string.time_ago, res.getQuantityString(
                    R.plurals.minutes, timeDifference.toInt() / A_MINUTE,
                    timeDifference.toInt() / A_MINUTE
                )
            )
        else if (timeDifference < 24 * AN_HOUR)
            res.getString(
                R.string.time_ago, res.getQuantityString(
                    R.plurals.hours, timeDifference.toInt() / AN_HOUR,
                    timeDifference.toInt() / AN_HOUR
                )
            )
        else if (timeDifference < 48 * AN_HOUR)
            res.getString(R.string.yesterday)
        else
            res.getString(
                R.string.time_ago, res.getQuantityString(
                    R.plurals.days, timeDifference.toInt() / A_DAY,
                    timeDifference.toInt() / A_DAY
                )
            )
    }

    companion object {

        /**
         * One second (in milliseconds)
         */
        private const val A_SECOND = 1000
        /**
         * One minute (in milliseconds)
         */
        private const val A_MINUTE = 60 * A_SECOND
        /**
         * One hour (in milliseconds)
         */
        private const val AN_HOUR = 60 * A_MINUTE
        /**
         * One day (in milliseconds)
         */
        private const val A_DAY = 24 * AN_HOUR
    }

}
