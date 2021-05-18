package com.bpmlinks.vbank.ui.callscheduled

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.model.BranchDtosItem
import javax.inject.Inject

class CallScheduledViewModel @Inject constructor(application: Application) : BaseViewModel(application){
    var scheduledTime = MutableLiveData<String>()
    val branchDetails = MutableLiveData<BranchDtosItem>()

}