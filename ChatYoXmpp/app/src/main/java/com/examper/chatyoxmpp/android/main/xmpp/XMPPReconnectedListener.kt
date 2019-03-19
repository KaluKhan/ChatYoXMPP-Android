package com.examper.chatyoxmpp.android.main.xmpp

interface XMPPReconnectedListener {
    fun onXMPPReConnected()
    fun onXMPPConnectionError(error:String?)
}
