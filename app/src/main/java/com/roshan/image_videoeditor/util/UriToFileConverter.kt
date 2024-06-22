package com.roshan.image_videoeditor.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

fun uriToFileConverter(context: Context, uris: List<Uri>, pickedFiles: (List<File>) -> Unit) {
    val fileList = ArrayList<File>()

    for (i in uris.indices) {
        val file = uris[i].toFile(context)
        if (file != null) {
            fileList.add(file)
        }

        if (i == uris.size-1) {
            pickedFiles(fileList)
        }
    }
}



fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(imageFileName, ".jpg", externalCacheDir)
}


fun Uri.toFile(context: Context): File? {
    val contentResolver = context.contentResolver
    val fileName = contentResolver.getFileName(this) ?: return null
    val tempFile = File(context.cacheDir, fileName)
    tempFile.createNewFile()

    try {
        val inputStream: InputStream? = contentResolver.openInputStream(this)
        val outputStream = FileOutputStream(tempFile)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun android.content.ContentResolver.getFileName(uri: Uri): String? {
    var name: String? = null
    val returnCursor = this.query(uri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}