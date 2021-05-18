package com.bpmlinks.vbank.ui.login

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.model.UserInput
import com.bpmlinks.vbank.ui.purpose.PurposeRepository
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val purposeRepository: PurposeRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application) : BaseViewModel(application) {

    var userInput: UserInput = UserInput()


    fun getServiceType(): LiveData<ApisResponse<ServiceType>> = liveData(dispatchers.io()) {
        emit(ApisResponse.LOADING)
        emit(loginRepository.getServiceType())
        emit(ApisResponse.COMPLETED)
    }

    fun newCustomer() = liveData(dispatchers.io()) {
        emit(ApisResponse.LOADING)
        emit(purposeRepository.newCustomer(userInput))
        emit(ApisResponse.COMPLETED)
    }

}