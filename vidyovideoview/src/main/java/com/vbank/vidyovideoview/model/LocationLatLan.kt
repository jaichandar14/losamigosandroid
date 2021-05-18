package com.vbank.vidyovideoview.model

import com.google.gson.annotations.SerializedName

data class LocationLatLan(@SerializedName("longitude")
                          var longitude: String? = "0.0",
                          @SerializedName("latitude")
                          var latitude: String? = "0.0",
                          @SerializedName("customerKeyNb")
                          var customerKeyNb: Int? = 0,
                          @SerializedName("duringVideo")
                          var duringVideo: Boolean? = true,
                          @SerializedName("gpsOn")
                          var gpsOn: Boolean? = false,
                          @SerializedName("customerInCall")
                          var customerInCall: Boolean? = true,
                          @SerializedName("emailId")
                          var emailId: String? = "")

