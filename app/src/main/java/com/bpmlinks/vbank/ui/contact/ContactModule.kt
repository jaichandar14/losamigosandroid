package com.bpmlinks.vbank.ui.contact

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class ContactModule {
    @Provides
    fun provideContactViewModel(
        ContactRepository: ContactRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): ContactViewModel {
        return ContactViewModel(
            ContactRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun provideContactRepository(apiStories: ApiStories): ContactRepository {
        return ContactRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: ContactViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}