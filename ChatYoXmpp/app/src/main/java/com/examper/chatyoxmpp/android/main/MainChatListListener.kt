package com.examper.chatyoxmpp.android.main

import android.database.Cursor

interface MainChatListListener {
    fun setData(c: Cursor?)
    fun onResponseFailure(throwable: Throwable)
}