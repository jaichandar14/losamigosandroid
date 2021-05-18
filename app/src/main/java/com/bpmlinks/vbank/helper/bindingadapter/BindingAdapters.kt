package com.bpmlinks.vbank.helper.bindingadapter

import android.util.DisplayMetrics
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bpmlinks.vbank.model.BranchDtosItem
import java.lang.StringBuilder

object BindingAdapters {

    @BindingAdapter("screenHeightDividedBy")
    @JvmStatic
    fun setLayoutHeight(view: View, dividedBy: Double) {
        val layoutParams = view.layoutParams
        val displayMetrics: DisplayMetrics = view.context.resources.displayMetrics
        val height = displayMetrics.heightPixels / dividedBy
        layoutParams.height = height.toInt()
        view.layoutParams = layoutParams
    }


    @BindingAdapter("appendAddress")
    @JvmStatic
    fun appendAddress(view: AppCompatTextView,branchesItem: BranchDtosItem) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(branchesItem.addressLineOne).append(",").append("\t")
            .append(branchesItem.city).append(",").append("\t")
            .append(branchesItem.zipCode)

        view.text = stringBuilder.toString()
    }

    @BindingAdapter("selected")
    @JvmStatic
    fun setItem(pAppCompatSpinner: AppCompatSpinner, value: String?) {
        val position = (pAppCompatSpinner.adapter as ArrayAdapter<String>).getPosition(value)
        pAppCompatSpinner.setSelection(position, false)
    }

    @BindingAdapter(value = ["selectedAttrChanged"])
    @JvmStatic
    fun setListener(
        appCompatSpinner: AppCompatSpinner,
        inverseBindingListener: InverseBindingListener
    ) {
        val listener: AdapterView.OnItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                inverseBindingListener.onChange()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                inverseBindingListener.onChange()
            }
        }
        appCompatSpinner.onItemSelectedListener = listener
    }

    @InverseBindingAdapter(attribute = "selected", event = "selectedAttrChanged")
    @JvmStatic
    fun getItem(appCompatSpinner: AppCompatSpinner): String {
        return appCompatSpinner.selectedItem.toString()
    }

}