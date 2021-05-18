package com.vbank.vidyovideoview.model


import com.google.gson.annotations.SerializedName

data class DocuSignStatusRequest(@SerializedName("callKeyNb")
                                 var callKeyNb: Int? = 0,
                                 @SerializedName("action")
                                 var action: String? = "complete",
                                 @SerializedName("envolpeId")
                                 var envolpeId: String? = "")

data class Output(@SerializedName("result")
                  val result: Result,
                  @SerializedName("data")
                  val data: Msg,
                  @SerializedName("success")
                  val success: Boolean = false,
                  @SerializedName("message")
                  val message: String = "",
                  @SerializedName("statusCode")
                  val statusCode: Int = 0)

data class Msg(@SerializedName("statusMsg")
               val statusMsg: String)

