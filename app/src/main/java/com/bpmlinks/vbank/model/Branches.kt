package com.bpmlinks.vbank.model


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BankerDtosItem(
    @SerializedName("branchKeyNb")
    val branchKeyNb: Int = 0,
    @SerializedName("statusKeyNb")
    val statusKeyNb: Int = 0,
    @SerializedName("mobileNumber")
    val mobileNumber: String = "",
    @SerializedName("bankerName")
    var bankerName: String = "",
    @SerializedName("available")
    val available: Boolean = false,
    @SerializedName("emailId")
    val emailId: String = "",
    @SerializedName("designation")
    val designation: String = "",
    @SerializedName("bankerKeyNb")
    val bankerKeyNb: Int = 0,
    var currentSpeed: Int = 0
) : Parcelable

@Parcelize
data class Branches(
    @SerializedName("result")
    val result: Result,
    @SerializedName("data")
    val data: Data,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 0,
    var currentSpeed: Int = 0
) : Parcelable

@Parcelize
data class BranchDtosItem(
    @SerializedName("zipCode")
    val zipCode: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("city")
    val city: String = "",
    @SerializedName("addressLineOne")
    val addressLineOne: String = "",
    @SerializedName("branchName")
    val branchName: String = "",
    @SerializedName("emailId")
    val emailId: String = "",
    @SerializedName("branchKeyNb")
    val branchKeyNb: Int = 0,
    @SerializedName("branchCode")
    val branchCode: String = "",
    @SerializedName("branchAvailable")
    val branchAvailable: Boolean = false,
    @SerializedName("addressLineTwo")
    val addressLineTwo: String = "",
    @SerializedName("bankerDtos")
    val bankerDtos: List<BankerDtosItem>? = null,
    @SerializedName("contactNumber")
    val contactNumber: String = "",
    @SerializedName("state")
    val state: String = "",
    @SerializedName("distance")
    val distance: String = "") : Parcelable

@Parcelize
data class Data(
    @SerializedName("branchDtos")
    val branchDtos: List<BranchDtosItem>?
) : Parcelable





