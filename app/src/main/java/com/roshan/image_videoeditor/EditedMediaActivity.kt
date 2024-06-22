package com.roshan.image_videoeditor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.roshan.image_videoeditor.adapter.EditedMediaAdapter
import com.roshan.image_videoeditor.databinding.ActivityEditedMediaBinding
import java.io.File

class EditedMediaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditedMediaBinding
    private var editedFiles: List<File> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditedMediaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        initViews()

        binding.backIv.setOnClickListener {
            finish()
        }
    }


    private fun initViews() {
        val filePaths = intent.getStringArrayListExtra("FILE_PATHS")

        if (filePaths != null) {
            editedFiles = ArrayList()

            editedFiles = filePaths.map { filePath ->
                File(filePath)
            }
        }

        setUpEditedMedia(editedFiles)
    }


    private fun setUpEditedMedia(files: List<File>) {
        val mediaAdapter = EditedMediaAdapter(files = files)
        binding.editedMediaRv.layoutManager = GridLayoutManager(
            this@EditedMediaActivity,
            3
        )
        binding.editedMediaRv.setAdapter(mediaAdapter)
    }
}