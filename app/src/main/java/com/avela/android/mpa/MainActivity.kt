package com.avela.android.mpa

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.Constraints
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.avela.android.mpa.services.MPDService
import com.avela.android.mpa.ui.nowplaying.NowPlayingViewModel
import com.avela.android.mpa.viewmodels.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    private val viewModel: MainActivityViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val fragmentContainerView = findViewById<FragmentContainerView>(R.id.now_playing_fragment)
        val parent = findViewById<CoordinatorLayout>(R.id.container)
        val child = findViewById<FragmentContainerView>(R.id.nav_host_fragment)
        val bottomSheetBehavior = BottomSheetBehavior.from(fragmentContainerView)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        fragmentContainerView.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
            }
        }
        bottomSheetBehavior.blocksInteractionBelow(parent, child)
//        Intent(this, MPDService::class.java).also { intent ->
//            startService(intent)
//        }
        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    fadeView(findViewById(R.id.now_playing_small), 1f)
                    fadeView(findViewById(R.id.player_fragment), 0f)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    fadeView(findViewById(R.id.now_playing_small), 0f)
                    fadeView(findViewById(R.id.player_fragment), 1f)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                fadeView(findViewById(R.id.now_playing_small), slideOffset)
                fadeView(findViewById(R.id.player_fragment), 1 - slideOffset)
            }
        })

        viewModel.rootMediaId.observe(this,
            { rootMediaId ->
                Timber.d(rootMediaId)
            })
    }

    fun fadeView(view: View, offset: Float) {
        val alpha: Float = 1 - offset
        view.alpha = alpha
        view.visibility = if (alpha == 0f) View.GONE else View.VISIBLE
    }
}