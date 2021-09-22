package com.prime.track.storages

import com.google.gson.annotations.SerializedName

data class WifiData(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("success")
    val success: Boolean
)

data class Data(
    @SerializedName("bssid")
    val bssid: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("ip")
    val ip: String,
    @SerializedName("office_id")
    val officeId: Int,
    @SerializedName("_office_id")
    val _officeId: Int,
    @SerializedName("office_name")
    val officeName: String,
    @SerializedName("ssid")
    val ssid: String,
    @SerializedName("strength_range")
    val strengthRange: Int
)
