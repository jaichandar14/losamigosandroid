package com.bpmlinks.vbank.ui.choosebanker

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.vbank.vidyovideoview.broadcast.ConnectivityReceiver
import com.bpmlinks.vbank.databinding.FragmentChooseBankerBinding
import com.bpmlinks.vbank.helper.AppConstants
import com.bpmlinks.vbank.model.BankerDtosItem
import com.bpmlinks.vbank.model.CallbackRequest
import com.bpmlinks.vbank.twilio.VideoActivity
import com.bpmlinks.vbank.ui.choosebanker.adapter.ChooseBankerAdapter
import com.vbank.vidyovideoview.connector.UserDataParams
import com.vbank.vidyovideoview.helper.BundleKeys
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_choose_banker.*
import javax.inject.Inject

class ChooseBankerFragment : BaseFragment<FragmentChooseBankerBinding, ChooseBankerViewModel>(),
    View.OnClickListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): ChooseBankerViewModel? =
        ViewModelProvider(this, factory).get(ChooseBankerViewModel::class.java)

    override fun getBindingVariable(): Int = BR.chooseBankerVM

    override fun getContentView(): Int = R.layout.fragment_choose_banker

    private val navArgs by navArgs<ChooseBankerFragmentArgs>()
    private var adapter: ChooseBankerAdapter? = null
    private var myReceiver: ConnectivityReceiver? = null

    companion object {
        const val REMOVE_CURRENT_FRAGMENT = 111
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        init()
    }

    private fun init() {
        myReceiver = ConnectivityReceiver()
        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable: Drawable? =
            ContextCompat.getDrawable(requireActivity(), R.drawable.recycler_view_border)
        drawable?.let {
            itemDecoration.setDrawable(it)
        }
        recycler_view.addItemDecoration(itemDecoration)
        tool_bar.setOnClickListener(this)
        initRecyclerAdapter()
        getViewModel()?.branchInfoDtosItem?.value = navArgs.branchesInfo
        updateBranches(navArgs.branchesInfo?.bankerDtos)
    }

    private fun initRecyclerAdapter() {
        adapter = ChooseBankerAdapter(this)
        recycler_view.adapter = adapter
    }

    private fun updateBranches(branchInfoItem: List<BankerDtosItem>?) {
        adapter?.setBankerList(branchInfoItem)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_call_back -> {
                val branchItem = v.getTag(R.id.btn_call_back) as BankerDtosItem
                callBackDialog(branchItem)
            }
            R.id.btn_connecting_now -> {
                val branchItem = v.getTag(R.id.btn_connecting_now) as BankerDtosItem
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connectMeeting(branchItem)
                } else {
                    val intent = Intent(activity, VideoActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun connectMeeting(branchItem: BankerDtosItem) {
        val connectivityManager =
            requireActivity().getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val downSpeed = nc?.linkDownstreamBandwidthKbps
        if (checkInternetAvailable()) {
            downSpeed?.let {
                branchItem.currentSpeed = downSpeed / (1024 * 128)
                if (branchItem.currentSpeed >= 3) {
                    val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                    val customerKeyNb = sharedPref.getInt(AppConstants.CUSTOMER_KEY_NB, 0)
                    val customerName = sharedPref.getString(AppConstants.CUSTOMER_NAME, "")

                    val userDataParams = UserDataParams(
                        customerKeyNb = customerKeyNb,
                        branchKeyNb = branchItem.branchKeyNb,
                        brankerKeyNb = branchItem.bankerKeyNb,
                        bankerName = branchItem.bankerName,
                        displayName = customerName
                    )

                    val intent = Intent(requireActivity(), VideoActivity::class.java)
                    intent.putExtra(BundleKeys.UserDataParams, userDataParams)
                    startActivityForResult(intent, REMOVE_CURRENT_FRAGMENT)
                } else {
                    val action =
                        ChooseBankerFragmentDirections.actionChooseBankerFragmentToNetworkSpeedFragment(
                            branchItem
                        )
                    findNavController().navigate(action)
                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REMOVE_CURRENT_FRAGMENT) {
            findNavController().popBackStack()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun callBackDialog(branchItem: BankerDtosItem) {
        val callbackRequest = CallbackRequest()
        callbackRequest.bankerKeyNb = branchItem.bankerKeyNb
        callbackRequest.branchKeyNb = branchItem.branchKeyNb
        val action =
            ChooseBankerFragmentDirections.actionChooseBankerFragmentToCallbackDialogFragment(
                callbackRequest
                , getViewModel()?.branchInfoDtosItem?.value
            )
        findNavController().navigate(action)
    }

    override fun internetConnected() {

    }

}