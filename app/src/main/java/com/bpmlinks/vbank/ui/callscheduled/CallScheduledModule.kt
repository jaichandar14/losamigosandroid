package com.bpmlinks.vbank.ui.callscheduled

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class CallScheduledModule {
    @Provides
    fun provideCallScheduledViewModel(
        application: Application
    ): CallScheduledViewModel { return CallScheduledViewModel(application) }

    @Provides
    fun provideCallScheduledProvider(viewModel: CallScheduledViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}