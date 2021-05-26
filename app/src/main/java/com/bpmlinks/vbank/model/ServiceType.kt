package com.bpmlinks.vbank.model


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class SubServiceTypeDtosItem(
    @SerializedName("subServiceTypeKeyNb")
    val subServiceTypeKeyNb: Int = 0,
    @SerializedName("masterServiceTypeKeyNb")
    val masterServiceTypeKeyNb: Int = 0,
    @SerializedName("insertById")
    val insertById: Int = 0,
    @SerializedName("subServiceTypeName")
    val subServiceTypeName: String? = "",
    @SerializedName("lastModifiedId")
    val lastModifiedId: Int = 0,
    var selected: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(subServiceTypeKeyNb)
        parcel.writeInt(masterServiceTypeKeyNb)
        parcel.writeInt(insertById)
        parcel.writeString(subServiceTypeName)
        parcel.writeInt(lastModifiedId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SubServiceTypeDtosItem> {
        override fun createFromParcel(parcel: Parcel): SubServiceTypeDtosItem {
            return SubServiceTypeDtosItem(parcel)
        }

        override fun newArray(size: Int): Array<SubServiceTypeDtosItem?> {
            return arrayOfNulls(size)
        }
    }
}


data class ServiceType(
    @SerializedName("result")
    val result: Result,
    @SerializedName("data")
    val data: Service,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 0
)


data class SubServiceType(
    @SerializedName("result")
    val result: Result,
    @SerializedName("data")
    val data: SubServiceTypeDtos,
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("statusCode")
    val statusCode: Int = 0
)

data class SubServiceTypeDtos(
    @SerializedName("transType")
    val transType: String = "",
    @SerializedName("id")
    val id: String = "",
    @SerializedName("subServiceTypeDtos")
    val subServiceTypeDtos: List<SubServiceTypeDtosItem>?
)

data class MasterServiceTypeDtosItem(
    @SerializedName("masterServiceTypeName")
    val masterServiceTypeName: String? = "",
    @SerializedName("masterServiceTypeKeyNb")
    val masterServiceTypeKeyNb: Int = 0,
    @SerializedName("insertById")
    val insertById: Int = 0,
    @SerializedName("lastModifiedId")
    val lastModifiedId: Int = 0,
    @SerializedName("subServiceTypeDtos")
    val subServiceTypeDtos: List<SubServiceTypeDtosItem>?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.createTypedArrayList(SubServiceTypeDtosItem)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(masterServiceTypeName)
        parcel.writeInt(masterServiceTypeKeyNb)
        parcel.writeInt(insertById)
        parcel.writeInt(lastModifiedId)
        parcel.writeTypedList(subServiceTypeDtos)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MasterServiceTypeDtosItem> {
        override fun createFromParcel(parcel: Parcel): MasterServiceTypeDtosItem {
            return MasterServiceTypeDtosItem(parcel)
        }

        override fun newArray(size: Int): Array<MasterServiceTypeDtosItem?> {
            return arrayOfNulls(size)
        }
    }
}

data class Service(
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("masterServiceTypeDtos")
    val masterServiceTypeDtos: List<MasterServiceTypeDtosItem>?,
    @SerializedName("subServiceTypeDtos")
    val subServiceTypeDtos: String? = "",
    @SerializedName("schdeuleTime")
    val schdeuleTime:String?="",
    @SerializedName("scheduleDate")
    val scheduleDate:String?=""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.createTypedArrayList(MasterServiceTypeDtosItem),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeTypedList(masterServiceTypeDtos)
        parcel.writeString(subServiceTypeDtos)
        parcel.writeString(schdeuleTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Service> {
        override fun createFromParcel(parcel: Parcel): Service {
            return Service(parcel)
        }

        override fun newArray(size: Int): Array<Service?> {
            return arrayOfNulls(size)
        }
    }
}






