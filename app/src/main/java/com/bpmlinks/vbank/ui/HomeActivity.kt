package com.bpmlinks.vbank.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseActivity
import com.bpmlinks.vbank.databinding.ActivityHomeBinding
import com.bpmlinks.vbank.helper.BundleKeys
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(),
    HasSupportFragmentInjector {
    override fun getContentView(): Int = R.layout.activity_home

    override fun getViewModel(): HomeViewModel? =
        ViewModelProvider(this).get(HomeViewModel::class.java)

    override fun getBindingVariable(): Int = BR.homeVM

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    /*Inject fragment object.*/
    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        val intent = intent.extras
        if (intent?.getBoolean(BundleKeys.MOVE_TO_USER_INPUT_SCREEN) == true) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.splashFragment, true)
                .build()
            Navigation.findNavController(this, R.id.nav_host_fragment)
                .navigate(R.id.loginFragment, null, navOptions)
        }

        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

            }
        }
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    /*This method is called whenever the user chooses to navigate Up within your application's activity.*/
    override fun onSupportNavigateUp(): Boolean {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp()
    }

}