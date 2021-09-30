package com.avela.android.mpa.ui.nowplaying

import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.avela.android.mpa.R
import com.avela.android.mpa.databinding.FragmentPlayerBigBinding
import com.avela.android.mpa.ui.playlist.PlaylistFragment
import com.bumptech.glide.Glide
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlayerBigFragment : Fragment() {
    private lateinit var binding: FragmentPlayerBigBinding
    private val viewModel: NowPlayingViewModel by viewModels()
    private var currentAudioDuration: Long = 0L
    private var blockUpdate: Boolean = false
    private var currentMediaButtonRes = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPlayerBigBinding.inflate(inflater, container, false)

        binding.seekbar.addOnChangeListener { slider, value, fromUser ->
            binding.position.text = viewModel.progressToMSS(value)
//            Timber.d("change ${slider.value}")
        }
        binding.seekbar.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                blockUpdate = true
//                Timber.d("Start ${slider.value}")
            }

            override fun onStopTrackingTouch(slider: Slider) {
//                Timber.d("stop ${slider.value}")
                viewModel.setProgress(slider.value)
                blockUpdate = false
            }

        })
        binding.playlistSwitcher.setOnClickListener {
            parentFragmentManager.commit {
                add<PlaylistFragment>(R.id.player_fragment)
                setReorderingAllowed(true)
                addToBackStack("name") // name can be null
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mediaMetadata.observe(viewLifecycleOwner,
            { mediaItem -> updateUI(view, mediaItem) })

        viewModel.mediaButtonRes.observe(viewLifecycleOwner,
            { res ->
                if (res != currentMediaButtonRes) {
                    currentMediaButtonRes = res
                    binding.playPauseButton.apply {
                        (drawable as AnimatedVectorDrawable).registerAnimationCallback(object : Animatable2
                        .AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                super.onAnimationEnd(drawable)
                                setImageResource(res)
                            }
                        })
                        (drawable as AnimatedVectorDrawable).start()
                    }
                }
            })

        binding.playPauseButton.setOnClickListener {
            viewModel.mediaMetadata.value?.let { viewModel.playMediaId(it.id) }
        }

        binding.prevButton.setOnClickListener {
            viewModel.playPreviousSong()
        }

        binding.nextButton.setOnClickListener {
            viewModel.playNextSong()
        }

        viewModel.mediaPosition.observe(viewLifecycleOwner, {
            if (!blockUpdate) {
                binding.position.text = NowPlayingViewModel.NowPlayingMetadata.timestampToMSS(requireContext(), it.first)
                binding.seekbar.value = it.second
            }
        })
    }

//    private fun subscribeUi(adapter: BrowseLibraryAdapter, binding: FragmentNowPlayingBinding) {
//        viewModel.list.observe(viewLifecycleOwner) {
//            adapter.submitList(it)
//        }
//    }

    private fun updateUI(view: View, metadata: NowPlayingViewModel.NowPlayingMetadata) = with(binding) {
        when {
            metadata.albumArtUri != Uri.EMPTY -> {
                //            albumArt.setImageResource(R.drawable.ic_music_art)
                Glide.with(view)
                    .load(metadata.albumArtUri)
                    .into(coverBig)
            }
            metadata.albumArt != null -> {
                Glide.with(view)
                    .load(metadata.albumArt)
                    .into(coverBig)
            }
            else -> {
                Glide.with(view)
                    .load(R.drawable.ic_music_art)
                    .into(coverBig)
            }
        }

        songNameBig.text = metadata.title
//        subtitle.text = metadata.subtitle
        duration.text = metadata.duration
    }
}