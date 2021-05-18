package com.bpmlinks.vbank.ui.faq

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bpmlinks.vbank.BR
import com.bpmlinks.vbank.R
import com.bpmlinks.vbank.base.BaseFragment
import com.bpmlinks.vbank.databinding.FragmentFaqBinding
import com.bpmlinks.vbank.model.ApisResponse
import com.bpmlinks.vbank.model.FAQListResponse
import com.bpmlinks.vbank.model.FaqListItem
import com.bpmlinks.vbank.ui.faq.adapter.FAQAdapter
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_faq.*
import javax.inject.Inject


class FAQFragment : BaseFragment<FragmentFaqBinding, FAQViewModel>(), View.OnClickListener,
    SearchView.OnQueryTextListener {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    override fun getViewModel(): FAQViewModel? =
        ViewModelProvider(this, factory).get(FAQViewModel::class.java)

    override fun getBindingVariable(): Int = BR.faqVM

    override fun getContentView(): Int = R.layout.fragment_faq

    private var adapter: FAQAdapter? = null

    private var lastClickedItem: Int = -1
    private var faqList: ArrayList<FaqListItem>? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = ContextCompat.getColor(requireContext(),R.color.white)
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable: Drawable? =
            ContextCompat.getDrawable(requireActivity(), R.drawable.recycler_view_border)
        drawable?.let {
            itemDecoration.setDrawable(it)
        }
        //recycler_view.addItemDecoration(itemDecoration)
        val animator: ItemAnimator? = recycler_view?.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }

        tool_bar.setNavigationOnClickListener { findNavController().popBackStack() }
        init()
    }

    private fun init() {
        if (checkInternetAvailable()) {
            searchView.setOnQueryTextListener(this)
            getViewModel()?.getFAQList()?.observe(viewLifecycleOwner, observeFAQ)
        }
    }

    private val observeFAQ = Observer<ApisResponse<FAQListResponse>> { apiResponse ->
        when (apiResponse) {
            is ApisResponse.Success -> {
                faqList = apiResponse.response.data.faqList
                setAdapter(faqList)
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

    private fun setAdapter(sampleList: ArrayList<FaqListItem>?) {
        if (sampleList?.isNotEmpty() == true) {
            txtNoFAQAvailable.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
            adapter = FAQAdapter(sampleList, this)
            recycler_view.adapter = adapter
        } else {
            showNoFaqAvailable()
        }
    }

    private fun showNoFaqAvailable() {
        recycler_view.visibility = View.GONE
        txtNoFAQAvailable.visibility = View.VISIBLE
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgExpand -> {
                val position = v.getTag(R.id.imgExpand) as Int
                expandFaqSubTitle(position)
            }
        }
    }

    private fun expandFaqSubTitle(position: Int) {
       try {
            if (lastClickedItem != position) {
                if (lastClickedItem != -1) {
                    adapter?.mFilteredList?.get(lastClickedItem)?.isExpended = false
                    adapter?.notifyItemChanged(lastClickedItem)
                }
                adapter?.mFilteredList?.get(position)?.isExpended = true
                adapter?.notifyItemChanged(position)
                lastClickedItem = position
            } else {
                adapter?.mFilteredList?.get(position)?.isExpended =  adapter?.mFilteredList?.get(position)?.isExpended != true
                adapter?.notifyItemChanged(position)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        lastClickedItem = -1
        adapter?.filter?.filter(newText)
        return true
    }

}