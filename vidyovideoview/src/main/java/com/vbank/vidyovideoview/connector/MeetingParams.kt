package com.vbank.vidyovideoview.connector

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MeetingParams(
    var host: String = ConnectParams.HOST,
    var displayName: String? = ConnectParams.DISPLAY_NAME,
    var resource: String? = "",
    var token: String? = "",
    var callKeyNb: Int? = 0,
    var bankerName: String? = "",
    var title: String? = "",
    var isFromNotification: Boolean = false,
    var customerKeyNb: Int? = 0,
    var docusignurl:String?="",
    var roomName:String?="",
    var longitude: String ="0.0",
    var latitude: String="0.0",
    var duringVideo:Boolean?=true,
    var gpsOn:Boolean?=false,
    var customerInCall:Boolean?=true,
    var emailId:String? = "",
    var meetingTime:String? = ""

) : Parcelable {

}