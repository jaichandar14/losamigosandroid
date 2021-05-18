package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class DataItem(
    @SerializedName("title")
    val title: String? = "",
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("url")
    val url: String? = "",
    @SerializedName("cardNumber")
    val cardNumber: String? = "",
    @SerializedName("expirydate")
    val expirydate: String? = "",
    @SerializedName("publicKey")
    val publicKey: String? = "",
    var isSelected: Boolean? = false
)

data class UserActionItems(
    @SerializedName("result")
    val result: Result,
    @SerializedName("data")
    val data: MutableList<DataItem>?,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 0
)


data class DataItemResult(
    @SerializedName("info")
    val info: String = ""
)


