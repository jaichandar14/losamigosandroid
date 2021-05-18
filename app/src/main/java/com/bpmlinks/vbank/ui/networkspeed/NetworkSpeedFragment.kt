package com.bpmlinks.vbank.ui.networkspeed

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentNetworkSpeedBinding
import com.bpmlinks.vbank.extension.textColor
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.twilio.VideoActivity
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.helper.BundleKeys
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_network_speed.*
import javax.inject.Inject

class NetworkSpeedFragment : BaseFragment<FragmentNetworkSpeedBinding, NetworkSpeedViewModel>() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): NetworkSpeedViewModel? =
        ViewModelProvider(this, factory).get(NetworkSpeedViewModel::class.java)

    override fun getBindingVariable(): Int = BR.networkSpeedVM

    override fun getContentView(): Int = R.layout.fragment_network_speed
    private val navArgs by navArgs<NetworkSpeedFragmentArgs>()

    companion object{
        const val REMOVE_CURRENT_FRAGMENT = 111
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        getViewModel()?.bankInfo = navArgs.BankerInfoItem
        init()
    }

    private fun init() {
        speed_view.withTremble = false
        speed_view.currentSpeed = 0f
        getViewModel()?.bankInfo?.currentSpeed?.toFloat()?.let { currentSpeed->
            speed_view.speedTo(currentSpeed)
            if (currentSpeed <= 1.5) {
                tv_low.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.rectangle_orange_curve)
                tv_low.textColor = ContextCompat.getColor(requireActivity(), R.color.color_grey5)
            } else if (currentSpeed >= 1.5 && currentSpeed < 3.0) {
                tv_medium.background =
                    ContextCompat.getDrawable(requireActivity(), R.drawable.rectangle_orange_curve)
                tv_medium.textColor = ContextCompat.getColor(requireActivity(), R.color.color_grey5)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_description.text = (Html.fromHtml(
                        getString(R.string.connection_weak_medium),
                        Html.FROM_HTML_MODE_LEGACY
                    ))
                } else {
                    tv_description.text = Html.fromHtml(getString(R.string.connection_weak_medium))
                }
                btn_next.text = getString(R.string.start_video_call)
            }

            btn_next.setOnClickListener {
                if (currentSpeed <= 1.5) {
                    findNavController().popBackStack()
                } else if (currentSpeed >= 1.5 && currentSpeed < 3.0) {
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val customerKeyNb = sharedPref.getInt(AppConstants.CUSTOMER_KEY_NB, 0)
                    val customerName = sharedPref.getString(AppConstants.CUSTOMER_NAME, "")

                    val userDataParams = UserDataParams(
                        customerKeyNb = customerKeyNb, branchKeyNb = getViewModel()?.bankInfo?.branchKeyNb,
                        brankerKeyNb = getViewModel()?.bankInfo?.bankerKeyNb,
                        bankerName = getViewModel()?.bankInfo?.bankerName,displayName = customerName
                    )

                    val intent = Intent(requireActivity(), VideoActivity::class.java)
                    intent.putExtra(BundleKeys.UserDataParams, userDataParams)
                    startActivityForResult(intent, REMOVE_CURRENT_FRAGMENT)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == REMOVE_CURRENT_FRAGMENT) {
                findNavController().popBackStack(R.id.searchBankerFragment,false)
            }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun internetConnected() {

    }

}