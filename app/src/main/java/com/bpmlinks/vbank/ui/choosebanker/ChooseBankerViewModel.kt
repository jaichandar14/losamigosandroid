package com.bpmlinks.vbank.ui.choosebanker

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.BranchDtosItem
import javax.inject.Inject

class ChooseBankerViewModel @Inject constructor(
    private val chooseBankerRepository: ChooseBankerRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : BaseViewModel(application) {

    var branchInfoDtosItem = MutableLiveData<BranchDtosItem>()
}