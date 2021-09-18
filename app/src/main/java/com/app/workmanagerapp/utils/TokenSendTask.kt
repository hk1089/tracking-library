package com.app.workmanagerapp.utils

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.StringRequestListener
import com.app.workmanagerapp.modules.Scopes
import com.app.workmanagerapp.storages.PrefStorage
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import toothpick.Toothpick

class TokenSendTask {
    companion object {
        private val prefStorage: PrefStorage =
            Toothpick.openScope(Scopes.APP).getInstance(PrefStorage::class.java)
    }

    fun sendToken(context: Context, token: String, status: String, listener: (Boolean) -> Unit) {
        val deviceName: String = getDeviceName().replace(" ", "_")
        AndroidNetworking.get("$TOKEN_SEND_API$token&device=$deviceName&uuid=${context.getDeviceId()}&notificationStatus=$status")
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsString(object : StringRequestListener {
                override fun onResponse(response: String?) {
                    if (response != null)
                        prefStorage.lastCallLogSync = response
                    listener.invoke(true)
                }

                override fun onError(anError: ANError?) {
                    anError?.printStackTrace()
                }

            })
    }

    fun sendGPS(
        context: Context,
        token: String,
        status: String,
        history: String,
        callFrom: String
    ) {
        context.getLocation(false)
        context.getNetworkStatus { isWifi, isMobile ->
            val wifiStatus = if (isWifi) "ON" else "OFF"
            val mobileDataStatus = if (isMobile) "ON" else "OFF"
            val gpsStatus = if (context.getGPSStatus()) "ON" else "OFF"
            val airMode = context.getAirplaneMode()
            if (isMobile || isWifi) {

                if (prefStorage.currentLatitude == "0") {
                    context.getLocationSecondAtt { lat, lng ->
                        context.wifiStatus { wifis, ioStatus ->
                            val date: String = convertTime(System.currentTimeMillis())
                            val deviceName: String = getDeviceName().replace(" ", "_")
                            AndroidNetworking.get("$TOKEN_GPS_API$token&lat=$lat&lng=$lng&fetchDate=$date&device=$deviceName&serviceStatus=$status&inOutStatus=$ioStatus&wifiLog=$wifis&wifiStatus=$wifiStatus&mobileDataStatus=$mobileDataStatus&gpsStatus=$gpsStatus&history=$history&airplanMode=$airMode")
                                .setPriority(Priority.IMMEDIATE)
                                .build()
                                .getAsString(object : StringRequestListener {
                                    override fun onResponse(response: String?) {
                                        if (prefStorage.offlineData.isNotEmpty()) {
                                            sendGPS(
                                                context,
                                                token,
                                                status,
                                                prefStorage.offlineData, "Self"
                                            )
                                            prefStorage.offlineData = ""

                                        }
                                        if (callFrom == "PeriodicWork")
                                            sendCallLogs(context)
                                    }

                                    override fun onError(anError: ANError?) {
                                        anError?.printStackTrace()
                                    }

                                })
                        }
                    }
                } else {
                    context.wifiStatus { wifis, ioStatus ->
                        val date: String = convertTime(System.currentTimeMillis())
                        val deviceName: String = getDeviceName().replace(" ", "_")
                        AndroidNetworking.get("$TOKEN_GPS_API$token&lat=${prefStorage.currentLatitude}&lng=${prefStorage.currentLongitude}&fetchDate=$date&device=$deviceName&serviceStatus=$status&inOutStatus=$ioStatus&wifiLog=$wifis&wifiStatus=$wifiStatus&mobileDataStatus=$mobileDataStatus&gpsStatus=$gpsStatus&history=$history&airplanMode=$airMode")
                            .setPriority(Priority.IMMEDIATE)
                            .build()
                            .getAsString(object : StringRequestListener {
                                override fun onResponse(response: String?) {
                                    Timber.i("callFrom>> $callFrom")
                                    if (prefStorage.offlineData.isNotEmpty()) {
                                        sendGPS(
                                            context,
                                            token,
                                            status,
                                            prefStorage.offlineData,
                                            "Self"
                                        )
                                        prefStorage.offlineData = ""
                                    }
                                    if (callFrom == "PeriodicWork")
                                        sendCallLogs(context)
                                }

                                override fun onError(anError: ANError?) {
                                    anError?.printStackTrace()
                                }

                            })
                    }
                }


            } else {
                val offlineData =
                    "wifi-OFF mobileData-OFF gps-${gpsStatus} time-${convertTime(System.currentTimeMillis())} airplaneMode-$airMode"
                if (prefStorage.offlineData.isEmpty()) {
                    prefStorage.offlineData = offlineData
                }
                Timber.d("Hereee>> ${prefStorage.offlineData}")
            }

        }
    }

    fun sendCallLogs(context: Context) {
        context.getCallLogs(prefStorage.lastCallLogSync) {
            val str = Gson().toJson(it)
            val main = JSONObject()
            main.put("uuid", context.getDeviceId())
            main.put("syncDate", convertTime(System.currentTimeMillis()))
            main.put("callLogs", JSONArray(str))
            AndroidNetworking.post(SEND_CALL_LOG_API)
                .addJSONObjectBody(main)
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsString(object : StringRequestListener {
                    override fun onResponse(response: String?) {
                        prefStorage.lastCallLogSync = convertTime(System.currentTimeMillis())
                    }

                    override fun onError(anError: ANError?) {
                        anError?.printStackTrace()
                    }

                })

        }

    }
}