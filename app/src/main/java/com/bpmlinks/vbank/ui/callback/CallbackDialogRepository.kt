package com.bpmlinks.vbank.ui.callback

import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.CallbackRequest
import com.bpmlinks.vbank.model.TimeSlots
import com.bpmlinks.vbank.model.TimeSlotsRequest
import com.bpmlinks.vbank.webservices.ApiStories
import okhttp3.ResponseBody
import javax.inject.Inject

class CallbackDialogRepository @Inject constructor(private val apiStories: ApiStories){

    suspend fun getTimeSlots(timeSlotsRequest: TimeSlotsRequest?): ApisResponse<TimeSlots> {
        return try {
            val callApi = apiStories.getTimeSlot(timeSlotsRequest?.bankerKey,timeSlotsRequest?.branchKey)
            ApisResponse.Success(callApi)
        } catch (e: Exception) {
            ApisResponse.Error(e)
        }
    }

    suspend fun scheduleCall(callbackRequest: CallbackRequest?): ApisResponse<ResponseBody> {
        return try {
            val callApi = apiStories.scheduleCall(callbackRequest)
            ApisResponse.Success(callApi)
        } catch (e: Exception) {
            ApisResponse.Error(e)
        }
    }
}