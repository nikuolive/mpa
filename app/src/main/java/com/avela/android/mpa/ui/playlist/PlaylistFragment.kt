package com.avela.android.mpa.ui.playlist

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import com.avela.android.mpa.databinding.FragmentPlaylistListBinding
import com.avela.android.mpa.adapters.PlaylistAdapter
import com.avela.android.mpa.ui.placeholder.PlaceholderContent
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * A fragment representing a list of Items.
 */
@AndroidEntryPoint
class PlaylistFragment : Fragment() {

    private var columnCount = 1

    private lateinit var binding: FragmentPlaylistListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlaylistListBinding.inflate(inflater, container, false)

        // Set the adapter
        with(binding.list) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            Timber.d(PlaceholderContent.ITEMS.toString())
            adapter = PlaylistAdapter(PlaceholderContent.ITEMS)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback {
            parentFragmentManager.popBackStack()
            this.remove()
        }
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            PlaylistFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}