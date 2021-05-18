package com.bpmlinks.vbank.ui.purpose

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentChooseServiceTypeBinding
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.helper.AppConstants.DEVICE_OS
import com.bpmlinks.vbank.helper.AppPreferences
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.SubServiceTypeDtosItem
import com.bpmlinks.vbank.ui.purpose.adapter.SubServiceTypeAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_choose_service_type.*
import java.util.*
import javax.inject.Inject

class PurposeFragment : BaseFragment<FragmentChooseServiceTypeBinding, PurposeViewModel>(),
    View.OnClickListener, SubServiceTypeAdapter.OnSubServiceClickListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val adapter = SubServiceTypeAdapter()
    private val navArgs by navArgs<PurposeFragmentArgs>()

    override fun getViewModel(): PurposeViewModel? =
        ViewModelProvider(this, factory).get(PurposeViewModel::class.java)

    override fun getBindingVariable(): Int = BR.purposeVM

    override fun getContentView(): Int = R.layout.fragment_choose_service_type

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        getViewModel()?.serviceItem = navArgs.serviceItem
        getViewModel()?.userInputDto = navArgs.userInput!!
        init()
    }

    private fun init() {
        if(getViewModel()?.isSelected()!!){
            btn_next.isEnabled = true
            btn_next.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(),R.color.color_splash))
        }else{
            btn_next.isEnabled = false
            btn_next.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(),R.color.color_grey2))
        }
        btn_next.setOnClickListener(this)
        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
        layout_choose.setHasFixedSize(true)
        layout_choose.adapter = adapter
        getViewModel()?.serviceItem?.subServiceTypeDtos?.let { adapter.refreshItems(it) }
        adapter.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_next -> {
                if(checkInternetAvailable()){
                    newCustomer()
                }
            }
        }
    }

    override fun onSubServiceClicked(service: SubServiceTypeDtosItem) {
        btn_next.isEnabled = true
        btn_next.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireActivity(),R.color.color_splash))
        getViewModel()?.setSelected(service.subServiceTypeKeyNb)
        getViewModel()?.userInputDto?.customerPurposeDto?.subServiceTypeKeyNb = service.subServiceTypeKeyNb
    }

    private fun newCustomer() {
        val androidID = UUID.randomUUID().toString()
        getViewModel()?.userInputDto?.deviceOS = DEVICE_OS
        getViewModel()?.userInputDto?.hardwareId = androidID
        getViewModel()?.userInputDto?.pushToken = AppPreferences.getInstance().getStringValue(requireActivity(),AppPreferences.FCM_TOKEN)
        getViewModel()?.newCustomer()
            ?.observe(viewLifecycleOwner, Observer { apiResponse ->
                when (apiResponse) {
                    is ApisResponse.Success -> {
                        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return@Observer
                        with (sharedPref.edit()) {
                            apiResponse.response.data.id?.toInt()?.let {id->
                                putInt(AppConstants.CUSTOMER_KEY_NB, id) }
                            commit() }
                        with (sharedPref.edit()) {
                            getViewModel()?.userInputDto?.customerFirstName?.let {name->
                                putString(AppConstants.CUSTOMER_NAME, name) }
                            commit() }
                        moveToNextScreen()
                    }
                    is ApisResponse.Error -> {
                        showToast(apiResponse.exception.localizedMessage!!)
                    }
                    ApisResponse.LOADING -> {
                        showProgress()
                    }
                    ApisResponse.COMPLETED -> {
                        hideProgress()
                    }
                }
            })
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun moveToNextScreen() {
        val action = PurposeFragmentDirections.actionPurposeFragmentToSearchBankerFragment()
        findNavController().navigate(action)
    }

    override fun internetConnected() {

    }

}