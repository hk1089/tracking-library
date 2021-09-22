package com.prime.track.services

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.prime.track.utils.*
import timber.log.Timber

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
        // Handler(Looper.getMainLooper()).postDelayed({TokenSendTask().sendCallLogs(context)}, 10000)


        Timber.d("PeriodicWork >> Background")

    }


}