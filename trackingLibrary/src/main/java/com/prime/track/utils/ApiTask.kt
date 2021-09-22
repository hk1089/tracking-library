package com.prime.track.utils

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.androidnetworking.interfaces.StringRequestListener
import com.google.gson.*
import com.prime.track.storages.PrefStorage
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import com.google.gson.annotations.SerializedName

import com.prime.track.storages.WifiData


class ApiTask {

    fun getWifiList(){
        val header = Gson().fromJson(prefStorage.apiHeader, Map::class.java)
        AndroidNetworking.get(prefStorage.getWifiListUrl)
            .addHeaders(header)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {
                    val parser = JsonParser.parseString(response.toString())
                    val gson = Gson()
                    val wifiData: WifiData = gson.fromJson(parser, WifiData::class.java)
                    prefStorage.savedWifi = gson.toJson(wifiData)
                }

                override fun onError(anError: ANError?) {
                    anError?.printStackTrace()
                }

            })
    }
    fun sendDataToServer(headerStr: String, url: String, jsonObject: JSONObject){
        val header = Gson().fromJson(headerStr, Map::class.java)
        AndroidNetworking.post(url)
            .addHeaders(header)
            .addJSONObjectBody(jsonObject)
            .setPriority(Priority.IMMEDIATE)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener{
                override fun onResponse(response: JSONObject?) {

                }

                override fun onError(anError: ANError?) {
                    anError?.printStackTrace()
                }

            })
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



    fun sendCallLogs(context: Context) {
        val prefStorage = PrefStorage(context)
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