package com.bpmlinks.vbank.di

import com.bpmlinks.vbank.ui.HomeActivity
import com.bpmlinks.vbank.ui.HomeModule
import com.bpmlinks.vbank.ui.callback.CallbackDialogFragment
import com.bpmlinks.vbank.ui.callback.CallbackDialogModule
import com.bpmlinks.vbank.ui.callscheduled.CallScheduledFragment
import com.bpmlinks.vbank.ui.callscheduled.CallScheduledModule
import com.bpmlinks.vbank.ui.choosebanker.ChooseBankerFragment
import com.bpmlinks.vbank.ui.choosebanker.ChooseBankerModule
import com.bpmlinks.vbank.ui.contact.ContactFragment
import com.bpmlinks.vbank.ui.contact.ContactModule
import com.bpmlinks.vbank.ui.faq.FAQFragment
import com.bpmlinks.vbank.ui.faq.FAQModule
import com.bpmlinks.vbank.ui.login.LoginFragment
import com.bpmlinks.vbank.ui.login.LoginModule
import com.bpmlinks.vbank.ui.networkspeed.NetworkSpeedFragment
import com.bpmlinks.vbank.ui.networkspeed.NetworkSpeedModule
import com.bpmlinks.vbank.ui.purpose.PurposeFragment
import com.bpmlinks.vbank.ui.purpose.PurposeModule
import com.bpmlinks.vbank.ui.searchbranches.SearchBranchesFragment
import com.bpmlinks.vbank.ui.searchbranches.SearchBranchesModule
import com.bpmlinks.vbank.ui.vehicleinspection.VehicleInspectionFragment
import com.bpmlinks.vbank.ui.vehicleinspection.VehicleInspectionModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/* The ActivityBuilder generates AndroidInjector for Activities defined in this class*/
@Module
abstract class ActivityBuilder {
    @ContributesAndroidInjector(modules = [HomeModule::class])
    abstract fun provideHomeActivity(): HomeActivity
    @ContributesAndroidInjector(modules = [LoginModule::class])
    abstract fun provideLoginFragment(): LoginFragment
    @ContributesAndroidInjector(modules = [PurposeModule::class])
    abstract fun providePurposeFragment(): PurposeFragment
    @ContributesAndroidInjector(modules = [VehicleInspectionModule::class])
    abstract fun provideVehicleInspectionFragment(): VehicleInspectionFragment

    @ContributesAndroidInjector(modules = [SearchBranchesModule::class])
    abstract fun provideSearchBankerFragment(): SearchBranchesFragment
    @ContributesAndroidInjector(modules = [ChooseBankerModule::class])
    abstract fun provideChooseBankerFragment(): ChooseBankerFragment



    @ContributesAndroidInjector(modules = [CallScheduledModule::class])
    abstract fun provideCallScheduledFragment(): CallScheduledFragment
    @ContributesAndroidInjector(modules = [NetworkSpeedModule::class])
    abstract fun provideNetworkSpeedFragment(): NetworkSpeedFragment
    @ContributesAndroidInjector(modules = [CallbackDialogModule::class])
    abstract fun provideCallbackDialogFragment(): CallbackDialogFragment
    @ContributesAndroidInjector(modules = [FAQModule::class])
    abstract fun provideFAQFragment(): FAQFragment
    @ContributesAndroidInjector(modules = [ContactModule::class])
    abstract fun provideContactFragment(): ContactFragment
}