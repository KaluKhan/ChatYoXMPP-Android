package com.examper.chatyoxmpp.android.main

import android.database.Cursor

interface ChatScreenListener {
    fun setData(c: Cursor?)
    fun onBroadCastReceive()
}