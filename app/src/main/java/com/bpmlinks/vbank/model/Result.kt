package com.bpmlinks.vbank.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Result(@SerializedName("info")
                  val info: String = "") : Parcelable