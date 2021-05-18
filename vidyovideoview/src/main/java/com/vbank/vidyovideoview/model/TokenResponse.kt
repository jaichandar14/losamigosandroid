package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class TokenResponse(@SerializedName("result")
                         val result: Result,
                         @SerializedName("data")
                         val data: Data,
                         @SerializedName("success")
                         val success: Boolean = false,
                         @SerializedName("message")
                         val message: String = "",
                         @SerializedName("statusCode")
                         val statusCode: Int = 0)


data class Data(@SerializedName("vidyoToken")
                val vidyoToken: String = "",
                @SerializedName("roomId")
                val roomId: String = "",
                @SerializedName("callKeyNb")
                var callKeyNb : Int? = 0)


data class Result(@SerializedName("info")
                  val info: String = "")


