package com.bpmlinks.vbank.ui.networkspeed

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class NetworkSpeedModule {
    @Provides
    fun provideNetworkSpeedViewModel(application: Application): NetworkSpeedViewModel {
        return NetworkSpeedViewModel(
            application
        )
    }

    @Provides
    fun provideViewModelProvider(viewModel: NetworkSpeedViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }
}