package com.examper.chatyoxmpp.android.main.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector(private val _context: Context?) {

    /**
     * Checking for active internet providers
     */
    val isConnectedToInternet: Boolean
        get() {
            val cm = _context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
            return activeNetwork?.isConnected == true
        }
}
