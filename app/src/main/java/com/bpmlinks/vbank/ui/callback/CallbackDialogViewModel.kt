package com.bpmlinks.vbank.ui.callback

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.*
import javax.inject.Inject

class CallbackDialogViewModel @Inject constructor(
    private val callbackDialogRepository: CallbackDialogRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : BaseViewModel(application)  {

    private val timeSlotsRequest = MutableLiveData<TimeSlotsRequest>()
     val branchDetails = MutableLiveData<BranchDtosItem>()

    var availableSlots = MutableLiveData<AvailableTimingMasterDtosItem>()

    fun getTimeSlots() = timeSlotsRequest.switchMap {
        liveData(dispatchers.io()) {
            emit(ApisResponse.LOADING)
            emit(callbackDialogRepository.getTimeSlots(it))
            emit(ApisResponse.COMPLETED)
        }
    }

     fun setTimeSlotRequest(timeSlotsRequest: TimeSlotsRequest?){
        this.timeSlotsRequest.value =  timeSlotsRequest
    }


    private val callbackRequest = MutableLiveData<CallbackRequest>()

    fun scheduleCall() = callbackRequest.switchMap {
        liveData(dispatchers.io()) {
            emit(ApisResponse.LOADING)
            emit(callbackDialogRepository.scheduleCall(it))
            emit(ApisResponse.COMPLETED)
        }
    }

    fun setCallBackRequest(callbackRequest: CallbackRequest?){
        this.callbackRequest.value =  callbackRequest
    }



}