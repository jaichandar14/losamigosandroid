package com.bpmlinks.vbank.ui.contact

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentContactBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_contact.*
import kotlinx.android.synthetic.main.fragment_faq.tool_bar
import javax.inject.Inject


class ContactFragment : BaseFragment<FragmentContactBinding, ContactViewModel>(),
    View.OnClickListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): ContactViewModel? =
        ViewModelProvider(this, factory).get(ContactViewModel::class.java)

    override fun getBindingVariable(): Int = BR.contactVM

    override fun getContentView(): Int = R.layout.fragment_contact

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
        txt_mail.setOnClickListener(this)
        txt_call.setOnClickListener(this)
    }

    override fun internetConnected() {

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.txt_call -> {
              call(txt_call.text.toString())
            }
            R.id.txt_mail -> {
                sendEmail(toMail = txt_mail.text.toString())
            }
        }
    }

    private fun sendEmail(toMail: String, subject: String = "", message: String = "") {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.type = "text/plain"
        emailIntent.putExtra(Intent.EXTRA_EMAIL  , arrayOf(toMail))
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.lbl_choose_email)))
        } catch (e: Exception) {
            e.message?.let { showToast(it) }
        }
    }

    private fun call(number : String){
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("tel:${number}")
        startActivity(intent)
    }
}