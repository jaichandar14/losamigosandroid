package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class BankerNameData(@SerializedName("bankerName")
                val bankerName: String? = "")


data class BankerName(@SerializedName("result")
                      val result: Result,
                      @SerializedName("data")
                      val data: BankerNameData? = null,
                      @SerializedName("success")
                      val success: Boolean = false,
                      @SerializedName("message")
                      val message: String = "",
                      @SerializedName("statusCode")
                      val statusCode: Int = 0)




