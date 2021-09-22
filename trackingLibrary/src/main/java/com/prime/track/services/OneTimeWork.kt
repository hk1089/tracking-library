package com.prime.track.services

import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.prime.track.utils.*
import timber.log.Timber

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
        if (prefStorage.isWorkStart){
            if (prefStorage.isLocation)
                context.getCurrentLocation { jsonObject ->
                    ApiTask().sendDataToServer(prefStorage.apiHeader, prefStorage.setLocationsUrl, jsonObject)
                }
            if (prefStorage.isWifi)
                context.wifiStatus { result->
                    ApiTask().sendDataToServer(prefStorage.apiHeader, prefStorage.setWifiListUrl, result)
                }
            if (prefStorage.isCalls)
                context.getCallLogs(prefStorage.lastCallLogSync){ jsonObject->
                    ApiTask().sendDataToServer(prefStorage.apiHeader, prefStorage.setCallLogsUrl, jsonObject)
                }
        }
        else
            WorkManager.getInstance().cancelAllWork()
        Timber.d("OneTimeWork >> Background")
    }
}