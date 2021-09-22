package com.app.workmanagerapp.notifications

import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


import com.prime.track.MainClass


class FirebaseMessaging : FirebaseMessagingService() {



    companion object {
        private const val FIREBASE_TYPE = "type"
        private const val FIREBASE_TYPE_VALUE = "1"

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.data.isNotEmpty()) {
            Log.d("Message", "onNotification>> "+remoteMessage.data)
            val mainClass = MainClass(this)
            mainClass.onNotification(remoteMessage.data)


        }

    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

    }
}