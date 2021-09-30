package com.avela.android.mpa.ui.browsedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.avela.android.mpa.databinding.FragmentBrowseDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseDetailFragment : Fragment() {

    private val viewModel: BrowseDetailViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBrowseDetailBinding.inflate(inflater, container, false)
//        val adapter = BrowseLibraryAdapter()
//        binding.detailList.adapter = adapter
//        subscribeUi(adapter, binding)
        return binding.root
    }

//    private fun subscribeUi(adapter: BrowseLibraryAdapter, binding: FragmentBrowseDetailBinding) {
//        viewModel.list.observe(viewLifecycleOwner) {
//            adapter.submitList(it)
//        }
//    }
}