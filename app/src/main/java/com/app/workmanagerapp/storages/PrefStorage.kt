package com.app.workmanagerapp.storages

import android.content.Context
import com.app.workmanagerapp.R
import com.f2prateek.rx.preferences2.RxSharedPreferences
import javax.inject.Inject

class PrefStorage @Inject constructor(context: Context) {
    private val prefs =
        RxSharedPreferences.create(context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE))

    private val tokenPreferences = prefs.getString("firebase_token")
    private val logStart = prefs.getBoolean("log_start")
    private val logSyncPreferences = prefs.getString("call_logs_sync")
    private val logString = prefs.getString("log_String")
    private val offlineDataPreferences = prefs.getString("offline_data")
    private val wifiListPreferences = prefs.getString("wifiList")
    private val currentLatitudePreferences = prefs.getString("current_latitude")
    private val currentLongitudePreferences = prefs.getString("current_longitude")

    var firebaseToken
    get() = tokenPreferences.get()
    set(value) = tokenPreferences.set(value)

    var taskLog
    get() = logString.get()
    set(value) = logString.set(value)

    var isWorkStart
        get() = logStart.get()
        set(value) = logStart.set(value)

    var offlineData
        get() = offlineDataPreferences.get()
        set(value) = offlineDataPreferences.set(value)

    var getWifiLog
        get() = wifiListPreferences.get()
        set(value) = wifiListPreferences.set(value)

    var lastCallLogSync
        get() = logSyncPreferences.get()
        set(value) = logSyncPreferences.set(value)

    var currentLatitude
        get() = currentLatitudePreferences.get()
        set(value) = currentLatitudePreferences.set(value)

    var currentLongitude
        get() = currentLongitudePreferences.get()
        set(value) = currentLongitudePreferences.set(value)

}