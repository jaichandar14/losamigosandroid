package com.bpmlinks.vbank.ui.purpose

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class PurposeModule {
    @Provides
    fun providePurposeViewModel(
        purposeRepository: PurposeRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): PurposeViewModel {
        return PurposeViewModel(
            purposeRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun providePurposeRepository(apiStories: ApiStories): PurposeRepository {
        return PurposeRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: PurposeViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}