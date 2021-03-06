package com.github.s0nerik.music.screens.main.fragments

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.RelativeLayout
import com.github.s0nerik.music.App
import com.github.s0nerik.music.R
import com.github.s0nerik.music.adapters.LocalMusicFragmentsAdapter
import com.github.s0nerik.music.base.BaseBoundFragment
import com.github.s0nerik.music.databinding.FragmentLocalMusicBinding
import com.github.s0nerik.music.events.EFabActionAvailable
import com.github.s0nerik.music.events.EPlaybackStateChanged
import com.github.s0nerik.music.players.PlayerController
import com.github.s0nerik.rxbus.RxBus
import com.jakewharton.rxbinding.support.v4.view.RxViewPager
import com.jakewharton.rxbinding.widget.RxTextView
import com.mypopsy.drawable.SearchCrossDrawable
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import kotlinx.android.synthetic.main.fragment_local_music.*
import org.jetbrains.anko.onClick
import rx.Subscription

class LocalMusicFragment : BaseBoundFragment<FragmentLocalMusicBinding>() {
    override val layoutId = R.layout.fragment_local_music

//    @Inject
//    lateinit var mainPrefs: MainPrefs

    private val radialEqualizerViewSubscription: Subscription? = null
    private val localPlayerServiceIntent: Intent? = null
    private var streamPlayerServiceIntent: Intent? = null
    private var canShowFab = true

    private var currentFragment: Fragment? = null
        set(fragment) {
            field = fragment
            if (fragment is SortableFragment) {
                toolbar.menu.findItem(R.id.action_sort).isVisible = true
            } else {
                toolbar.menu.findItem(R.id.action_sort).isVisible = false
            }
        }

//    private val nowPlayingFragment by lazy {
//        childFragmentManager.findFragmentById(R.id.nowPlayingFragment) as NowPlayingFragment
//    }

    private var fabAction = {}

    private lateinit var adapter: LocalMusicFragmentsAdapter
//    val adapter = LocalMusicFragmentsAdapter(childFragmentManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.comp.inject(this)
//        streamPlayerServiceIntent = Intent(activity, StreamPlayerService)
//        activity.stopService(streamPlayerServiceIntent)
        initEventHandlers()
    }

    private fun initEventHandlers() {
        RxBus.on(EPlaybackStateChanged::class.java).bindToLifecycle(this).subscribe { onEvent(it) }
        RxBus.on(EFabActionAvailable::class.java).bindToLifecycle(this).subscribe { onEvent(it) }
//        RxBus.on(PlaybackPausedEvent).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(EPlaybackProgress).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(ShouldStartArtistInfoActivity).bindToLifecycle(this).subscribe(::onEvent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("MISSING_DEPENDENCY_CLASS")
        binding.playerController = PlayerController(App.comp.getLocalPlayer())

        initToolbar()

        val adapter = LocalMusicFragmentsAdapter(childFragmentManager)

        pager.offscreenPageLimit = 3
        pager.adapter = adapter
        tabs.setupWithViewPager(pager)

        fab.onClick { fabAction() }

        RxViewPager.pageSelections(pager)
                .bindToLifecycle(this@LocalMusicFragment)
                .subscribe { currentFragment = adapter.getItem(it) }
    }

    protected fun initToolbar() {
        toolbar.title = getString(R.string.local_music)

        toolbar.inflateMenu(R.menu.local_music)
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_search -> {
                    searchView.visibility = View.VISIBLE
                    true
                }
                R.id.action_sort -> {
//                        showSortPopup(activity.findViewById(R.id.action_sort))
                    true
                }
                else -> false
            }
        }

        initSearchView()

        RxBus.post(toolbar)
    }

    private fun initSearchView() {
        val searchToggle = SearchCrossDrawable(context)
        searchIcon.setImageDrawable(searchToggle)

        val searchTextState = RxTextView.textChanges(searchText).map{ it.length > 0 }
        val searchTextStateChange = searchTextState.startWith(false).buffer(2, 1).filter { it[0] !== it[1] }

        RxTextView.textChanges(searchText)
                .bindToLifecycle(this@LocalMusicFragment)
                .subscribe {
//                        RxBus.post(FilterLocalMusicCommand(it))
                }

        searchIcon.onClick {
            if (searchToggle.progress > 0)
                searchText.setText("")
        }

        btnClose.onClick {
            searchText.setText("")
            searchView.visibility = View.GONE
        }

        searchTextStateChange
                .filter { !it[0] && it[1] }
                .subscribe {
                    val animator = ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 500
                    animator.addUpdateListener {
                        searchToggle.progress = it.animatedValue as Float
                    }
                    animator.start()
                }

        searchTextStateChange
                .filter { it[0] && !it[1] }
                .subscribe {
                    val animator = ValueAnimator.ofFloat(1f, 0f)
                    animator.duration = 500
                    animator.addUpdateListener {
                        searchToggle.progress = it.animatedValue as Float
                    }
                    animator.start()
                }
    }

    // region Event handlers

    private fun onEvent(c: EFabActionAvailable) {
        if (!canShowFab) return

        fabAction = c.action
        with(binding) {
            fab.setImageResource(c.iconId)
            fab.show()
        }
    }

    private fun onEvent(e: EPlaybackStateChanged) {
        if (e.type == EPlaybackStateChanged.Type.STARTED) {
            if (canShowFab) {
                fab.hide(object : FloatingActionButton.OnVisibilityChangedListener() {
                    override fun onHidden(fab: FloatingActionButton?) {
                        nowPlayingView.show()
                                .subscribe {
                                    with(binding) {
                                        val a = object : Animation() {
                                            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                                                val params = coordinator.layoutParams as RelativeLayout.LayoutParams
                                                params.bottomMargin = (it * interpolatedTime).toInt()
                                                coordinator.layoutParams = params
                                            }
                                        }

                                        a.duration = 150
                                        coordinator.startAnimation(a)
                                    }
                                }

                    }
                })
                canShowFab = false
            }
        }
    }

//    // endregion
}
