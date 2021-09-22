package com.prime.track.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.permissionx.guolindev.PermissionX

import android.os.Build
import java.text.SimpleDateFormat
import java.util.*
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Handler
import timber.log.Timber

import com.prime.track.modules.Scopes
import toothpick.Toothpick
import java.lang.StringBuilder
import android.os.Looper
import com.prime.track.storages.PrefStorage

import java.lang.Exception

import android.net.ConnectivityManager
import android.provider.CallLog
import android.provider.Settings
import com.prime.track.storages.CallLogsData

import com.google.android.gms.location.*
import com.google.gson.Gson
import com.prime.track.storages.WifiData
import io.nlopez.smartlocation.SmartLocation
import org.json.JSONArray
import org.json.JSONObject


const val WORK_NAME = "LocationFetch"
const val WORK_TAG = "PeriodicWork"
const val ONE_TIME_WORK_NAME = "oneTimeWorkNAME"
const val ONE_TIME_WORK_TAG = "oneTimeWork"

const val START_STATUS = "started"
const val STOP_STATUS = "stopped"
const val RUNNING_STATUS = "running"

const val NOTIFICATION_START = "Start"
const val NOTIFICATION_STOP = "Stop"

const val TOKEN_SEND_API = "http://elogist.in:8092/api/send-notification?token="
const val TOKEN_GPS_API = "http://elogist.in:8092/api/send-gps-data?token="
const val SEND_CALL_LOG_API = "http://elogist.in:8092/api/send-calllog-data"

val sendDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

val prefStorage: PrefStorage =
    Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)

fun FragmentActivity.permissions(
    list: List<String>,
    listener: (Boolean, List<String>, List<String>) -> Unit
) {

    PermissionX.init(this)
        .permissions(list)
        .onExplainRequestReason { scope, deniedList ->
            scope.showRequestReasonDialog(
                deniedList,
                "To check background service app need some permissions.",
                "OK",
                "Cancel"
            )
        }
        .request { allGranted, grantedList, deniedList ->
            listener.invoke(allGranted, grantedList, deniedList)
        }
}


fun convertTime(time: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return formatter.format(Date(time))
}

fun convertReverseTime(date: String): Long {
    val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
    return formatter.parse(date)!!.time
}


fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else {
        capitalize(manufacturer) + " " + model
    }
}

private fun capitalize(s: String?): String {
    if (s == null) {
        return ""
    }
    val first = s[0]
    return if (Character.isUpperCase(first)) {
        s
    } else {
        Character.toUpperCase(first).toString() + s.substring(1)
    }
}

fun Context.getCurrentLocation(listener: (JSONObject) -> Unit) {
    SmartLocation.with(this).location()
        .oneFix()
        .start { location ->
            val jsobject = JSONObject()
            jsobject.put("lat", location.latitude)
            jsobject.put("lng", location.longitude)
            jsobject.put("action", "2")
            jsobject.put("gps", "0")
            jsobject.put("fetch_time", convertTime(System.currentTimeMillis()))
            val jsonArray = JSONArray()
            jsonArray.put(jsobject)
            val jsonObject = JSONObject()
            jsonObject.put("locationInfo", jsonArray)
            listener.invoke(jsonObject)
        }
}

@SuppressLint("MissingPermission")
fun Context.getLocation(isStop: Boolean) {
    val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    val locationRequest = LocationRequest.create().apply {
        interval = 100
        fastestInterval = 50
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        maxWaitTime = 10
        smallestDisplacement = 1F
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                if (location.latitude != 0.0) {
                    prefStorage.currentLatitude = location.latitude.toString()
                    prefStorage.currentLongitude = location.longitude.toString()
                }

            }
            Timber.d("lat>> ${prefStorage.currentLatitude}, lng>> ${prefStorage.currentLongitude}")
        }
    }
    if (isStop)
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    else
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
}

@SuppressLint("MissingPermission")
fun Context.getLocationSecondAtt(listener: (String, String) -> Unit) {
    try {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val minDistanceUpdates = 1F // 1 meters
        val minTimeUpdates: Long = 1000 * 60 * 1 // 1 minute

        when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
                val thread: Thread = object : Thread() {
                    override fun run() {
                        Looper.prepare()
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed(object : Runnable {

                            override fun run() {
                                locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    minTimeUpdates,
                                    minDistanceUpdates
                                ) {

                                }
                                handler.removeCallbacks(this)
                            }
                        }, 3000)
                        Looper.loop()
                    }
                }
                thread.start()

                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                listener.invoke(location!!.latitude.toString(), location.longitude.toString())
            }
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> {
                val thread: Thread = object : Thread() {
                    override fun run() {
                        Looper.prepare()
                        val handler = Handler(Looper.getMainLooper())
                        handler.postDelayed(object : Runnable {
                            override fun run() {
                                locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    minTimeUpdates,
                                    minDistanceUpdates
                                ) {

                                }
                                handler.removeCallbacks(this)
                            }
                        }, 3000)
                        Looper.loop()
                    }
                }
                thread.start()

                val location =
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                listener.invoke(location!!.latitude.toString(), location.longitude.toString())
            }
            else -> listener.invoke("0", "0")
        }
    } catch (e: Exception) {
        listener.invoke("0", "0")
    }

}

