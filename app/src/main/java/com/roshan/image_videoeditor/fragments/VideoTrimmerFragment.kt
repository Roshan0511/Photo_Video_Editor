package com.roshan.image_videoeditor.fragments

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.roshan.image_videoeditor.R
import com.roshan.image_videoeditor.tools.OnRangeChangedListener
import com.roshan.image_videoeditor.tools.RangeSeekBar
import com.roshan.image_videoeditor.util.trimVideo
import java.io.File

class VideoTrimmerFragment : DialogFragment(), OnRangeChangedListener {

    private lateinit var videoView: VideoView
    private lateinit var rangeSeekBar: RangeSeekBar
    private lateinit var mTrimDoneTextView: TextView

    private var mVideoTrimmerListener: VideoTrimmerListener? = null

    private var videoDurationMs: Long = 0
    private var startPositionMs: Long = 0
    private var endPositionMs: Long = 0

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    interface VideoTrimmerListener {
        fun onDone(file: File)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_video_trimmer, container, false)
    }

    fun setOnVideoTrimmerListener(videoTrimmerListener: VideoTrimmerListener) {
        mVideoTrimmerListener = videoTrimmerListener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        mTrimDoneTextView = view.findViewById(R.id.trim_done_tv)
        videoView = view.findViewById(R.id.videoView)
        rangeSeekBar = view.findViewById(R.id.rangeSeekBar)

        val arguments = requireArguments()

        val path = arguments.getString(EXTRA_VIDEO_PATH)
        val uri = Uri.fromFile(File(path!!))

        videoView.setVideoURI(uri)
        videoView.setOnPreparedListener { mp ->
            videoDurationMs = mp.duration.toLong()
            rangeSeekBar.setMax(100)
            rangeSeekBar.setRange(0, 100)

            videoView.start()
        }

        rangeSeekBar.setOnRangeChangedListener(this)

        videoView.setOnErrorListener { mp, what, extra ->
            false
        }

        videoView.setOnCompletionListener {
            videoView.start()
        }

        videoView.setOnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                monitorPlayback()
            }
            false
        }

        //Make a callback on activity when user is done with video trimming
        mTrimDoneTextView.setOnClickListener {
            if (startPositionMs >= endPositionMs) {
                startPositionMs = 0
                endPositionMs = videoDurationMs
            }

            val outputFile = File(activity.getExternalFilesDir(null), "trimmed_video.mp4")

            trimVideo(uri, outputFile, startPositionMs, endPositionMs, activity)

            dismiss()
            val videoTrimmerListener = mVideoTrimmerListener
            videoTrimmerListener?.onDone(outputFile)
        }
    }

    override fun onRangeChanged(startProgress: Int, endProgress: Int) {
        if (startProgress >= endProgress) {
            rangeSeekBar.setRange(startProgress - 1, endProgress)
            return
        }

        startPositionMs = startProgress * videoDurationMs / 100
        endPositionMs = endProgress * videoDurationMs / 100

        videoView.seekTo(startPositionMs.toInt())
        if (videoView.isPlaying) {
            videoView.pause()
        }
        videoView.start()

        monitorPlayback()
    }

    private fun monitorPlayback() {
        videoView.postDelayed(object : Runnable {
            override fun run() {
                val currentPosMs = videoView.currentPosition.toLong()
                if (currentPosMs >= endPositionMs) {
                    videoView.seekTo(startPositionMs.toInt())
                } else {
                    videoView.postDelayed(this, 100) // Check every 100ms
                }
            }
        }, 100)
    }

    private fun getVideoDuration(uri: Uri, activity: Activity): Long {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(activity, uri)
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        retriever.release()
        return duration?.toLong() ?: 0
    }

    companion object {
        private val TAG: String = VideoTrimmerFragment::class.java.simpleName
        private const val EXTRA_VIDEO_PATH = "extra_video_path"

        @JvmStatic
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            path: String = ""
        ): VideoTrimmerFragment {
            val args = Bundle()
            args.putString(EXTRA_VIDEO_PATH, path)
            val fragment = VideoTrimmerFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        videoView.resume()
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }
}