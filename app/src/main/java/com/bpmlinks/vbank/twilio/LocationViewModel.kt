package com.bpmlinks.vbank.twilio

import android.app.Application
import androidx.lifecycle.AndroidViewModel


class LocationViewModel(application: Application):AndroidViewModel(application) {
private  var locationLiveData= LocationLiveData(application)
fun getLocationLiveDate()=locationLiveData
}