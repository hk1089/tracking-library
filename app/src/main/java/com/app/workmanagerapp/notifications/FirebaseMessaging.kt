package com.app.workmanagerapp.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import androidx.work.WorkInfo

import androidx.work.WorkManager
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.storages.PrefStorage
import com.app.workmanagerapp.utils.*
import toothpick.Toothpick
import java.util.concurrent.ExecutionException


class FirebaseMessaging : FirebaseMessagingService() {

    private lateinit var periodicHelper: PeriodicHelper
    private lateinit var tokenSendTask: TokenSendTask

    companion object {
        private const val FIREBASE_TYPE = "type"
        private const val FIREBASE_TYPE_VALUE = "1"
        private val prefStorage: PrefStorage = Toothpick.openScope(Scopes.APP).getInstance(
            PrefStorage::class.java
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        periodicHelper = PeriodicHelper(applicationContext)
        tokenSendTask = TokenSendTask()
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("oneWorkIsEnable>> ${getStateOfOneWork()}")
            Timber.d("periodicIsEnable>> ${getStateOfWork()}")
            Timber.d("Message data payload: %s", remoteMessage.data)

            val type = remoteMessage.data[FIREBASE_TYPE]
            if (type == FIREBASE_TYPE_VALUE && prefStorage.isWorkStart) {

                Timber.d("IsEnable>> ${getStateOfWork()}")
                if (getStateOfWork() != WorkInfo.State.ENQUEUED && getStateOfWork() != WorkInfo.State.RUNNING) {
                    periodicHelper.startLog()
                    Timber.d("IsEnable>> service started")
                    tokenSendTask.sendGPS(
                        this,
                        prefStorage.firebaseToken,
                        STOP_STATUS, "", "Notification"
                    )


                } else {
                    Timber.d("IsEnable>> service already working")
                    tokenSendTask.sendGPS(
                        this,
                        prefStorage.firebaseToken,
                        RUNNING_STATUS, "", "Notification"
                    )


                }

            }
        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Timber.d("Message Notification Body: ${remoteMessage.notification!!.body}")
        }
    }

    private fun getStateOfWork(): WorkInfo.State {
        return try {
            if (WorkManager.getInstance().getWorkInfosForUniqueWork(WORK_NAME)
                    .get().size > 0
            ) {
                WorkManager.getInstance().getWorkInfosForUniqueWork(WORK_NAME)
                    .get()[0].state
            } else {
                WorkInfo.State.CANCELLED
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
            WorkInfo.State.CANCELLED
        } catch (e: InterruptedException) {
            e.printStackTrace()
            WorkInfo.State.CANCELLED
        }
    }

    private fun getStateOfOneWork(): WorkInfo.State {
        return try {
            if (WorkManager.getInstance().getWorkInfosForUniqueWork(ONE_TIME_WORK_NAME)
                    .get().size > 0
            ) {
                WorkManager.getInstance().getWorkInfosForUniqueWork(ONE_TIME_WORK_NAME)
                    .get()[0].state
            } else {
                WorkInfo.State.CANCELLED
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
            WorkInfo.State.CANCELLED
        } catch (e: InterruptedException) {
            e.printStackTrace()
            WorkInfo.State.CANCELLED
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Timber.d("Token>> $p0")
    }
}