package com.bpmlinks.vbank.ui.contact

import android.app.Application
import com.bpmlinks.vbank.base.BaseViewModel
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.coroutines.DispatcherProvider
import javax.inject.Inject

class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    application: Application
) : BaseViewModel(application) {

}