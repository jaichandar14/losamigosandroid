package com.bpmlinks.vbank.ui.vehicleinspection

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bpmlinks.vbank.base.BaseViewModel
import javax.inject.Inject

class VehicleInspectionViewModel  @Inject constructor( application: Application) : BaseViewModel(application) {
    var scheduledTime = MutableLiveData<String>()
//    val scheduleDate = MutableLiveData<String>()


}