package com.bpmlinks.vbank.base

import android.app.Dialog
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.extension.isNetConnected
import com.vbank.vidyovideoview.broadcast.ConnectivityReceiver
import com.vbank.vidyovideoview.helper.BundleKeys

abstract class BaseFragment<out V : ViewDataBinding, out T : BaseViewModel> : Fragment(),
    ConnectivityReceiver.ConnectivityReceiverListener {

    private var mDataBinding: V? = null
    private var mViewModel: T? = null
    private var noInternetDialog: Dialog? = null

    abstract fun getViewModel(): T?

    abstract fun getBindingVariable(): Int

    abstract fun getContentView(): Int

    abstract fun internetConnected()

    private var myReceiver: ConnectivityReceiver = ConnectivityReceiver()

    private var isDisconnected : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBinding = DataBindingUtil.inflate(inflater, getContentView(), container, false)
        return mDataBinding?.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        performDataBinding()
    }

    private fun performDataBinding() {
        getViewModel()?.let { viewModel ->
            mViewModel = ViewModelProvider(this).get(viewModel::class.java)
            mDataBinding?.setVariable(getBindingVariable(), mViewModel)
            mDataBinding?.lifecycleOwner = this
            mDataBinding?.executePendingBindings()
        }
    }

    fun checkInternetAvailable(): Boolean {
        val hasInternet = requireActivity().isNetConnected()
        return if (!hasInternet) {
            showFullScreenDialog()
            false
        } else {
            true
        }
    }

    fun showMessageAlert(message: String) {
        val dialog = AlertDialog.Builder(requireActivity())
        dialog.setMessage(message)
        dialog.setPositiveButton("OKAY") { dialog, _ -> dialog?.cancel() }
        dialog.create().show()
    }

    fun showToast(msg: String) {
        Toast.makeText(activity?.applicationContext, msg, Toast.LENGTH_LONG).show()
    }

    private fun showFullScreenDialog() {
        noInternetDialog = Dialog(requireContext(), android.R.style.Theme)
        noInternetDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        noInternetDialog?.setContentView(R.layout.dialog_no_internet)
        noInternetDialog?.setCancelable(false)
        val window = noInternetDialog?.window
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val btn: AppCompatButton? = noInternetDialog?.findViewById(R.id.btn_retry)
        btn?.setOnClickListener {
            if (checkInternetAvailable()) {
                noInternetDialog?.dismiss()
                internetConnected()
            } else {
                showToast(getString(R.string.internet_connection))
            }
        }
        noInternetDialog?.show()
    }

    private fun setConnectivityListener(listener: ConnectivityReceiver.ConnectivityReceiverListener) {
        ConnectivityReceiver.connectivityReceiverListener = listener
    }

    override fun onPause() {
        requireActivity().unregisterReceiver(myReceiver)
        super.onPause()
    }

    override fun onResume() {
        setConnectivityListener(this)
        val intentFilter = IntentFilter()
        intentFilter.addAction(BundleKeys.CONNECTING_CHANGE)
        requireActivity().registerReceiver(myReceiver, intentFilter)
        super.onResume()
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (isConnected) {
            if (isDisconnected){
                noInternetDialog?.dismiss()
                internetConnected()
            }
        }else{
            isDisconnected = true
        }
    }
}