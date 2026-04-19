package com.aiawareness.diary.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordPhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun stageImportedPhoto(uri: Uri): File {
        val tempDirectory = File(context.cacheDir, IMPORT_TEMP_DIR).apply { mkdirs() }
        return copySourceStreamToTempFile(
            openInputStream = { context.contentResolver.openInputStream(uri) },
            tempDirectory = tempDirectory,
            errorMessage = "无法读取图片"
        )
    }

    fun persistImportedPhoto(uri: Uri): String {
        val importedTempFile = stageImportedPhoto(uri)

        return try {
            persistPhoto(
                openInputStream = { importedTempFile.inputStream() },
                errorMessage = "无法读取图片",
                exifOrientation = {
                    ExifInterface(importedTempFile.absolutePath).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                }
            )
        } finally {
            importedTempFile.delete()
        }
    }

    fun persistCapturedPhoto(tempFile: File): String = persistPhoto(
        openInputStream = { tempFile.inputStream() },
        errorMessage = "无法读取拍照图片",
        exifOrientation = {
            ExifInterface(tempFile.absolutePath).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    )

    fun persistStagedPhoto(tempFile: File): String = try {
        persistPhoto(
            openInputStream = { tempFile.inputStream() },
            errorMessage = "无法读取图片",
            exifOrientation = {
                ExifInterface(tempFile.absolutePath).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        )
    } catch (_: IOException) {
        // Some imported formats preview correctly but fail our decode/recompress path.
        // In that case, keep the original staged bytes so saving the record still works.
        copyTempFileToPersistedPhoto(tempFile, File(context.filesDir, PHOTO_DIR).apply { mkdirs() }).absolutePath
    }

    fun createCameraTempFile(): File {
        val directory = File(context.cacheDir, CAMERA_TEMP_DIR).apply { mkdirs() }
        return File.createTempFile("capture_", ".jpg", directory)
    }

    fun deletePhoto(path: String) {
        if (path.isBlank()) {
            return
        }
        val target = File(path)
        if (target.exists()) {
            target.delete()
        }
    }

    private fun persistPhoto(
        openInputStream: () -> InputStream?,
        errorMessage: String,
        exifOrientation: () -> Int
    ): String {
        val decodedBitmap = decodeSampledBitmap(openInputStream, errorMessage)
        val rotatedBitmap = rotateBitmapIfNeeded(decodedBitmap, exifOrientation())

        return try {
            persistBitmap(rotatedBitmap)
        } finally {
            if (rotatedBitmap !== decodedBitmap) {
                decodedBitmap.recycle()
            }
            rotatedBitmap.recycle()
        }
    }

    private fun decodeSampledBitmap(
        openInputStream: () -> InputStream?,
        errorMessage: String
    ): Bitmap {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openInputStream()?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOptions)
        } ?: throw IOException(errorMessage)

        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            throw IOException(errorMessage)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(boundsOptions.outWidth, boundsOptions.outHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        val decodedBitmap = openInputStream()?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: throw IOException(errorMessage)

        return decodedBitmap
    }

    private fun persistBitmap(bitmap: Bitmap): String {
        val directory = File(context.filesDir, PHOTO_DIR).apply { mkdirs() }
        val target = File.createTempFile("record_", ".jpg", directory)
        FileOutputStream(target).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                throw IOException("图片保存失败")
            }
        }
        return target.absolutePath
    }

    private fun rotateBitmapIfNeeded(bitmap: Bitmap, exifOrientation: Int): Bitmap {
        val matrix = Matrix()

        when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            else -> return bitmap
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun calculateInSampleSize(width: Int, height: Int): Int {
        var inSampleSize = 1
        var sampledWidth = width
        var sampledHeight = height

        while (sampledWidth > MAX_DECODE_DIMENSION || sampledHeight > MAX_DECODE_DIMENSION) {
            inSampleSize *= 2
            sampledWidth = width / inSampleSize
            sampledHeight = height / inSampleSize
        }

        return inSampleSize.coerceAtLeast(1)
    }

    companion object {
        private const val PHOTO_DIR = "record_photos"
        private const val CAMERA_TEMP_DIR = "record_photo_camera"
        private const val IMPORT_TEMP_DIR = "record_photo_import"
        private const val JPEG_QUALITY = 82
        private const val MAX_DECODE_DIMENSION = 2048
    }
}

internal fun copySourceStreamToTempFile(
    openInputStream: () -> InputStream?,
    tempDirectory: File,
    errorMessage: String
): File {
    val sourceInputStream = openInputStream() ?: throw IOException(errorMessage)
    val tempFile = File.createTempFile("import_", ".tmp", tempDirectory)

    return try {
        sourceInputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    } catch (e: IOException) {
        tempFile.delete()
        throw IOException(errorMessage, e)
    }
}

internal fun copyTempFileToPersistedPhoto(sourceFile: File, targetDirectory: File): File {
    val targetFile = File.createTempFile("record_", ".img", targetDirectory)
    return try {
        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        targetFile
    } catch (e: IOException) {
        targetFile.delete()
        throw e
    }
}
