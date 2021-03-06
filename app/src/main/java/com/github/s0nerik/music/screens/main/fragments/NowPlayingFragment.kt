package com.github.s0nerik.music.screens.main.fragments

import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.View
import com.bumptech.glide.Glide
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringConfig
import com.facebook.rebound.SpringSystem
import com.github.s0nerik.music.App
import com.github.s0nerik.music.R
import com.github.s0nerik.music.base.BaseBoundFragment
import com.github.s0nerik.music.data.models.Song
import com.github.s0nerik.music.databinding.FragmentNowPlayingBinding
import com.github.s0nerik.music.ext.hide
import com.github.s0nerik.music.ext.show
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.fragment_now_playing.*
import rx.Observable
import rx.Subscription

class NowPlayingFragment : BaseBoundFragment<FragmentNowPlayingBinding>() {
    override val layoutId = R.layout.fragment_local_music

//    @Inject
//    protected var utils: Utils? = null
//    @Inject
//    protected var player: LocalPlayer? = null

    private val radialEqualizerViewSubscription: Subscription? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.comp.inject(this)
        initEventHandlers()
    }

    private fun initEventHandlers() {
//        RxBus.on(ESongChanged).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(PlaybackStartedEvent).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(PlaybackPausedEvent).bindToLifecycle(this).subscribe(::onEvent)
//        RxBus.on(EPlaybackProgress).bindToLifecycle(this).subscribe(::onEvent)
    }

    fun show(fragmentManager: FragmentManager): Observable<Int> {
        return Observable.create({
            fragment.hide()

            fragmentManager.beginTransaction().show(this@NowPlayingFragment).commitNowAllowingStateLoss()

            val mainSpringSystem = SpringSystem.create()
            val mainSpring = mainSpringSystem.createSpring()
            mainSpring.springConfig = SpringConfig(1000.0, 100.0)
            mainSpring.addListener(object : SimpleSpringListener() {
                override fun onSpringActivate(spring: Spring) {
                    fragment.show()
                    fragment.translationY = 100000f
                    mainProgressShadow.alpha = 0f

                    scaleFab(0f)
                }

                override fun onSpringUpdate(s: Spring) {
                    val translation = (1f - s.currentValue) * mainGroup.height

                    fragment.translationY = translation.toFloat()
                    mainProgressShadow.alpha = s.currentValue.toFloat()
                }

                override fun onSpringAtRest(spr: Spring) {
                    val springSystem = SpringSystem.create()
                    val spring = springSystem.createSpring()
                    spring.addListener(object : SimpleSpringListener() {
                        override fun onSpringUpdate(s: Spring) {
                            scaleFab(s.currentValue.toFloat())
                        }

                        override fun onSpringAtRest(s: Spring) {
                            it.onNext(mainGroup.height - mainGroup.paddingTop)
                            it.onCompleted()
                        }

                    })
                    spring.endValue = 1.0
                }
            })
            mainSpring.endValue = 1.0
        })
    }

    private fun scaleFab(scale: Float) {
        btnPlayPause.scaleX = scale
        btnPlayPause.scaleY = scale

        circleProgressBg.scaleX = scale
        circleProgressBg.scaleY = scale
        circleProgressBg.alpha = scale

        circleProgressShadow.scaleX = scale
        circleProgressShadow.scaleY = scale
        circleProgressShadow.alpha = scale
    }

    fun setSongInfo(song: Song) {
        Glide.with(cover.context)
                .load(song.albumArtUri)
                .bitmapTransform(BlurTransformation(activity, Glide.get(activity).bitmapPool))
                .placeholder(R.color.md_black_1000)
                .error(R.drawable.no_cover)
                .crossFade()
                .into(cover)

        artist.text = song.artistNameForUi
        title.text = song.title
    }

//    @OnClick(R.id.layout, R.id.cover)
//    fun onLayoutClicked() {
//        val intent = Intent(activity, LocalPlaybackActivity)
//        startActivity(intent)
//        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left_long_alpha)
//    }

//    @OnClick(R.id.playbackFab)
//    fun onFabClicked() {
//        player!!.invokeMethod("togglePause", arrayOfNulls<Any>(0))
//    }

//    private fun onEvent(event: ESongChanged) {
//        setSongInfo(event.song)
//    }
//
//    private fun onEvent(e: PlaybackStartedEvent) {
//        (playbackFab as ImageView).setImageResource(R.drawable.ic_pause_24dp)
//
//        //        radialEqualizerViewSubscription?.unsubscribe()
//        //
//        //        radialEqualizerViewSubscription = Observable.interval(200, TimeUnit.MILLISECONDS)
//        //                .observeOn(AndroidSchedulers.mainThread())
//        //                .subscribe {
//        //            radialEqualizerView.randomize()
//        //        }
//    }
//
//    private fun onEvent(e: PlaybackPausedEvent) {
//        (playbackFab as ImageView).setImageResource(R.drawable.ic_play_arrow_24dp)
//
//        radialEqualizerViewSubscription!!.unsubscribe()
//    }
//
//    private fun onEvent(e: EPlaybackProgress) {
//        setProgress(e.progressPercent)
//    }

    private fun setProgress(progress: Float) {
        val circleRect = Rect()
        playbackFab.getGlobalVisibleRect(circleRect)
        val circleStartPercent = 100f - (mainProgress.width - circleRect.left) / mainProgress.width.toFloat() * 100f
        val circleEndPercent = 100f - (mainProgress.width - circleRect.right) / mainProgress.width.toFloat() * 100f
        if (progress > circleStartPercent && progress < circleEndPercent) {
            val circlePercents = circleEndPercent - circleStartPercent
            circleProgress.progress = ((progress - circleStartPercent) / circlePercents).toFloat() * 100f
            mainProgress.progress = (((circleEndPercent + circleStartPercent) / 2f).toInt() * 10f).toInt()
        } else if (progress <= circleStartPercent) {
            mainProgress.progress = (progress.toInt() * 10f).toInt()
            circleProgress.progress = 0f
        } else if (progress >= circleEndPercent) {
            mainProgress.progress = (progress.toInt() * 10f).toInt()
            circleProgress.progress = 100f
        }
    }
}
