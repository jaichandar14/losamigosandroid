package com.bpmlinks.vbank.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider

abstract class BaseDialogFragment<out V : ViewDataBinding, out T : BaseViewModel> :
    DialogFragment() {

    private var mDataBinding: V? = null
    private var mViewModel: T? = null

    abstract fun getViewModel(): T?

    abstract fun getBindingVariable(): Int

    abstract fun getContentView(): Int


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mDataBinding = DataBindingUtil.inflate(inflater, getContentView(), container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
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
}