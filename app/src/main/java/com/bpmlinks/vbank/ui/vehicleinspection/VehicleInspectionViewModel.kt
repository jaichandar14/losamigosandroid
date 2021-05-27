package com.bpmlinks.vbank.ui.vehicleinspection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.UserInput
import com.bpmlinks.vbank.ui.purpose.PurposeRepository
import javax.inject.Inject

class VehicleInspectionViewModel  @Inject constructor(
    private val purposeRepository: PurposeRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application) : BaseViewModel(application) {

    var scheduledTime = MutableLiveData<String>()
    val scheduleDate = MutableLiveData<String>()

    var userInput: UserInput = UserInput()

    fun newCustomer() = liveData(dispatchers.io()) {
        emit(ApisResponse.LOADING)
        emit(purposeRepository.newCustomer(userInput))
        emit(ApisResponse.COMPLETED)
    }
}