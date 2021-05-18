package com.bpmlinks.vbank.ui.vehicleinspection

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.helper.viewmodel.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class VehicleInspectionModule {
    @Provides
    fun provideVehicleInspectionViewModel(
        application: Application
    ): VehicleInspectionViewModel {
        return VehicleInspectionViewModel(
            application
        )
    }


    @Provides
    fun provideViewModelProvider(viewModel: VehicleInspectionViewModel): ViewModelProvider.Factory {
        return ViewModelProviderFactory(viewModel)
    }

}