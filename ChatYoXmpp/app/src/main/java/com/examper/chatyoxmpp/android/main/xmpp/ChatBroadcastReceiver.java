package com.examper.chatyoxmpp.android.main.xmpp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.examper.chatyoxmpp.android.main.services.XMPPService;

/***
 * @ChatBroadcastReceiver is used to detect internet connection status.
 * The @XMPPService reConnects when device gets internet connection*/
public class ChatBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = "YaximBroadcastReceiver";
    private static int networkType = -1;
    private static boolean connectedWithNetwork = false;



    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent.getAction());

        try {
            Intent xmppServiceIntent = new Intent(context, XMPPService.class);

            if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
                Log.d(TAG, "System shutdown, stopping yaxim.");
                context.stopService(xmppServiceIntent);
            } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

                // // refresh DNS servers from android prefs
                // org.xbill.DNS.ResolverConfig.refresh();
                // org.xbill.DNS.Lookup.refreshDefault();

                // prepare intent

                // there are three possible situations here: disconnect,
                // reconnect, connection change
                ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                boolean isConnected = (networkInfo != null) && (networkInfo.isConnected() == true);
                boolean wasConnected = (networkType != -1);

                if (connectedWithNetwork && isConnected)
                    return;

                connectedWithNetwork = isConnected;

                if (wasConnected && !isConnected) {
                    Log.d(TAG, "we got disconnected");
                    networkType = -1;

                } else if (isConnected && (networkInfo.getType() != networkType)) {
                    Log.d(TAG, "we got (re)connected: " + networkInfo.toString());
                    networkType = networkInfo.getType();
                    xmppServiceIntent.setAction("reconnect");
                    context.startService(xmppServiceIntent);
                } else if (isConnected && (networkInfo.getType() == networkType)) {

                } else
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
