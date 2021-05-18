package com.bpmlinks.vbank.model


import com.google.gson.annotations.SerializedName

data class FAQListResponse(@SerializedName("result")
                           val result: Result,
                           @SerializedName("data")
                           val data: FaqListData,
                           @SerializedName("success")
                           val success: Boolean = false,
                           @SerializedName("message")
                           val message: String = "",
                           @SerializedName("statusCode")
                           val statusCode: Int = 0)


data class FaqListData(@SerializedName("faqDtos")
                val faqList: ArrayList<FaqListItem>?)


data class FaqListItem(@SerializedName("question")
                       val question: String = "",
                       @SerializedName("answer")
                       val answer: String = "",
                       @SerializedName("questionKeyNb")
                       val questionKeyNb: Int = 0,
                       var isExpended : Boolean)



