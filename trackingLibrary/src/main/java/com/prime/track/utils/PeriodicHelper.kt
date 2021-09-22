package com.prime.track.utils

import android.content.Context
import android.os.Build
import androidx.work.*
import com.prime.track.modules.Scopes
import com.prime.track.services.OneTimeWork
import com.prime.track.services.PeriodicWork
import com.prime.track.storages.PrefStorage
import io.nlopez.smartlocation.SmartLocation

import java.util.concurrent.TimeUnit

class PeriodicHelper(val context: Context) {

    fun stopLog(){
        SmartLocation.with(context).location().stop()
        WorkManager.getInstance().cancelAllWork()
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