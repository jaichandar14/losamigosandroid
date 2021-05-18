package com.bpmlinks.vbank.ui.searchbranches

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ZipCode
import javax.inject.Inject

class SearchBranchesViewModel @Inject constructor(
    private val searchBankerRepository: SearchBranchesRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : BaseViewModel(application) {

    var userCurrentLocation = MutableLiveData<String>()

    var zipcode = MutableLiveData<ZipCode>()

    fun getBranchesByZipCode() = zipcode.switchMap {
        liveData(dispatchers.io()) {
            emit(ApisResponse.LOADING)
            emit(searchBankerRepository.getBranchesByZipCode(it.zipCode))
            emit(ApisResponse.COMPLETED)
        }
    }

    fun setZipCode(zipCode : ZipCode?){
        this.zipcode.value =  zipCode
    }
}