package com.prime.track

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.google.gson.Gson
import com.prime.track.modules.AppModule
import com.prime.track.modules.Scopes
import com.prime.track.utils.*
import timber.log.Timber
import toothpick.Toothpick
import java.util.concurrent.ExecutionException

class MainClass constructor(val context: Context) {


    private var periodicHelper: PeriodicHelper = PeriodicHelper(context)
    private var apiTask: ApiTask = ApiTask()

    companion object {
        private const val FIREBASE_TYPE = "type"
        private const val FIREBASE_TYPE_VALUE = "1"
    }

    init {
        initLogger()
        initDI()
    }


    fun initializeValue() {
        prefStorage.getWifiListUrl = "http://elogist.in/itrm_webservices/Admin/getWifisMasterList"
        prefStorage.setWifiListUrl = "http://elogist.in/itrm_webservices/Admin/saveWifiLog"
        prefStorage.setCallLogsUrl =
            "http://elogist.in/itrm_webservices/UserCallLogs/setUserCallLogs"
        prefStorage.setLocationsUrl =
            "http://elogist.in/itrm_webservices/Location/saveLocationTrackingLogs"
        val map = HashMap<String, Any>()
        map["Content-Type"] = "application/json"
        map["version"] = "1.0"
        map["entrymode"] = "1"
        map["authkey"] =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6OTMsImZvaWQiOjAsIm5hbWUiOiJLdWxkZWVwIFByYWphcGF0IiwibW9iaWxlbm8iOjg5NjM4MDUxNDYsImVtYWlsIjoia3VsZGVlcC5wcmFqYXBhdEBlbG9naXN0LmluIiwidGltZSI6IjIwMjEtMDktMTVUMTg6MzQ6MzArMDU6MzAifQ.PUpot7zTcBEk2TsvIEJKokxd_tWqAxX9oN0fBLaK3nA"
        prefStorage.apiHeader = Gson().toJson(map)
        prefStorage.userId = "93"
        apiTask.getWifiList()
        prefStorage.isCalls = false
        prefStorage.isWifi = false
        prefStorage.isLocation = true

    }

    fun doTask(isStart: Boolean) {
        if (context is FragmentActivity) {
            val permissionList = mutableListOf<String>()
            if (prefStorage.isLocation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    permissionList.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                } else {
                    permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
                    permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }
            if (prefStorage.isCalls)
                permissionList.add(Manifest.permission.READ_CALL_LOG)
            checkPermission(permissionList, isStart)
        }else{
            checkPermission(mutableListOf(), isStart)
        }

    }

    private fun checkPermission(permissionList: MutableList<String>, isStart: Boolean) {
        if (isStart) {
            (context as FragmentActivity).permissions(
                permissionList
            ) { allGranted, _, deniedList ->
                if (allGranted) {
                    periodicHelper.startLog()
                }

            }
        }else{
            periodicHelper.stopLog()
        }
    }

    fun onNotification(remoteMessage: Map<String, String>) {
        val type = remoteMessage[FIREBASE_TYPE]
        if (type == "1" && prefStorage.isWorkStart) {

            if (getStateOfWork() != WorkInfo.State.ENQUEUED && getStateOfWork() != WorkInfo.State.RUNNING) {
                periodicHelper.startLog()
            } else {
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

    private fun checkService() {
        if (prefStorage.isWorkStart) {
            WorkManager.getInstance().cancelAllWork()
            periodicHelper.startLog()
        }
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initDI() {
        Toothpick.openScope(Scopes.APP).installModules(
            AppModule(
                context
            )
        )
        AndroidNetworking.initialize(context)
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY)
    }
}