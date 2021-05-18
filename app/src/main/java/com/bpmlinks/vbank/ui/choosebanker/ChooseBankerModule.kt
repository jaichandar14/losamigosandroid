package com.bpmlinks.vbank.ui.choosebanker

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory

import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class ChooseBankerModule {
    @Provides
    fun provideChooseBankerViewModel(
        chooseBankerRepository: ChooseBankerRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): ChooseBankerViewModel {
        return ChooseBankerViewModel(
            chooseBankerRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun provideChooseBankerRepository(apiStories: ApiStories): ChooseBankerRepository {
        return ChooseBankerRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: ChooseBankerViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}