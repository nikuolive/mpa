package com.avela.android.mpa.ui.browselibrary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.avela.android.mpa.adapters.BrowseLibraryAdapter
import com.avela.android.mpa.databinding.FragmentBrowseLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseLibraryFragment(private val listToShow: String = "") : Fragment() {

    private val browseLibraryViewModel: BrowseLibraryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentBrowseLibraryBinding.inflate(inflater, container, false)
        if (listToShow == "") {
            val adapter = BrowseLibraryAdapter {
                browseLibraryViewModel.getSongList(it)
            }
            binding.libraryList.adapter = adapter
            subscribeUi(adapter, binding)
        }
        return binding.root
    }

    private fun subscribeUi(adapter: BrowseLibraryAdapter, binding: FragmentBrowseLibraryBinding) {
        browseLibraryViewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}