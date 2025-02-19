package com.udacity.project4.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

fun getActivity(context: Context?, id: Int, intent: Intent?, flag: Int): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_MUTABLE or flag)
    } else {
        PendingIntent.getActivity(context, id, intent, flag)
    }
}

fun getBroadcast(context: Context?, id: Int, intent: Intent?, flag: Int): PendingIntent {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.getBroadcast(context, id, intent!!, PendingIntent.FLAG_MUTABLE or flag)
    } else {
        PendingIntent.getBroadcast(context, id, intent!!, flag)
    }
}