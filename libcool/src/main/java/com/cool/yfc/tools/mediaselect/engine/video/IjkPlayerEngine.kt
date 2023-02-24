package com.cool.yfc.tools.mediaselect.engine.video

import android.content.Context
import android.view.View
import com.luck.picture.lib.config.PictureSelectionConfig
import com.luck.picture.lib.engine.VideoPlayerEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnPlayerListener
import java.util.concurrent.CopyOnWriteArrayList

class IjkPlayerEngine : VideoPlayerEngine<IjkPlayerView> {
    companion object {
        val instance: IjkPlayerEngine = lazy { IjkPlayerEngine() }.value
    }

    /**
     * 播放状态监听器集
     */
    private val listeners = CopyOnWriteArrayList<OnPlayerListener>()
    override fun onCreateVideoPlayer(context: Context): View {
        return IjkPlayerView(context)
    }

    override fun onStarPlayer(player: IjkPlayerView, media: LocalMedia) {
        val mediaPlayer = player.mediaPlayer
        val config = PictureSelectionConfig.getInstance()
        mediaPlayer!!.isLooping = config.isLoopAutoPlay
        player.start(media.availablePath)
    }

    override fun onResume(player: IjkPlayerView) {
        val mediaPlayer = player.mediaPlayer
        mediaPlayer?.start()
    }

    override fun onPause(player: IjkPlayerView) {
        val mediaPlayer = player.mediaPlayer
        mediaPlayer?.pause()
    }

    override fun isPlaying(player: IjkPlayerView): Boolean {
        val mediaPlayer = player.mediaPlayer
        return mediaPlayer != null && mediaPlayer.isPlaying
    }

    override fun addPlayListener(playerListener: OnPlayerListener) {
        if (!listeners.contains(playerListener)) {
            listeners.add(playerListener)
        }
    }

    override fun removePlayListener(playerListener: OnPlayerListener?) {
        if (playerListener != null) {
            listeners.remove(playerListener)
        } else {
            listeners.clear()
        }
    }

    override fun onPlayerAttachedToWindow(player: IjkPlayerView) {
        val mediaPlayer = player.initMediaPlayer()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerReady()
            }
        }
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.reset()
            mediaPlayer.setDisplay(null)
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerEnd()
            }
            player.clearCanvas()
        }
        mediaPlayer.setOnErrorListener { _, _, _ ->
            for (i in listeners.indices) {
                val playerListener = listeners[i]
                playerListener.onPlayerError()
            }
            false
        }
    }

    override fun onPlayerDetachedFromWindow(player: IjkPlayerView) {
        player.release()
    }

    override fun destroy(player: IjkPlayerView) {
        player.release()
    }
}