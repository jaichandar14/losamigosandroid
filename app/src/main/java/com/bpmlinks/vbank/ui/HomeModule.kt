package com.bpmlinks.vbank.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class HomeModule {
    @Provides
    fun provideViewModelProvider(viewModel: HomeViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

    @Provides
    fun provideHomeViewModel(application: Application) : HomeViewModel {
        return HomeViewModel(application)
    }
}