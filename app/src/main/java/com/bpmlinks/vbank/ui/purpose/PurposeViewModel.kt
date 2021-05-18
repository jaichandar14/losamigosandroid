package com.bpmlinks.vbank.ui.purpose

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.*
import javax.inject.Inject

class PurposeViewModel @Inject constructor(private val purposeRepository: PurposeRepository, private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(), application: Application) : BaseViewModel(application) {
    var serviceItem: MasterServiceTypeDtosItem? = null
    var userInputDto:UserInput = UserInput()

    fun setSelected(subServiceKeyNb: Int) {
        serviceItem?.subServiceTypeDtos?.let{subServiceList->
            subServiceList.forEach { serviceItem ->
                serviceItem.apply {
                    serviceItem.selected = serviceItem.subServiceTypeKeyNb == subServiceKeyNb
                }
            }
        }
    }


    private var userInput = MutableLiveData<UserInput>()

    fun getUserInputParams(): LiveData<UserInput> = userInput

    fun newCustomer() = liveData(dispatchers.io()) {
        emit(ApisResponse.LOADING)
        emit(purposeRepository.newCustomer(userInputDto))
        emit(ApisResponse.COMPLETED)
    }

    fun onClick() {
        val params = UserInput()
        this.userInput.value = params
    }


    fun isSelected():Boolean {
        var isSelected = false
        serviceItem?.subServiceTypeDtos?.let{subServiceList->
            subServiceList.forEach { serviceItem ->
              if(serviceItem.selected){
                  isSelected = true
              }
            }
        }
        return isSelected
    }


}