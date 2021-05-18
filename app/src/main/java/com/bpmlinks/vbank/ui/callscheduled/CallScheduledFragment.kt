package com.bpmlinks.vbank.ui.callscheduled

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentCallScheduledBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_call_scheduled.*
import javax.inject.Inject

class CallScheduledFragment : BaseFragment<FragmentCallScheduledBinding,CallScheduledViewModel>(){

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): CallScheduledViewModel? =
        ViewModelProvider(this,factory).get(CallScheduledViewModel::class.java)

    override fun getBindingVariable(): Int = BR.callScheduledVM

    override fun getContentView(): Int = R.layout.fragment_call_scheduled

    private val navArgs by navArgs<CallScheduledFragmentArgs>()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        init()
    }

    private fun init(){
        btn_exit.setOnClickListener {
            findNavController().popBackStack(R.id.searchBankerFragment,false)
        }
        getViewModel()?.scheduledTime?.value = navArgs.ScheduledTime
        getViewModel()?.branchDetails?.value = navArgs.BranchDetails
    }

    override fun internetConnected() {

    }

}