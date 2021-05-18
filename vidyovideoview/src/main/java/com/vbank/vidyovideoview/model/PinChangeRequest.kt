package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class PinChangeRequest(@SerializedName("callKeyNb")
                            var callKeyNb: Int? = 0,
                            @SerializedName("pinNumber")
                            var pinNumber: String? = "",
                            @SerializedName("expireDate")
                            var expireDate: String? = "",
                            @SerializedName("cardNumber")
                            var cardNumber: String? = "",
                            @SerializedName("customerKeyNb")
                            var customerKeyNb: Int? = 0)


