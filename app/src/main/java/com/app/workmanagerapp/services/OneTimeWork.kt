package com.app.workmanagerapp.services

import android.content.Context
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

class OneTimeWork(private val context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {
    private lateinit var periodicHelper: PeriodicHelper
    override fun doWork(): Result {
        periodicHelper = PeriodicHelper(context)
        executeTask()
        periodicHelper.startSingleLog()
        Timber.e("OneTimeWork: isStopped-------> $isStopped")
        return Result.success()
    }

    private fun executeTask() {
        val prefStorage: PrefStorage =
            Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)
        if (prefStorage.isWorkStart)
            TokenSendTask().sendGPS(context, prefStorage.firebaseToken, START_STATUS, "", "OneTimeWork")
        else
            WorkManager.getInstance().cancelAllWork()
        Timber.d("OneTimeWork >> Background")
    }
}