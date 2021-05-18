package com.bpmlinks.vbank.ui.searchbranches

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class SearchBranchesModule {
    @Provides
    fun provideSearchBankerViewModel(
        searchBankerRepository: SearchBranchesRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): SearchBranchesViewModel {
        return SearchBranchesViewModel(
            searchBankerRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun provideSearchBankerRepository(apiStories: ApiStories): SearchBranchesRepository {
        return SearchBranchesRepository(apiStories)
    }

    @Provides
    fun provideViewModelProvider(viewModel: SearchBranchesViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}