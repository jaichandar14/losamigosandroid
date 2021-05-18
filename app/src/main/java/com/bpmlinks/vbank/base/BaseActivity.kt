package com.bpmlinks.vbank.base

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.bpmlinks.vbank.extension.isNetConnected
import com.google.android.gms.maps.OnMapReadyCallback

abstract class BaseActivity<out T : ViewDataBinding,  out V : BaseViewModel>: AppCompatActivity()
     {

    private var mViewDataBinding: T? = null
    private var mViewModel: V? = null

    abstract fun getContentView(): Int

    abstract fun getViewModel(): V?

    fun getViewDataBinding(): T? {
        return mViewDataBinding
    }

    abstract fun getBindingVariable(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
    }

    private fun performDataBinding(){
        mViewDataBinding = DataBindingUtil.setContentView(this, getContentView())
        mViewModel = getViewModel()
        mViewDataBinding?.setVariable(getBindingVariable(), mViewModel)
        mViewDataBinding?.executePendingBindings()
    }

    fun pushFragment(fragment: Fragment, frameContainer: Int, addToBackStack: Boolean = false, tag: String){
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(frameContainer, fragment)
        if (addToBackStack) fragmentTransaction.addToBackStack(tag)
        fragmentTransaction.commit()
    }

    fun showInfoAlert(message: String){
        val builder = AlertDialog.Builder(this@BaseActivity)
        builder.setMessage(message)
        builder.setPositiveButton("OKAY") { dialogInterface, i ->
            dialogInterface.cancel()
        }
        builder.setCancelable(false)
        builder.create().show()
    }

    fun checkInternetAvailable(): Boolean{
        val hasInternet = this.isNetConnected()
        if (!hasInternet){
            showInfoAlert("Internet error")
            return false
        }else{
            return true
        }
    }

    abstract fun showProgress()

    abstract fun hideProgress()
}