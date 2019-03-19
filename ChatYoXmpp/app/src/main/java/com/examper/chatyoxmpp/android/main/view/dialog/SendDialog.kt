package com.examper.chatyoxmpp.android.main.view.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.examper.chatyoxmpp.android.main.AddJabberListener
import com.examper.chatyoxmpp.android.main.R
import kotlinx.android.synthetic.main.dialog_send_message.*

class SendDialog(private val mContext: Context, private val mAddListener: AddJabberListener) : Dialog(mContext, R.style.Theme_Custom) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_send_message)
        sendBtn.setOnClickListener {
            hideKeyboard()
            val friendJId: String = editTextValue(fJabberEt)
            val friendName: String = editTextValue(fNameEt)
            if (TextUtils.isEmpty(friendJId)) {
               Toast.makeText(mContext,"Please enter receiver's jabber id!.",Toast.LENGTH_SHORT).show()
            }else if(TextUtils.isEmpty(friendName)){
                Toast.makeText(mContext,"Please enter receiver's Name!.",Toast.LENGTH_SHORT).show()
            } else {
                dismiss()
                mAddListener.onAddJabber(friendJId,friendName)
            }
        }
    }

    private fun hideKeyboard() {
        try {
            val imm = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(fJabberEt!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun editTextValue(editView: EditText?): String {
        var s: String = editView?.text.toString()
        if (TextUtils.isEmpty(s)) {
            s = ""
        }
        return s
    }
}
