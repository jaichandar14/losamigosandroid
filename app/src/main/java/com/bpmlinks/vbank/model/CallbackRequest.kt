package com.bpmlinks.vbank.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CallbackRequest(@SerializedName("branchKeyNb")
                           var branchKeyNb: Int? = 0,
                           @SerializedName("scheduleDate")
                           var scheduleDate: String? = "",
                           @SerializedName("bankerKeyNb")
                           var bankerKeyNb: Int? = 0,
                           @SerializedName("customerKeyNb")
                           var customerKeyNb: Int? = 0,
                           @SerializedName("scheduleTimeKeyNb")
                           var scheduleTimeKeyNb: Int? = 0) : Parcelable


