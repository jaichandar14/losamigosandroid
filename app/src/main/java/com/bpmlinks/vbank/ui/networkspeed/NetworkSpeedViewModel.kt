package com.bpmlinks.vbank.ui.networkspeed

import android.app.Application
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.model.BankerDtosItem
import javax.inject.Inject

class NetworkSpeedViewModel @Inject constructor(app: Application) :BaseViewModel(app) {
    var bankInfo: BankerDtosItem? = null
}