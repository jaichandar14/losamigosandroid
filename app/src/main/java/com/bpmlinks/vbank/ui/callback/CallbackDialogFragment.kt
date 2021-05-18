package com.bpmlinks.vbank.ui.callback

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseDialogFragment
import com.bpmlinks.vbank.databinding.DialogTimeSlotBinding
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.model.*
import com.bpmlinks.vbank.ui.callback.adapter.SpinnerAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.dialog_time_slot.*
import okhttp3.ResponseBody
import javax.inject.Inject


class CallbackDialogFragment : BaseDialogFragment<DialogTimeSlotBinding, CallbackDialogViewModel>(), View.OnClickListener {
    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): CallbackDialogViewModel? =
        ViewModelProvider(this, factory).get(CallbackDialogViewModel::class.java)

    override fun getBindingVariable(): Int = BR.callBackVM

    override fun getContentView(): Int = R.layout.dialog_time_slot

    private val navArgs by navArgs<CallbackDialogFragmentArgs>()
    private var availableTimingMasterDtosItem: List<AvailableTimingMasterDtosItem>? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_Dialog_Alert)

    }

    override fun onStart() {
        super.onStart()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        dialog?.window?.setLayout((width * 0.75).toInt(), FrameLayout.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        btnYes.setOnClickListener(this)
        btnNo.setOnClickListener(this)
        img_spinner_arrow.setOnClickListener(this)
        getViewModel()?.branchDetails?.value = navArgs.BranchDetails
        getViewModel()?.getTimeSlots()?.observe(viewLifecycleOwner, timeSlotObserver)
        getViewModel()?.scheduleCall()?.observe(viewLifecycleOwner, scheduleCallObserver)
        val timeSlotRequest = TimeSlotsRequest(
            navArgs.CallbackRequest.bankerKeyNb,
            navArgs.CallbackRequest.branchKeyNb
        )
        getViewModel()?.branchDetails?.value = navArgs.BranchDetails
        getViewModel()?.setTimeSlotRequest(timeSlotRequest)
    }


    private val timeSlotObserver = Observer<ApisResponse<TimeSlots>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                availableTimingMasterDtosItem = apiResponse.response.data.availableTimingMasterDtos
                createSpinnerAdapter(availableTimingMasterDtosItem)
            }
            is ApisResponse.Error -> {
            }
            ApisResponse.LOADING -> {
                showProgress()
            }
            ApisResponse.COMPLETED -> {
                hideProgress()
            }
        }
    }

    private val scheduleCallObserver = Observer<ApisResponse<ResponseBody>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                showScheduledSuccessScreen()
            }
            is ApisResponse.Error -> {
            }
            ApisResponse.LOADING -> {
                showProgress()
            }
            ApisResponse.COMPLETED -> {
                hideProgress()
            }
        }
    }

    private fun showScheduledSuccessScreen() {
        availableTimingMasterDtosItem?.let {
            val selectedItem = availableTimingMasterDtosItem?.get(spinnerTimeSlot.selectedItemPosition)
            val action =
                CallbackDialogFragmentDirections.actionCallbackDialogFragmentToCallScheduledFragment(selectedItem?.timeSlot,
                    getViewModel()?.branchDetails?.value)
            findNavController().navigate(action)
        }

    }


    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }

    private fun createSpinnerAdapter(timeSlots: List<AvailableTimingMasterDtosItem>?) {
        if (timeSlots?.isNotEmpty() == true) {
            btnNo.text = getString(R.string.all_alert_dialog_cancel)
            lnr_spinner.visibility = View.VISIBLE
            txt_no_slots_available.visibility = View.GONE
            btnYes.visibility = View.VISIBLE
            btnNo.visibility = View.VISIBLE
            val spinnerAdapter = SpinnerAdapter(requireActivity(), timeSlots)
            spinnerTimeSlot.adapter = spinnerAdapter
        }else{
            btnNo.text = getString(R.string.all_alert_dialog_close)
            txt_no_slots_available.visibility = View.VISIBLE
            lnr_spinner.visibility = View.GONE
            btnYes.visibility = View.GONE
            btnNo.visibility = View.VISIBLE

        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnNo -> {
                dismiss()
            }
            R.id.btnYes -> {
                scheduleCall()
            }
            R.id.img_spinner_arrow->{
                spinnerTimeSlot.performClick()
            }
        }
    }

    private fun scheduleCall() {
        availableTimingMasterDtosItem?.let { items ->
            if (items.isNotEmpty()) {
                val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                val customerKeyNb = sharedPref.getInt(AppConstants.CUSTOMER_KEY_NB, 0)
                val callbackRequest = CallbackRequest()
                callbackRequest.bankerKeyNb = navArgs.CallbackRequest.bankerKeyNb
                callbackRequest.branchKeyNb = navArgs.CallbackRequest.branchKeyNb
                callbackRequest.customerKeyNb = customerKeyNb
                val selectedItem = items[spinnerTimeSlot.selectedItemPosition]
                callbackRequest.scheduleTimeKeyNb = selectedItem.scheduleTimeKeyNb
              //  callbackRequest.scheduleDate = selectedItem.timeSlot
                getViewModel()?.setCallBackRequest(callbackRequest)
            }
        }
    }

}