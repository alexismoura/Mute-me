package com.dragsystem.muteme.receivers


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony.Sms
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner

/** Handles updating databases and states when a SMS message is sent. */
class SmsStatusSentReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        TODO("Not yet implemented")
    }


}
