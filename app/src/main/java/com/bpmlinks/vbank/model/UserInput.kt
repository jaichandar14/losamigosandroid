package com.bpmlinks.vbank.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CustomerPurposeDto(
    @SerializedName("subServiceTypeKeyNb")
    var subServiceTypeKeyNb: Int = 0,
    @SerializedName("masterServiceTypeKeyNb")
    var masterServiceTypeKeyNb: Int = 0,
    @SerializedName("customerServiceKeyNb")
    var customerServiceKeyNb: Int = 0,
    @SerializedName("customerKeyNb")
    var customerKeyNb: Int = 0
) : Parcelable

@Parcelize
data class UserInput(
    @SerializedName("customerServiceKeyNb")
    var customerServiceKeyNb: Int = 0,
    @SerializedName("zipCode")
    var zipCode: String? = "",
    @SerializedName("addressLineTwo")
    var addressLineTwo: String? = "",
    @SerializedName("customerServiceDto")
    var customerPurposeDto: CustomerPurposeDto? = CustomerPurposeDto(),
    @SerializedName("mobileNumber")
    var mobileNumber: String? = "",
    @SerializedName("customerFirstName")
    var customerFirstName: String? = "",
    @SerializedName("addressLineOne")
    var addressLineOne: String? = "",
    @SerializedName("customerLastName")
    var customerLastName: String? = "",
    @SerializedName("emailId")
    var emailId: String? = "",
    @SerializedName("customerKeyNb")
    var customerKeyNb: Int = 0,
    @SerializedName("deviceOS")
    var deviceOS: String? = "",
    @SerializedName("pushToken")
    var pushToken: String? = "",
    @SerializedName("hardwareId")
    var hardwareId: String? = "",
    @SerializedName("osVersion")
    var osVersion: String? = "",
   @SerializedName("buildNumber")
var buildNumber: String? = "",
@SerializedName("modelName")
var modelName: String? = "",
@SerializedName("deviceName")
var deviceName: String? = ""






) : Parcelable
