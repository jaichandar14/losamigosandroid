package com.bpmlinks.vbank.ui.faq

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class FAQModule {
    @Provides
    fun provideFAQViewModel(
        FAQRepository: FAQRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): FAQViewModel {
        return FAQViewModel(
            FAQRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun provideFAQRepository(apiStories: ApiStories): FAQRepository {
        return FAQRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: FAQViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}