@SuppressLint("Recycle", "Range")
fun Context.getCallLogs(temp: String, listener: (JSONObject) -> Unit) {
    val contentResolver = contentResolver
    val filter = convertReverseTime(temp).toString()
    val mSelectionClause = CallLog.Calls.DATE + " >= ?"
    val mSelectionArgs = arrayOf(filter)
    val cursor = if (filter.isNotEmpty())
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            mSelectionClause,
            mSelectionArgs,
            CallLog.Calls.DATE + " Desc"
        )
    else
        contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            CallLog.Calls.DATE + " Desc"
        )
    try {
        if (cursor != null) {
            val totalCalls = cursor.count
            val logList = mutableListOf<CallLogsData>()
            if (cursor.moveToFirst()) {
                for (i in 0 until totalCalls) {
                    val callLogsData = CallLogsData()
                    val subscriberId =
                        if (cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID)) != null) {
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.PHONE_ACCOUNT_ID))
                        } else {
                            ""
                        }
                    callLogsData.callerID = subscriberId
                    callLogsData.name =
                        if (cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) != null)
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                        else
                            ""
                    callLogsData.number =
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                    callLogsData.datetime = sendDateFormat.format(
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)).toLong()
                    )
                    callLogsData.duration =
                        cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION))

                    callLogsData.type =
                        when {
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt() == CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                    == CallLog.Calls.OUTGOING_TYPE.toString() -> "OUTGOING"
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt() == CallLog.Calls.MISSED_TYPE -> "MISSED"
                            cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))
                                .toInt() == CallLog.Calls.REJECTED_TYPE -> "REJECTED"
                            else -> ""
                        }
                    if (prefStorage.selectedSim == subscriberId) {
                        logList.add(callLogsData)
                    }
                    cursor.moveToNext()
                }
            }
            val str = Gson().toJson(logList)
            val jsonObject = JSONObject()
            jsonObject.put("aduserid", prefStorage.userId)
            jsonObject.put("entrymode", "1")
            jsonObject.put("callInfo", JSONArray(str).toString())
            listener.invoke(jsonObject)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.wifiStatus(listener: (JSONObject) -> Unit) {
    val wifi = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    wifi.startScan()
    val wifiList = wifi.scanResults
    val jsonArray = JSONArray()
    val wifiData = Gson().fromJson(prefStorage.savedWifi, WifiData::class.java)
    for (scanResult in wifiList) {
        wifiData.data.forEachIndexed { index, data ->
            if (scanResult.BSSID == data.bssid) {
                if (WifiManager.calculateSignalLevel(scanResult.level, 5) >= 1) {
                    val jsObject = JSONObject()
                    jsObject.put("wifi_id", data.id)
                    jsObject.put("strength", scanResult.level)
                    jsObject.put("fetch_time", convertTime(System.currentTimeMillis()))
                    jsonArray.put(jsObject)
                }
                Timber.d("SSID>> ${scanResult.SSID} &BSSID>> ${data.bssid} & Level>> ${scanResult.level}")
            }
        }
    }
    val jsonObject = JSONObject()
    jsonObject.put("wifiInfo", jsonArray.toString())
    jsonObject.put("ismanual", "0")
    jsonObject.put("lat", "null")
    jsonObject.put("long", "null")

    listener.invoke(jsonObject)

}

fun Context.getGPSStatus(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

fun Context.getNetworkStatus(listener: (Boolean, Boolean) -> Unit) {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = manager.activeNetworkInfo
    if (activeNetwork != null) {
        val wifi = activeNetwork.type == ConnectivityManager.TYPE_WIFI
        val mobile = activeNetwork.type == ConnectivityManager.TYPE_MOBILE
        listener.invoke(wifi, mobile)
    } else {
        listener.invoke(false, false)
    }
}


@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String =
    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: ""

fun Context.getAirplaneMode(): String {
    val result = Settings.Global.getString(contentResolver, Settings.Global.AIRPLANE_MODE_ON)
    return if (result == "0") "OFF" else "ON"
}

