package com.prime.track.storages

import android.content.Context
import com.f2prateek.rx.preferences2.RxSharedPreferences
import com.prime.track.R
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
    private val getWifiListPreferences = prefs.getString("get_wifi_list")
    private val setWifiListPreferences = prefs.getString("set_wifi_list")
    private val setCallLogsPreferences = prefs.getString("set_call_logs")
    private val setLocationPreferences = prefs.getString("set_locations")
    private val apiHeaderPreferences = prefs.getString("api_header")
    private val savedWifiPreference = prefs.getString("saved_wifi")
    private val userIdPreferences = prefs.getString("userId")
    private val simSlotPreferences = prefs.getString("sim_slot")
    private val locationPreferences = prefs.getBoolean("is_user_location")
    private val wifiPreferences = prefs.getBoolean("is_user_wifi")
    private val callPreferences = prefs.getBoolean("is_user_call")
    private val selectedSimPreferences = prefs.getString("sim_selection")
    private val callerIdPreferences = prefs.getString("callerId")

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

    var getWifiListUrl
        get() = getWifiListPreferences.get()
        set(value) = getWifiListPreferences.set(value)

    var setWifiListUrl
        get() = setWifiListPreferences.get()
        set(value) = setWifiListPreferences.set(value)

    var setCallLogsUrl
        get() = setCallLogsPreferences.get()
        set(value) = setCallLogsPreferences.set(value)

    var setLocationsUrl
        get() = setLocationPreferences.get()
        set(value) = setLocationPreferences.set(value)

    var apiHeader
        get() = apiHeaderPreferences.get()
        set(value) = apiHeaderPreferences.set(value)

    var savedWifi
        get() = savedWifiPreference.get()
        set(value) = savedWifiPreference.set(value)

    var userId
        get() = userIdPreferences.get()
        set(value) = userIdPreferences.set(value)

    var simSelection
        get() = simSlotPreferences.get()
        set(value) = simSlotPreferences.set(value)

    var isLocation
        get() = locationPreferences.get()
        set(value) = locationPreferences.set(value)

    var isWifi
        get() = wifiPreferences.get()
        set(value) = wifiPreferences.set(value)

    var isCalls
        get() = callPreferences.get()
        set(value) = callPreferences.set(value)

    var callerId
        get() = callerIdPreferences.get()
        set(value) = callerIdPreferences.set(value)

    var selectedSim
        get() = selectedSimPreferences.get()
        set(value) = selectedSimPreferences.set(value)

}