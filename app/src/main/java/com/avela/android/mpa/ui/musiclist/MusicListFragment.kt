package com.avela.android.mpa.ui.musiclist

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.avela.android.mpa.R
import com.google.android.material.tabs.TabLayoutMediator
import com.avela.android.mpa.adapters.ALBUM_PAGE_INDEX
import com.avela.android.mpa.adapters.MusicListPagerAdapter
import com.avela.android.mpa.adapters.PLAYLIST_PAGE_INDEX
import com.avela.android.mpa.databinding.FragmentMusicListPagerBinding
import com.avela.android.mpa.workers.AudioDatabaseWorker
import timber.log.Timber

class MusicListFragment : Fragment() {

    private lateinit var binding: FragmentMusicListPagerBinding

    private lateinit var content: ActivityResultLauncher<Uri>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMusicListPagerBinding.inflate(inflater, container, false)
        val tabLayout = binding.tabs
        val viewPager = binding.viewPager

        viewPager.adapter = MusicListPagerAdapter(this)

        content = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            val cr = context?.applicationContext?.contentResolver

            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (uri != null) {
                cr?.takePersistableUriPermission(uri, takeFlags)
                Timber.d("$uri")
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.toolbar.inflateMenu(R.menu.activity_menu)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.activity_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh -> {
                val request = OneTimeWorkRequestBuilder<AudioDatabaseWorker>()
                    .build()
                WorkManager.getInstance(requireContext().applicationContext)
                    .enqueueUniqueWork("scanning", ExistingWorkPolicy.KEEP, request)
                true
            }

            R.id.select_folder -> {
                content.launch(null)
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }

        }
    }


    private fun getTabIcon(position: Int): Int {
        return when (position) {
            ALBUM_PAGE_INDEX -> R.drawable.ic_home_black_24dp
            PLAYLIST_PAGE_INDEX -> R.drawable.ic_dashboard_black_24dp
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            ALBUM_PAGE_INDEX -> getString(R.string.title_home)
            PLAYLIST_PAGE_INDEX -> getString(R.string.title_browse)
            else -> null
        }
    }
}