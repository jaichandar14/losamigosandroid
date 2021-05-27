package com.bpmlinks.vbank.ui.vehicleinspection

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.coroutines.DefaultDispatcherProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import com.bpmlinks.vbank.ui.purpose.PurposeRepository
import dagger.Module
import dagger.Provides

@Module
class VehicleInspectionModule {
    @Provides
    fun provideVehicleInspectionViewModel(
        purposeRepository: PurposeRepository,
        defaultDispatcherProvider: DefaultDispatcherProvider,
        application: Application
    ): VehicleInspectionViewModel {
        return VehicleInspectionViewModel(
            purposeRepository,
            defaultDispatcherProvider,
            application
        )
    }


    @Provides
    fun provideViewModelProvider(viewModel: VehicleInspectionViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}