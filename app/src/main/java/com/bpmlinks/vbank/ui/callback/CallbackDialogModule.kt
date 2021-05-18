package com.bpmlinks.vbank.ui.callback

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class CallbackDialogModule {
    @Provides
    fun provideCallbackDialogViewModel(
        callbackDialogRepository: CallbackDialogRepository,
        defaultDispatcherProvider:
        DefaultDispatcherProvider, application: Application
    ): CallbackDialogViewModel {
        return CallbackDialogViewModel(callbackDialogRepository, defaultDispatcherProvider, application)
    }

    @Provides
    fun provideCallbackDialogRepository(apiStories: ApiStories): CallbackDialogRepository {
        return CallbackDialogRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: CallbackDialogViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}