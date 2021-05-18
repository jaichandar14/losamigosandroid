package com.bpmlinks.vbank.ui.faq

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.FAQListResponse
import javax.inject.Inject


class FAQViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : BaseViewModel(application) {


    fun getFAQList(): LiveData<ApisResponse<FAQListResponse>> = liveData(dispatchers.io()) {
        emit(ApisResponse.LOADING)
        emit(faqRepository.getFAQList())
        emit(ApisResponse.COMPLETED)
    }


}