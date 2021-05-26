package com.bpmlinks.vbank.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentLoginBinding
import com.bpmlinks.vbank.extension.isEmailValid
import com.bpmlinks.vbank.extension.isValidMobileNumber
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.helper.AppPreferences
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.ServiceType
import com.bpmlinks.vbank.ui.login.adapter.MasterServiceAdapter
import com.vbank.vidyovideoview.connector.MeetingParams
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bottom_menu.*
import kotlinx.android.synthetic.main.login_bottom.*
import java.util.*
import javax.inject.Inject


class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>(), View.OnClickListener
          //MasterServiceAdapter.OnServiceClickListener
    {


var meetingParams = MeetingParams()
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private val adapter = MasterServiceAdapter()

    override fun getViewModel(): LoginViewModel? =
        ViewModelProvider(this, factory).get(LoginViewModel::class.java)

    override fun getBindingVariable(): Int = BR.loginVMLayout

    override fun getContentView(): Int = R.layout.login_bottom

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.status_bar_colore)
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        lnrTop.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, (height / 3).toInt())

        init()
    }

    private fun init() {
        var loginAlready = requireActivity().applicationContext.getSharedPreferences("MyUser", Context.MODE_PRIVATE)
        var mailId = loginAlready.getString("MailId","")
        Log.d("TAG", "isLogged: ${mailId}")
        if (!mailId.isNullOrEmpty()){
            moveToNextScreen(meetingParams?.meetingTime!!)
        }else {

//        tv_faq.setOnClickListener(this)
            tv_contact.setOnClickListener(this)
//        tv_settings.setOnClickListener(this)
            login_btn.setOnClickListener(this)


            if (checkInternetAvailable()) {
                getViewModel()?.getServiceType()?.observe(viewLifecycleOwner, serviceTypeObserver)
            }
            //adapter.setOnClickListener(this)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(v: View?) {
        when (v?.id) {
//            R.id.tv_faq -> {
//                showFaqScreen()
//            }
//            R.id.tv_settings -> {
//                showSettingsScreen()
//            }
            R.id.tv_contact -> {
                showContactScreen()
            }
            R.id.login_btn -> {

                apicall()

            }}}

    private fun apicall() {

        val reqString = ("Device name :"+Build.DEVICE
                + " Device version1:" + Build.ID
                + " Device version1:" + Build.MANUFACTURER
                + " Device model:" + Build.MODEL+
                "device buildnumber"+Build.VERSION.BASE_OS
                + " Device version2:" + Build.VERSION.RELEASE
                + " Device version3:" + Build.VERSION.INCREMENTAL
                + " Device version:4" + Build.FINGERPRINT
                + " Device version:5" + Build.VERSION.PREVIEW_SDK_INT
                + " Device version:6" + Build.VERSION.SDK_INT
                + "Device version code: " + VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name)
        Log.d("reqString","${reqString}")


        if ((et_name.text.isNullOrEmpty() && et_email.text.isNullOrEmpty() && et_mobile_number.text.isNullOrEmpty()) || (!et_name.text.isNullOrEmpty() && et_email.text.isNullOrEmpty() && et_mobile_number.text.isNullOrEmpty())) {
            showToast(getString(R.string.mandatory_error))
        } else if (!et_name.text.isNullOrEmpty() && !et_mobile_number.text.isNullOrEmpty() && et_email.text.isNullOrEmpty()) {
            showToast(getString(R.string.email_error))
        } else if (!et_name.text.isNullOrEmpty() && !et_email.text.isNullOrEmpty() && et_mobile_number.text.isNullOrEmpty()) {
            showToast(getString(R.string.mobile_error))
        } else if (!et_email.text.isNullOrEmpty() && !et_email.text.toString()
                .isEmailValid(et_email.text.toString())
        ) {
            showToast(getString(R.string.email_error))
        } else if (!et_mobile_number.text.isNullOrEmpty() && !et_mobile_number.text.toString()
                .isValidMobileNumber(et_mobile_number.text.toString())
        ) {
            showToast(getString(R.string.mobile_error))
        } else {
            getViewModel()?.userInput?.customerFirstName = et_name.text.toString()
            getViewModel()?.userInput?.emailId = et_email.text.toString()
            getViewModel()?.userInput?.mobileNumber = et_mobile_number.text.toString()
            getViewModel()?.userInput?.customerPurposeDto?.masterServiceTypeKeyNb =
                adapter.serviceList.get(0).masterServiceTypeKeyNb
            //val action = LoginFragmentDirections.actionLoginFragmentToPurposeFragment( adapter.serviceList.get(0), getViewModel()?.userInput)

            val androidID = UUID.randomUUID().toString()
            getViewModel()?.userInput?.deviceOS = AppConstants.DEVICE_OS
            getViewModel()?.userInput?.hardwareId = androidID
            getViewModel()?.userInput?.pushToken = AppPreferences.getInstance()
                .getStringValue(requireActivity(), AppPreferences.FCM_TOKEN)

            getViewModel()?.userInput?.osVersion = Build.VERSION.RELEASE
            getViewModel()?.userInput?.buildNumber = Build.VERSION.INCREMENTAL
            getViewModel()?.userInput?.modelName = Build.MODEL
            getViewModel()?.userInput?.deviceName = Build.MANUFACTURER+"_"+Build.DEVICE

            isLogged(et_email.text.toString())

            getViewModel()?.newCustomer()
                ?.observe(viewLifecycleOwner, Observer { apiResponse ->
                    when (apiResponse) {
                        is ApisResponse.Success -> {
                            var sharedPref =
                                    activity?.getPreferences(Context.MODE_PRIVATE)
                                            ?: return@Observer
                            with(sharedPref.edit()) {
                                apiResponse.response.data.id?.toInt()?.let { id ->
                                    putInt(AppConstants.CUSTOMER_KEY_NB, id)
                                }
                                commit()
                            }
                            with(sharedPref.edit()) {
                                getViewModel()?.userInput?.customerFirstName?.let { name ->
                                    putString(AppConstants.CUSTOMER_NAME, name)
                                }

                                commit()
                            }
                            meetingParams.meetingTime =apiResponse.response.data.schdeuleTime

                            apiResponse.response.data.schdeuleTime?.let { moveToNextScreen(it) }
                        }
                        is ApisResponse.CustomError -> {
                            showToast(apiResponse.message)

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


    }

        private fun moveToNextScreen(timesheduled: String) {

            val action = LoginFragmentDirections.actionLoginFragmentToInspectionFragment(timesheduled)
            findNavController().navigate(action)
        }

        private fun showFaqScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToFAQFragment()
        findNavController().navigate(action)
    }

    private fun showContactScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToContactFragment()
        findNavController().navigate(action)
    }

    private fun showSettingsScreen() {
        val action = LoginFragmentDirections.actionLoginFragmentToSettingsFragment()
        findNavController().navigate(action)
    }

    private val serviceTypeObserver = Observer<ApisResponse<ServiceType>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                apiResponse.response.data.masterServiceTypeDtos?.let { list ->
                    adapter.refreshItems(list)
                }
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
    }

    private fun showProgress() {
        progress_bar.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress_bar.visibility = View.GONE
    }


    override fun internetConnected() {
        init()
    }

        fun isLogged(email :String){
            var sharedPreferences = activity?.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
            var editor = sharedPreferences?.edit()
            editor?.putString("MailId", email)
            editor?.commit()

            var getSharedPreferences = requireActivity().applicationContext.getSharedPreferences("MyUser",Context.MODE_PRIVATE)
           var mailId = getSharedPreferences?.getString("MailId","")
            Log.d("TAG", "isLogged: ${mailId}")

        }

}