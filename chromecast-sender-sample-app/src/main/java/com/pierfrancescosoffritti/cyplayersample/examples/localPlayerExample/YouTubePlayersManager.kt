package com.pierfrancescosoffritti.cyplayersample.examples.localPlayerExample

import android.view.View
import android.widget.Button
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.ChromecastYouTubePlayerContext
import com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender.io.infrastructure.ChromecastConnectionListener
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.player.views.YouTubePlayerView
import com.pierfrancescosoffritti.cyplayersample.R
import com.pierfrancescosoffritti.cyplayersample.ui.SimpleChromeCastUIController
import com.pierfrancescosoffritti.cyplayersample.utils.VideoIdsProvider

/**
 * Class used to manage the two YouTubePlayers, local and cast.
 *
 * The local YouTubePlayer is supposed to stop playing when the cast player stars and vice versa.
 *
 * When one of the two players stops, the other has to resume the playback from where the previous player stopped.
 */
class YouTubePlayersManager(
        localYouTubePlayerInitListener: LocalYouTubePlayerInitListener,
        private val youtubePlayerView: YouTubePlayerView, chromecastControls: View,
        private val chromecastPlayerListener: YouTubePlayerListener) : ChromecastConnectionListener {

    private val nextVideoButton = chromecastControls.findViewById<Button>(R.id.next_video_button)

    val chromecastUIController = SimpleChromeCastUIController(chromecastControls)

    private var localYouTubePlayer: YouTubePlayer? = null
    private var chromecastYouTubePlayer: YouTubePlayer? = null

    private val chromecastPlayerStateTracker = YouTubePlayerTracker()
    private val localPlayerStateTracker = YouTubePlayerTracker()

    private var playingOnCastPlayer = false

    init {
        initLocalYouTube(localYouTubePlayerInitListener)
        nextVideoButton.setOnClickListener { chromecastYouTubePlayer?.loadVideo(VideoIdsProvider.getNextVideoId(), 0f) }
    }

    override fun onChromecastConnecting() {
        localYouTubePlayer?.pause()
    }

    override fun onChromecastConnected(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        initializeCastPlayer(chromecastYouTubePlayerContext)

        playingOnCastPlayer = true
    }

    override fun onChromecastDisconnected() {
        if(chromecastPlayerStateTracker.state == PlayerConstants.PlayerState.PLAYING)
            localYouTubePlayer?.loadVideo(chromecastPlayerStateTracker.videoId!!, chromecastPlayerStateTracker.currentSecond)
        else
            localYouTubePlayer?.cueVideo(chromecastPlayerStateTracker.videoId!!, chromecastPlayerStateTracker.currentSecond)

        chromecastUIController.resetUI()

        playingOnCastPlayer = false
    }

    fun togglePlayback() {
        if(playingOnCastPlayer)
            if(chromecastPlayerStateTracker.state == PlayerConstants.PlayerState.PLAYING)
                chromecastYouTubePlayer?.pause()
            else
                chromecastYouTubePlayer?.play()
        else
            if(localPlayerStateTracker.state == PlayerConstants.PlayerState.PLAYING)
                localYouTubePlayer?.pause()
            else
                localYouTubePlayer?.play()
    }

    private fun initLocalYouTube(localYouTubePlayerInitListener: LocalYouTubePlayerInitListener) {
        youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                localYouTubePlayer = youTubePlayer
                youTubePlayer.addListener(localPlayerStateTracker)

                if(!playingOnCastPlayer)
                    youTubePlayer.loadVideo(VideoIdsProvider.getNextVideoId(), chromecastPlayerStateTracker.currentSecond)

                localYouTubePlayerInitListener.onLocalYouTubePlayerInit()
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                if(playingOnCastPlayer && localPlayerStateTracker.state == PlayerConstants.PlayerState.PLAYING)
                    youTubePlayer.pause()
            }
        }, true)
    }

    private fun initializeCastPlayer(chromecastYouTubePlayerContext: ChromecastYouTubePlayerContext) {
        chromecastYouTubePlayerContext.initialize(object: AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                chromecastYouTubePlayer = youTubePlayer

                chromecastUIController.youTubePlayer = youTubePlayer

                youTubePlayer.addListener(chromecastPlayerListener)
                youTubePlayer.addListener(chromecastPlayerStateTracker)
                youTubePlayer.addListener(chromecastUIController)

                youTubePlayer.loadVideo(localPlayerStateTracker.videoId!!, localPlayerStateTracker.currentSecond)
            }
        })
    }

    /**
     * Interface used to notify its listeners than the local YouTubePlayer is ready to play videos.
     */
    interface LocalYouTubePlayerInitListener {
        fun onLocalYouTubePlayerInit()
    }
}