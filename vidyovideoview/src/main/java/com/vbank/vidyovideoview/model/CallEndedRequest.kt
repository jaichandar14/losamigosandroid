package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class CallEndedRequest(@SerializedName("callKeyNb")
                            val callKeyNb: Int? = 0,
                            @SerializedName("reason")
                            val reason: String? = "",
                            @SerializedName("bankerKeyNb")
                            val bankerKeyNb: Int? = 0,
                            @SerializedName("customerKeyNb")
                            val customerKeyNb: Int? = 0)


