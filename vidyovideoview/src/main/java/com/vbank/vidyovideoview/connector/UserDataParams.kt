package com.vbank.vidyovideoview.connector

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDataParams(var host : String = ConnectParams.HOST, var displayName : String? = ConnectParams.DISPLAY_NAME,
var resource : String = "", var token : String = "",val customerKeyNb : Int?=0,
                          val branchKeyNb : Int? = 0,val brankerKeyNb: Int?=0,val bankerName : String? = null) : Parcelable