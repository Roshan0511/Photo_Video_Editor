package com.roshan.image_videoeditor

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.roshan.image_videoeditor.adapter.SelectedMediaAdapter
import com.roshan.image_videoeditor.databinding.ActivitySelectedMediaBinding
import com.roshan.image_videoeditor.util.FileSaveHelper
import com.roshan.image_videoeditor.util.createImageFile
import com.yalantis.ucrop.UCrop
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.SaveSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class SelectedMediaActivity : AppCompatActivity(), SelectedMediaAdapter.EditingListener,
    SelectedMediaAdapter.CropMediaListener {

    private lateinit var binding: ActivitySelectedMediaBinding
    private lateinit var mediaAdapter: SelectedMediaAdapter
    private var selectedFiles: List<File> = emptyList()
    private var currentPage: Int = 0

    private var isEditingInProgress = false // Flag to track if text or emoji editing is in progress

    private lateinit var mSaveFileHelper: FileSaveHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectedMediaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        initViews()
        setClickListeners()
    }

    private fun initViews() {
        selectedFiles = intent.getStringArrayListExtra("FILE_PATHS")?.map { File(it) } ?: emptyList()
        setUpSelectedMedia()

        mSaveFileHelper = FileSaveHelper(this@SelectedMediaActivity)
    }

    private fun setClickListeners() {
        binding.saveTv.setOnClickListener { saveAllImages() }
    }

    private fun setUpSelectedMedia() {
        mediaAdapter = SelectedMediaAdapter(this, selectedFiles, this, this)
        binding.mediaVp.adapter = mediaAdapter
        binding.mediaVp.offscreenPageLimit = selectedFiles.size.takeIf { it > 0 } ?: ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT

        binding.currentPageTv.text = "${currentPage+1}/${selectedFiles.size}"
        binding.indicator.setViewPager(binding.mediaVp)

        binding.mediaVp.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                currentPage = position
                binding.currentPageTv.text = "${currentPage+1}/${selectedFiles.size}"
                binding.indicator.animatePageSelected(position % selectedFiles.size)
                isEditingInProgress = false
            }
        })
    }

    private fun saveMedia() {
        val filePaths = selectedFiles.map { it.absolutePath }
        val intent = Intent(this, EditedMediaActivity::class.java).apply {
            putStringArrayListExtra("FILE_PATHS", ArrayList(filePaths))
        }
        startActivity(intent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data!!)

            try {
                val inputStream = contentResolver.openInputStream(resultUri!!)
                if (inputStream != null) {
                    val outputFile = createTempFile(suffix = ".jpg")
                    val outputStream = FileOutputStream(outputFile)
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()

                    mediaAdapter.updateData(outputFile, currentPage)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isEditingInProgress) {
            Toast.makeText(this, "Finish editing before exiting", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onEditingStarted() {
        isEditingInProgress = true
        binding.mediaVp.isUserInputEnabled = false
    }

    override fun onEditingFinished() {
        isEditingInProgress = false
        binding.mediaVp.isUserInputEnabled = true
    }



    override fun onVideoCrop(file: File) {
        Log.i("Dekho-Dekho", "onVideoCrop: ${file.absolutePath}")
        mediaAdapter.updateData(file, currentPage)
    }

    override fun onImageCrop(uri: Uri) {
        val outputUri = Uri.fromFile(this.createImageFile())

        val options = UCrop.Options()
        options.setActiveControlsWidgetColor(getColor(R.color.appColor))

        UCrop.of(uri, outputUri)
            .withAspectRatio(5f, 5f)
            .withOptions(options)
            .withMaxResultSize(2000, 2000)
            .start(this@SelectedMediaActivity)
    }







    @SuppressLint("MissingPermission")
    private fun saveAllImages() {
        val files = mediaAdapter.getAllFiles()
        val mSaveFileHelper = FileSaveHelper(this)
        val filePath = ArrayList<String>()

        lifecycleScope.launch {
            val deferredResults = files.mapIndexed { index, file ->
                async {
                    val mediaType = checkMediaType(file)
                    if (mediaType == "I") {
                        val photoEditor = mediaAdapter.getPhotoEditor(index)
                        if (photoEditor != null) {
                            val newFile = createImageFile()

                            val saveSettings = SaveSettings.Builder()
                                .setClearViewsEnabled(true)
                                .setTransparencyEnabled(true)
                                .build()

                            try {
                                val result = photoEditor.saveAsFile(newFile.absolutePath, saveSettings)
                                if (result is SaveFileResult.Success) {
                                    mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                    filePath.add(newFile.absolutePath)
                                } else {
                                    Log.e("PhotoEditor", "Failed to save edited image: $file")
                                }
                            } catch (e: Exception) {
                                Log.e("PhotoEditor", "Error saving file: ${file.absolutePath}", e)
                            }
                        } else {
                            Log.e("PhotoEditor", "PhotoEditor is null for file: $file")
                        }
                    } else {
                        filePath.add(file.absolutePath)
                    }
                }
            }

            // Wait for all save operations to complete
            deferredResults.awaitAll()

            val intent = Intent(this@SelectedMediaActivity, EditedMediaActivity::class.java).apply {
                putStringArrayListExtra("FILE_PATHS", filePath)
            }
            startActivity(intent)
            finish()
        }
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
}
