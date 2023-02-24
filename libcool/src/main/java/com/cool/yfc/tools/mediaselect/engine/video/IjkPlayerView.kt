package com.cool.yfc.tools.mediaselect.engine.video

import android.content.Context
import android.graphics.PixelFormat
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import com.luck.picture.lib.config.PictureMimeType
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import java.io.IOException

class IjkPlayerView : FrameLayout, SurfaceHolder.Callback {
    private var surfaceView: IjkVideoSurfaceView? = null
    var mediaPlayer: IjkMediaPlayer? = null
        private set

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        surfaceView = IjkVideoSurfaceView(context)
        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        layoutParams.gravity = Gravity.CENTER
        surfaceView!!.layoutParams = layoutParams
        addView(surfaceView)
        val surfaceHolder = surfaceView!!.holder
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT)
        surfaceHolder.addCallback(this)
    }

    fun initMediaPlayer(): IjkMediaPlayer {
        if (mediaPlayer == null) {
            mediaPlayer = IjkMediaPlayer()
        }
        mediaPlayer!!.setOnVideoSizeChangedListener { mediaPlayer, _, _, _, _ ->
            surfaceView!!.adjustVideoSize(mediaPlayer.videoWidth,
                mediaPlayer.videoHeight)
        }
        mediaPlayer!!.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1)
        return mediaPlayer!!
    }

    fun start(path: String?) {
        try {
            if (PictureMimeType.isContent(path)) {
                mediaPlayer!!.setDataSource(context, Uri.parse(path))
            } else {
                mediaPlayer!!.dataSource = path
            }
            mediaPlayer!!.setDisplay(surfaceView!!.holder)
            mediaPlayer!!.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer!!.setDisplay(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    override fun surfaceDestroyed(holder: SurfaceHolder) {}
    class IjkVideoSurfaceView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : SurfaceView(context, attrs, defStyleAttr) {
        /**
         * 视频宽度
         */
        private var videoWidth = 0

        /**
         * 视频高度
         */
        private var videoHeight = 0
        fun adjustVideoSize(videoWidth: Int, videoHeight: Int) {
            if (videoWidth == 0 || videoHeight == 0) {
                return
            }
            this.videoWidth = videoWidth
            this.videoHeight = videoHeight
            holder.setFixedSize(videoWidth, videoHeight)
            requestLayout()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            var width = getDefaultSize(videoWidth, widthMeasureSpec)
            var height = getDefaultSize(videoHeight, heightMeasureSpec)
            if (videoWidth > 0 && videoHeight > 0) {
                val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
                val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
                val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
                val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
                if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                    width = widthSpecSize
                    height = heightSpecSize
                    if (videoWidth * height < width * videoHeight) {
                        width = height * videoWidth / videoHeight
                    } else if (videoWidth * height > width * videoHeight) {
                        height = width * videoHeight / videoWidth
                    }
                } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                    width = widthSpecSize
                    height = width * videoHeight / videoWidth
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                        height = heightSpecSize
                    }
                } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                    height = heightSpecSize
                    width = height * videoWidth / videoHeight
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                        width = widthSpecSize
                    }
                } else {
                    width = videoWidth
                    height = videoHeight
                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                        height = heightSpecSize
                        width = height * videoWidth / videoHeight
                    }
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                        width = widthSpecSize
                        height = width * videoHeight / videoWidth
                    }
                }
            }
            setMeasuredDimension(width, height)
        }
    }

    fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer!!.setOnPreparedListener(null)
            mediaPlayer!!.setOnCompletionListener(null)
            mediaPlayer!!.setOnErrorListener(null)
            mediaPlayer = null
        }
    }

    fun clearCanvas() {
        surfaceView!!.holder.setFormat(PixelFormat.OPAQUE)
        surfaceView!!.holder.setFormat(PixelFormat.TRANSPARENT)
    }
}