package com.examper.chatyoxmpp.android.main.xmpp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.os.Vibrator
import android.support.v4.app.NotificationCompat
import com.examper.chatyoxmpp.android.main.ChatYoApplication
import com.examper.chatyoxmpp.android.main.R
import com.examper.chatyoxmpp.android.main.constants.AppConstants
import com.examper.chatyoxmpp.android.main.view.activity.MainActivity
import com.examper.chatyoxmpp.android.main.view.activity.UserChatActivity
import java.util.*


class NotificationService(context: Context?) {

    private var mNotificationMGR: NotificationManager? = null
    private val mVibrator: Vibrator
    var mWakeLock: WakeLock
    private var context: Context? = null
    private val notificationCountMap = HashMap<String, Int>(2)
    private val notificationIdMap = HashMap<String, Int>(2)
    private var lastNotificationId = 2

    init {
        if (context != null) {
            this.context = context
        }
        mVibrator = this.context!!.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mWakeLock = (this.context!!.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, context!!.resources.getString(R.string.app_name))
        addNotificationMGR()
    }


    private fun addNotificationMGR() {
        mNotificationMGR = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

     fun notifyClient(fromJid: String, senderName: String, message: String, showNotification: Boolean) {

        try {

            mWakeLock.acquire(1000)
            if (!showNotification) {
                try {
                    /* Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    Ringtone r = RingtoneManager.getRingtone(context, notification);
                    r.play();*/
                    val mp = MediaPlayer.create(context, R.raw.sound)
                    mp.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return
            }
            // }

            val notifyId: Int
            if (notificationIdMap.containsKey(fromJid)) {
                notifyId = notificationIdMap[fromJid]!!
            } else {
                lastNotificationId++
                notifyId = lastNotificationId
                notificationIdMap[fromJid] = Integer.valueOf(notifyId)
            }

            mVibrator.vibrate(400)

            mWakeLock.release()
            generateNotification(senderName, fromJid, message, notifyId)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }


    fun resetNotificationCounter(userJid: String) {
        notificationCountMap.remove(userJid)
    }

    fun clearNotification(Jid: String) {
        val notifyId: Int
        if (notificationIdMap.containsKey(Jid)) {
            notifyId = notificationIdMap[Jid]!!
            mNotificationMGR!!.cancel(notifyId)
        }
    }

    private fun generateNotification(sender: String, jId: String, message: String, notId: Int) {
        var intent = Intent()
        //when app in background and closed
        if (!ChatYoApplication.isAppForeground()) {
            ChatYoApplication.setChatViewed(false)
            intent = Intent(context, MainActivity::class.java)

            intent.putExtra(AppConstants.BUNDLE_KEY_IS_NOTIF_CLICK, true)
            intent.putExtra(AppConstants.BUNDLE_KEY_JABBER, jId)

            /*
            intent.putExtra(AppConstants.BUNDLE_KEY_NAME, sender)
            */

        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            if (ChatYoApplication.getCurrentActivity() is MainActivity) {
                intent = Intent(context, UserChatActivity::class.java)
                intent.putExtra(AppConstants.BUNDLE_KEY_NAME, sender)
                intent.putExtra(AppConstants.BUNDLE_KEY_JABBER, jId)
            } //else {
            //In this else case, user is already on UserChatActivity
            /*
                ApplicationUtils.getInstance().setChatViewed(false);
                intent = new Intent(context, );
                intent.putExtra(AppConstants.BUNDLE_KEY_NAME, senderName);
                intent.putExtra(AppConstants.BUNDLE_KEY_JABBER, senderJId);*/

            //}

        }//
        /* String details[] = userName.split("#");
//            intent.putExtra("userjid", jid);
        intent.putExtra("carname", details[0]);
        intent.putExtra("register_no", details[1]);
        userName = details[0];*/


        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP


        val notificationManager = context!!
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mBuilder = NotificationCompat.Builder(
            context
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentTitle(sender) //+ "@" + groupName)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
                //new NotificationCompat.BigPictureStyle().bigPicture(result)
            )
            .setContentText(message)

        val pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        //PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent. | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pIntent)

        val notification = mBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        notification.defaults = notification.defaults or Notification.DEFAULT_SOUND
        notification.defaults = notification.defaults or Notification.DEFAULT_LIGHTS
        notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE

        notificationManager.notify(notId, notification)


        val pm = context!!.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isScreenOn
        if (!isScreenOn) {

            val wl = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK
                        or PowerManager.ACQUIRE_CAUSES_WAKEUP
                        or PowerManager.ON_AFTER_RELEASE, "ChatYo:MyLock"
            )

            wl.acquire(10000)
            wl.release()
        }

    }
}
