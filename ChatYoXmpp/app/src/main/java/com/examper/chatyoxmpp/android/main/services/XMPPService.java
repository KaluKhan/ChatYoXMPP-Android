package com.examper.chatyoxmpp.android.main.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.examper.chatyoxmpp.android.main.ChatYoApplication;
import com.examper.chatyoxmpp.android.main.constants.AppConstants;
import com.examper.chatyoxmpp.android.main.xmpp.NotificationService;
import com.examper.chatyoxmpp.android.main.xmpp.XMPPReconnectedListener;
import com.examper.chatyoxmpp.android.main.xmpp.XmppConnection;
import org.jetbrains.annotations.Nullable;


public class XMPPService extends Service implements XMPPReconnectedListener {

    public static XmppConnection xmppConnection = null;
    private IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("XmppConnection", "---xmpp service oncreate");
        String jabberId = ChatYoApplication.Companion.getStringPrefs(AppConstants.KEY_PREFS_JABBER);
        String password = ChatYoApplication.Companion.getStringPrefs(AppConstants.KEY_PREFS_PASSWORD);

        if (!TextUtils.isEmpty(jabberId))
            xmppConnection = new XmppConnection(this, jabberId, password, XMPPService.this);

    }


    @Override
    public void onXMPPReConnected() {
        try {
            Intent bIntent = new Intent("custom-event-name");
            LocalBroadcastManager.getInstance(XMPPService.this).sendBroadcast(bIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onXMPPConnectionError(@Nullable String error) {

    }

    public class LocalBinder extends Binder {
        public XMPPService getServerInstance() {
            return XMPPService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static void ReConnectXmpp() {
        if (xmppConnection != null && !xmppConnection.isConnected()) {
            xmppConnection.reConnect();
        }
    }

    public NotificationService getNotificationService() {
        if (xmppConnection != null) {
            return xmppConnection.getNotificationService();
        } else {
            String jabberId = ChatYoApplication.Companion.getStringPrefs(AppConstants.KEY_PREFS_JABBER);
            String password = ChatYoApplication.Companion.getStringPrefs(AppConstants.KEY_PREFS_PASSWORD);
            xmppConnection = new XmppConnection(this, jabberId, password, XMPPService.this);
            return xmppConnection.getNotificationService();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("XmppConnection", "---xmpp service onStartCommand");

        if (intent != null ) {
            if (xmppConnection != null){

            if ("disconnect".equals(intent.getAction())) {

                return START_STICKY;
            } else if ("reconnect".equals(intent.getAction())) {
                // reset reconnection timeout
                Log.d("XmppConnection", "---xmpp service restarting connection");
                ReConnectXmpp();
                return START_STICKY;
            } else if ("ping".equals(intent.getAction())) {
                return START_STICKY;
            }
            }//else {
               //data=(String) intent.getExtras().get("data");
            //}
        }
        doConnect();
        return START_STICKY;
    }

    private void doConnect() {
        if (xmppConnection != null) {
            xmppConnection.doConnect();
        }
    }

    public void sendMessage(String ToJid, String Subject, String msg_body) {
        if (xmppConnection != null) {
            xmppConnection.sendMessage(ToJid, Subject, msg_body);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("XmppConnection", "---xmpp service onDestroy");

        if (xmppConnection != null) {
            xmppConnection.disconnectXmpp("Generic onStop");
            Log.d("XmppConnection", "true Condition step 2..");
            xmppConnection = null;
        }
    }
}
