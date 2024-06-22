package com.roshan.image_videoeditor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.roshan.image_videoeditor.databinding.ActivityMainBinding
import com.roshan.image_videoeditor.util.Utils
import com.roshan.image_videoeditor.util.uriToFileConverter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        Utils.askPermissions(this@MainActivity)

        binding.pickFilesFab.setOnClickListener {
            pickMedia()
        }
    }




    // Get media files from the intent data
    private val pickMediaLauncher = registerForActivityResult(ActivityResultContracts
        .StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val clipData = result.data?.clipData
            val uris = mutableListOf<Uri>()

            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    uris.add(clipData.getItemAt(i).uri)
                }
            } else {
                result.data?.data?.let { uri ->
                    uris.add(uri)
                }
            }

            handleSelectedMedia(uris)
        } else {
            Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickMedia() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        val chooserIntent = Intent.createChooser(intent, "Select Media")
        pickMediaLauncher.launch(chooserIntent)
    }


    private fun handleSelectedMedia(uris: List<Uri>) {
        if (uris.isNotEmpty()) {
            uriToFileConverter(this@MainActivity, uris, pickedFiles = { files ->
                val filePaths: ArrayList<String> = ArrayList()
                for (file in files) {
                    Log.i("Selected", "handleSelectedImages: ${file.path}")
                    filePaths.add(file.absolutePath)
                }

                val intent = Intent(this, SelectedMediaActivity::class.java)
                intent.putStringArrayListExtra("FILE_PATHS", filePaths)
                startActivity(intent)
            })
        } else {
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show()
        }
    }
}