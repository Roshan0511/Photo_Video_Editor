package com.roshan.image_videoeditor.util

import android.content.ContentResolver
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

fun trimVideo(inputUri: Uri, outputFile: File, startMs: Long, endMs: Long, context: Context) {
    val extractor = MediaExtractor()
    val inputStream = context.contentResolver.openInputStream(inputUri)

    inputStream?.use {
        if (inputUri.scheme == ContentResolver.SCHEME_CONTENT) {
            extractor.setDataSource(context, inputUri, null)
        } else if (inputUri.scheme == ContentResolver.SCHEME_FILE) {
            extractor.setDataSource(inputUri.path!!)
        } else {
            throw IllegalArgumentException("Unsupported URI scheme.")
        }

        var videoTrackIndex = -1
        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("video/") == true) {
                videoTrackIndex = i
                break
            }
        }
        if (videoTrackIndex >= 0) {
            extractor.selectTrack(videoTrackIndex)
        } else {
            throw IllegalArgumentException("No video track found in the file.")
        }

        if (outputFile.exists()) {
            outputFile.delete()
        }
        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackIndex = muxer.addTrack(format!!)

        val bufferInfo = MediaCodec.BufferInfo()
        muxer.start()

        extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        val inputBuffer = ByteBuffer.allocate(format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE))

        var sampleTime = extractor.sampleTime
        while (sampleTime < endMs * 1000 && sampleTime != -1L) {
            bufferInfo.offset = 0
            bufferInfo.size = extractor.readSampleData(inputBuffer, 0)
            if (bufferInfo.size < 0) {
                break
            } else {
                bufferInfo.presentationTimeUs = sampleTime
                bufferInfo.flags = extractor.sampleFlags
                muxer.writeSampleData(trackIndex, inputBuffer, bufferInfo)
                extractor.advance()
                sampleTime = extractor.sampleTime
            }
        }
        muxer.stop()
        muxer.release()
        extractor.release()
    }
}