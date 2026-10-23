package com.vanexa.supercalculator.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileOutputStream

/**
 * ImageTools — Enterprise-Grade Offline Image Compression Engine.
 * 
 * Features:
 * - OOM-Proof Ingestion using inSampleSize
 * - WebP and JPEG format support
 * - Coroutine Flow state emission
 */
object ImageTools {

    private const val TAG = "ImageTools"
    private const val MAX_FILE_SIZE = 50 * 1024 * 1024L // 50 MB
    private const val MAX_DIMENSION = 4000 // Max safe pixel dimension for memory

    enum class CompressionFormat {
        JPEG,
        WEBP
    }

    /**
     * Compress an image from Uri and emit progress updates.
     */
    fun compressImage(
        context: Context,
        inputUri: Uri,
        format: CompressionFormat
    ): Flow<PdfTools.ProgressUpdate> = flow {
        emit(PdfTools.ProgressUpdate.Loading("Initializing image engine..."))

        try {
            val fileSize = getFileSize(context, inputUri)
            if (fileSize > MAX_FILE_SIZE) {
                val sizeMB = String.format("%.1f", fileSize / (1024.0 * 1024.0))
                emit(PdfTools.ProgressUpdate.Error("File too large (Max 50 MB). Current: ${sizeMB} MB"))
                return@flow
            }

            emit(PdfTools.ProgressUpdate.Loading("Analyzing image structure..."))

            // 1. OOM-Proof: Decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // 2. Calculate optimal subsampling to prevent OutOfMemoryError
            options.inSampleSize = calculateInSampleSize(options, MAX_DIMENSION, MAX_DIMENSION)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            emit(PdfTools.ProgressUpdate.Loading("Decoding pixel data safely (Scale 1:${options.inSampleSize})..."))

            // 3. Load actual bitmap
            val bitmap = context.contentResolver.openInputStream(inputUri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            if (bitmap == null) {
                emit(PdfTools.ProgressUpdate.Error("Failed to decode image data."))
                return@flow
            }

            emit(PdfTools.ProgressUpdate.Loading("Applying compression algorithm..."))

            // 4. Setup output directory and file
            val outputDir = File(context.cacheDir, "image_tools").apply { mkdirs() }
            val extension = if (format == CompressionFormat.JPEG) "jpg" else "webp"
            val outputFile = File(outputDir, "vanexa_opt_${System.currentTimeMillis()}.$extension")

            // 5. Compress
            FileOutputStream(outputFile).use { out ->
                val compressFormat = if (format == CompressionFormat.JPEG) {
                    Bitmap.CompressFormat.JPEG
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        Bitmap.CompressFormat.WEBP_LOSSY
                    } else {
                        @Suppress("DEPRECATION")
                        Bitmap.CompressFormat.WEBP
                    }
                }
                
                val quality = if (format == CompressionFormat.JPEG) 80 else 75
                bitmap.compress(compressFormat, quality, out)
            }
            
            // Clean up bitmap
            bitmap.recycle()

            val finalSize = outputFile.length()
            val savedKb = fileSize / 1024
            val newKb = finalSize / 1024
            val percentage = if (savedKb > 0) ((savedKb - newKb).toFloat() / savedKb * 100).toInt() else 0

            val summary = "✓ Image Compressed! $savedKb KB → $newKb KB ($percentage% smaller)"
            emit(PdfTools.ProgressUpdate.Success(outputFile.absolutePath, summary))

        } catch (e: Exception) {
            Log.e(TAG, "Image compression failed", e)
            emit(PdfTools.ProgressUpdate.Error(e.message ?: "Unknown compression error"))
        }

    }.flowOn(Dispatchers.IO)


    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return size
    }
}
