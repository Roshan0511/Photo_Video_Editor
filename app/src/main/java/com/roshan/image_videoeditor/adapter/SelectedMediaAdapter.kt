package com.roshan.image_videoeditor.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.roshan.image_videoeditor.databinding.SelectedMediaSingleItemBinding
import com.roshan.image_videoeditor.fragments.EmojiBSFragment
import com.roshan.image_videoeditor.fragments.TextEditorDialogFragment
import com.roshan.image_videoeditor.fragments.VideoTrimmerFragment
import com.roshan.image_videoeditor.util.FileSaveHelper
import com.roshan.image_videoeditor.util.createImageFile
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import kotlinx.coroutines.launch
import java.io.File

class SelectedMediaAdapter(
    private val context: AppCompatActivity,
    private var files: List<File>,
    private val editingListener: EditingListener,
    private val cropMediaListener: CropMediaListener
) : RecyclerView.Adapter<SelectedMediaAdapter.MyViewHolder>() {

    interface EditingListener {
        fun onEditingStarted()
        fun onEditingFinished()
    }
    interface CropMediaListener {
        fun onVideoCrop(file: File)
        fun onImageCrop(uri: Uri)
    }

    private val TAG = "IMAGE_EDITING"

    private val photoEditors: MutableMap<Int, PhotoEditor> = mutableMapOf()

    inner class MyViewHolder(private val binding: SelectedMediaSingleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private lateinit var mEmojiBSFragment: EmojiBSFragment
        private lateinit var mPhotoEditor: PhotoEditor

        fun bind(file: File) {
            val type = checkMediaType(file)
            val uri = Uri.fromFile(file)

            if (type == "I") {
                setUpImageView(uri)

                binding.imageView.visibility = View.VISIBLE
                binding.videoRl.visibility = View.GONE

                binding.textIv.visibility = View.VISIBLE
                binding.emojiIv.visibility = View.VISIBLE
            } else {
                setUpViewView(uri, file.absolutePath)

                binding.imageView.visibility = View.GONE
                binding.videoRl.visibility = View.VISIBLE
                binding.playBtn.visibility = View.VISIBLE
                binding.pauseBtn.visibility = View.GONE

                binding.textIv.visibility = View.GONE
                binding.emojiIv.visibility = View.GONE
            }
        }




        @SuppressLint("MissingPermission")
        private fun setUpImageView(uri: Uri) {
            binding.imageView.source.setImageURI(uri)

            mPhotoEditor = PhotoEditor.Builder(context, binding.imageView)
                .setPinchTextScalable(true)
                .build()

            // Store the PhotoEditor instance
            photoEditors[adapterPosition] = mPhotoEditor

            mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {
                override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
                    Log.i(TAG, "onAddViewListener: Add")
                    editingListener.onEditingStarted()
                }

                override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
                    val textEditorDialogFragment =
                        TextEditorDialogFragment.show(context, text, colorCode)
                    textEditorDialogFragment.setOnTextEditorListener(object :
                        TextEditorDialogFragment.TextEditorListener {
                        override fun onDone(inputText: String, colorCode: Int) {
                            val styleBuilder = TextStyleBuilder()
                            styleBuilder.withTextColor(colorCode)
                            styleBuilder.withTextSize(26F)
                            mPhotoEditor.addText(inputText, styleBuilder)
                        }
                    })
                }

                override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {
                    Log.i(TAG, "onRemoveViewListener: Remove")
                    editingListener.onEditingFinished()
                }

                override fun onStartViewChangeListener(viewType: ViewType) {
                    Log.i(TAG, "onStartViewChangeListener: Start")
                    editingListener.onEditingStarted()
                }

                override fun onStopViewChangeListener(viewType: ViewType) {
                    Log.i(TAG, "onStopViewChangeListener: Stop")
                }

                override fun onTouchSourceImage(event: MotionEvent) {
                    Log.i(TAG, "onTouchSourceImage: Touch")
                    editingListener.onEditingFinished()
                }
            })

            mEmojiBSFragment = EmojiBSFragment()
            mEmojiBSFragment.setEmojiListener(object : EmojiBSFragment.EmojiListener {
                override fun onEmojiClick(emojiUnicode: String) {
                    mPhotoEditor.addEmoji(emojiUnicode)
                    editingListener.onEditingFinished()
                }
            })

            binding.emojiIv.setOnClickListener {
                showBottomSheetDialogFragment(mEmojiBSFragment, context)
            }

            binding.textIv.setOnClickListener {
                val textEditorDialogFragment = TextEditorDialogFragment.show(context)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        styleBuilder.withTextSize(26F)
                        mPhotoEditor.addText(inputText, styleBuilder)
                        editingListener.onEditingFinished()
                    }
                })
            }

            binding.cropIv.setOnClickListener {
                saveImage(mPhotoEditor)
            }
        }


        private fun setUpViewView(uri: Uri, path: String) {
            var mediaPlayer: MediaPlayer ?= null
            binding.videoView.setVideoURI(uri)
            binding.videoView.setOnPreparedListener { mp ->
                mediaPlayer = mp
                binding.playBtn.setOnClickListener {
                    mediaPlayer?.start()
                    binding.playBtn.visibility = View.GONE
                    binding.pauseBtn.visibility = View.VISIBLE
                }

                binding.pauseBtn.setOnClickListener {
                    mediaPlayer?.pause()
                    binding.playBtn.visibility = View.VISIBLE
                    binding.pauseBtn.visibility = View.GONE
                }
            }

            binding.videoView.setOnCompletionListener {
                binding.playBtn.visibility = View.VISIBLE
                binding.pauseBtn.visibility = View.GONE
            }

            binding.cropIv.setOnClickListener {
                mediaPlayer?.stop()
                binding.playBtn.visibility = View.VISIBLE
                binding.pauseBtn.visibility = View.GONE

                val videoTrimmerFragment = VideoTrimmerFragment.show(context, path)
                videoTrimmerFragment.setOnVideoTrimmerListener(object : VideoTrimmerFragment.VideoTrimmerListener {
                    override fun onDone(file: File) {
                        cropMediaListener.onVideoCrop(file = file)
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = SelectedMediaSingleItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun getAllFiles(): List<File> {
        return files
    }

    fun getPhotoEditor(position: Int): PhotoEditor? {
        return photoEditors[position]
    }

    fun updateData(newFile: File, position: Int) {
        files = files.toMutableList().apply {
            set(position, newFile)
        }
        notifyItemChanged(position)
    }

    private fun checkMediaType(file: File): String {
        val name = file.name.lowercase()
        val extension: String = name.substring(name.lastIndexOf(".") + 1)
        return if (extension.equals("jpg", ignoreCase = true) ||
            extension.equals("jpeg", ignoreCase = true) ||
            extension.equals("png", ignoreCase = true) ||
            extension.equals("webp", ignoreCase = true)
        ) {
            "I"
        } else {
            "V"
        }
    }

    private fun showBottomSheetDialogFragment(fragment: EmojiBSFragment?, context: AppCompatActivity) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(context.supportFragmentManager, fragment.tag)
    }



    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage(mPhotoEditor: PhotoEditor) {
        val mSaveFileHelper = FileSaveHelper(context)

        val newFile = context.createImageFile()
        context.lifecycleScope.launch {
            val saveSettings = SaveSettings.Builder()
                .setClearViewsEnabled(true)
                .setTransparencyEnabled(true)
                .build()

            val result = mPhotoEditor.saveAsFile(newFile.absolutePath, saveSettings)

            if (result is SaveFileResult.Success) {
                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(
                    context.contentResolver
                )

                cropMediaListener.onImageCrop(uri = Uri.fromFile(newFile))
            }
        }
    }
}