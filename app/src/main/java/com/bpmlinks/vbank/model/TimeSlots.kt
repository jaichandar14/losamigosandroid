package com.bpmlinks.vbank.model


import com.google.gson.annotations.SerializedName

data class TimeSlotsData(@SerializedName("availableTimingMasterDtos")
                val availableTimingMasterDtos: List<AvailableTimingMasterDtosItem>?)


data class AvailableTimingMasterDtosItem(@SerializedName("timeSlot")
                                         val timeSlot: String = "",
                                         @SerializedName("scheduleTimeKeyNb")
                                         val scheduleTimeKeyNb: Int = 0)


data class TimeSlots(@SerializedName("result")
                     val result: Result,
                     @SerializedName("data")
                     val data: TimeSlotsData,
                     @SerializedName("success")
                     val success: Boolean = false,
                     @SerializedName("message")
                     val message: String = "",
                     @SerializedName("statusCode")
                     val statusCode: Int = 0)

data class TimeSlotsRequest(val bankerKey : Int?,val branchKey : Int?)

