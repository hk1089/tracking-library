package com.app.workmanagerapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.work.*
import com.app.workmanagerapp.*
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.services.OneTimeWork
import com.app.workmanagerapp.services.PeriodicWork
import com.app.workmanagerapp.storages.PrefStorage
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import toothpick.Toothpick
import java.util.concurrent.TimeUnit

class PeriodicHelper(val context: Context) {
    companion object{
        private val prefStorage: PrefStorage = Toothpick.openScope(Scopes.APP).getInstance(
            PrefStorage::class.java)
    }

    fun getToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e( "getInstanceId failed>> ${task.exception}")
                return@addOnCompleteListener
            } else {
                prefStorage.firebaseToken = task.result!!
                Timber.d( "Token>>> ${task.result}")
                TokenSendTask().sendToken(context, prefStorage.firebaseToken,
                    NOTIFICATION_START
                ){}
            }
        }
    }

    fun startLog() {
        val mWorkManager = WorkManager.getInstance()
        val mConstraints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Constraints.Builder()
                .setRequiresStorageNotLow(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        } else {
            Constraints.Builder()
                .setRequiresStorageNotLow(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        }
        val mPeriodicWorkRequest = PeriodicWorkRequest
            .Builder(PeriodicWork::class.java, 15, TimeUnit.MINUTES)
            .setConstraints(mConstraints)
            .addTag(WORK_TAG)
            .build()

        mWorkManager
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                mPeriodicWorkRequest
            )
        prefStorage.isWorkStart = true

    }

    fun startSingleLog() {
        WorkManager.getInstance().cancelUniqueWork(ONE_TIME_WORK_NAME)
        val mWorkManager = WorkManager.getInstance()
        val mConstraints = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Constraints.Builder()
                .setRequiresStorageNotLow(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        } else {
            Constraints.Builder()
                .setRequiresStorageNotLow(false)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
        }
        val mOneTimeWorkRequest = OneTimeWorkRequest
            .Builder(OneTimeWork::class.java)
            .setInitialDelay(1L, TimeUnit.MINUTES)
            .setConstraints(mConstraints)
            .addTag(ONE_TIME_WORK_TAG)
            .build()

        mWorkManager
            .enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                mOneTimeWorkRequest
            )

    }
}