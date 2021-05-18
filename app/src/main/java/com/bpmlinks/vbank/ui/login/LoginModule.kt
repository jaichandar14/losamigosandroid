package com.bpmlinks.vbank.ui.login

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.ui.purpose.PurposeRepository
import com.bpmlinks.vbank.webservices.ApiStories
import dagger.Module
import dagger.Provides

@Module
class LoginModule {
    @Provides
    fun provideLoginViewModel(
        loginRepository: LoginRepository,
        purposeRepository: PurposeRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): LoginViewModel {
        return LoginViewModel(
            loginRepository,
            purposeRepository,
            defaultDispatcherProvider,
            application
        )
    }

    @Provides
    fun provideLoginRepository(apiStories: ApiStories): LoginRepository {
        return LoginRepository(apiStories)
    }

    @Provides
    fun providePurposeRepository(apiStories: ApiStories): PurposeRepository {
        return PurposeRepository(apiStories)
    }
    @Provides
    fun provideViewModelProvider(viewModel: LoginViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}