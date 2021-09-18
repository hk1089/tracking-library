package com.app.workmanagerapp.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.storages.PrefStorage
import com.app.workmanagerapp.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import toothpick.Toothpick

class PeriodicWork(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    @SuppressLint("CommitPrefEdits", "MissingPermission")
    private lateinit var periodicHelper: PeriodicHelper
    override fun doWork(): Result {
        return try {
            executeTask()
            periodicHelper = PeriodicHelper(context)
            periodicHelper.startSingleLog()
            Timber.e("PeriodicWork: isStopped-------> $isStopped")
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()

        }

    }

    private fun executeTask() {
        val prefStorage: PrefStorage =
            Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)
        if (prefStorage.isWorkStart)
            TokenSendTask().sendGPS(context, prefStorage.firebaseToken, START_STATUS, "", "PeriodicWork")
        else
            WorkManager.getInstance().cancelAllWork()
        // Handler(Looper.getMainLooper()).postDelayed({TokenSendTask().sendCallLogs(context)}, 10000)


        Timber.d("PeriodicWork >> Background")

    }


}