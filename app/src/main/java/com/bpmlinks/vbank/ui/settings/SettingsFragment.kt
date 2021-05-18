package com.bpmlinks.vbank.ui.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentSettingsBinding
import kotlinx.android.synthetic.main.fragment_faq.*

class SettingsFragment : BaseFragment<FragmentSettingsBinding,SettingsViewModel>() {
    override fun getViewModel(): SettingsViewModel? =
        ViewModelProvider(this).get(SettingsViewModel::class.java)

    override fun getBindingVariable(): Int = BR.settingVM

    override fun getContentView(): Int = R.layout.fragment_settings
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    override fun internetConnected() { }
}