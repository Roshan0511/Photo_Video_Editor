package com.roshan.image_videoeditor.adapter

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.roshan.image_videoeditor.databinding.EditedMediaSingleItemBinding
import java.io.File

class EditedMediaAdapter(private var files: List<File>) :
    RecyclerView.Adapter<EditedMediaAdapter.MyViewHolder>() {

    inner class MyViewHolder(private val binding: EditedMediaSingleItemBinding) :
        RecyclerView.ViewHolder(binding.root)
    {
        fun bind(file: File) {
            val type = checkMediaType(file)
            val uri = Uri.fromFile(file)
            if (type=="I") {
                binding.imageView.setImageURI(uri)

                binding.imageView.visibility = View.VISIBLE
                binding.videoRl.visibility = View.GONE
            } else {
                binding.videoView.setVideoURI(uri)
                binding.videoView.setOnPreparedListener { mediaPlayer ->
                    binding.playBtn.setOnClickListener {
                        mediaPlayer.start()

                        binding.playBtn.visibility = View.GONE
                        binding.pauseBtn.visibility = View.VISIBLE
                    }

                    binding.pauseBtn.setOnClickListener {
                        mediaPlayer.pause()

                        binding.playBtn.visibility = View.VISIBLE
                        binding.pauseBtn.visibility = View.GONE
                    }
                }
                binding.videoView.setOnCompletionListener {
                    Log.i("VideoView", "Video completed")
                    binding.playBtn.visibility = View.VISIBLE
                    binding.pauseBtn.visibility = View.GONE
                }

                binding.imageView.visibility = View.GONE
                binding.videoRl.visibility = View.VISIBLE
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditedMediaAdapter.MyViewHolder {
        return MyViewHolder(
            EditedMediaSingleItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: EditedMediaAdapter.MyViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int {
        return files.size
    }


    fun getCurrentItem(position: Int) : File {
        return files[position]
    }

    fun updateData(newList: List<File>, position: Int) {
        files = newList
        notifyItemChanged(position)
    }



    private fun checkMediaType(file: File) : String {
        val name = file.name
        val extension: String = name.substring(name.lastIndexOf(".") + 1)

        return if (extension.equals("jpg", ignoreCase = true) ||
            extension.equals("jpeg", ignoreCase = true) ||
            extension.equals("png", ignoreCase = true) ||
            extension.equals("webp", ignoreCase = true)) {
            "I"
        } else {
            "V"
        }
    }
}