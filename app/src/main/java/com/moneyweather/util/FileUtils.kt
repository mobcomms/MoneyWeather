package com.moneyweather.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.moneyweather.BuildConfig
import java.io.*
import java.lang.Exception
import java.net.URL

class FileUtils {
    companion object {
        fun getMimeType(fileName: String, fallback: String = "image/*"): String {
            return MimeTypeMap.getFileExtensionFromUrl(fileName)
                    ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase()) }
                    ?: fallback // You might set it to */*
        }

        fun findAttachFile(context: Context, returnUri: Uri): File? {
            val realPath: String

            // SDK < API11
            realPath = if (Build.VERSION.SDK_INT < 11) {
                RealPathUtil.getRealPathFromURI_BelowAPI11(context, returnUri)
            } else if (Build.VERSION.SDK_INT < 19) {
                RealPathUtil.getRealPathFromURI_API11to18(context, returnUri)
            } else {
                RealPathUtil.getRealPathFromURI_API19(context, returnUri)
            }

            return if (!TextUtils.isEmpty(realPath)) {
                File(realPath)
            }
            else null
        }

        fun download(context: Context, url: String, directory: String, filename: String, callback: (Uri?) -> Unit) {
            Thread(Runnable {
                try {
                    URL(url).openStream().use { input ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            callback(saveFileAboveQ(context, input, directory, filename))
                        }
                        else{
                            callback(saveFileLegacyStyle(context, input, directory, filename))
                        }
                    }
                } catch (e: Exception){
                    e.printStackTrace()
                    callback(null)
                }
            }).start()
        }

        fun saveFile(context: Context, bitmap: Bitmap, directory: String, filename: String): Uri? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                return saveFileAboveQ(context, bitmap, directory, filename)
            }
            else{
                return saveFileLegacyStyle(context, bitmap, directory, filename)
            }
        }

        fun saveFile(context: Context, file: File, directory: String, filename: String): Uri? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                return saveFileAboveQ(context, file, directory, filename)
            }
            else{
                return saveFileLegacyStyle(context, file, directory, filename)
            }
        }

        fun saveFileAboveQ(context: Context, bitmap: Bitmap, directory: String, filename: String):Uri? {

            var fos: OutputStream? = null
            var imageUri: Uri? = null

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            //use application context to get contentResolver
            val contentResolver = context.applicationContext.contentResolver

            contentResolver.also { resolver ->
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }

            fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

            imageUri?.let { imageUri ->
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                contentResolver.update(imageUri, contentValues, null, null)
            }

            return imageUri

        }

        fun saveFileAboveQ(context: Context, file: File, directory: String, filename: String):Uri? {

            return saveFileAboveQ(context, FileInputStream(file), directory, filename)

        }

        fun saveFileAboveQ(context: Context, inputStream: InputStream, directory: String, filename: String):Uri? {

            var fos: OutputStream? = null
            var imageUri: Uri? = null

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(filename))
                put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }

            val contentResolver = context.applicationContext.contentResolver

            contentResolver.also { resolver ->
                if(Environment.DIRECTORY_PICTURES == directory) {
                    imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
                else {
                    imageUri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            }

            inputStream.use { input ->
                fos?.use { output ->
                    input.copyTo(output)
                }
            }

            imageUri?.let {imageUri ->
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.applicationContext.contentResolver.update(imageUri, contentValues, null, null)
            }

            return imageUri

        }

        fun saveFileLegacyStyle(context: Context, bitmap: Bitmap, directory: String, filename: String): Uri? {
            try {
                var fos: OutputStream? = null

                val imagesDir = Environment.getExternalStoragePublicDirectory(directory)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)

                fos.use {bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)}

                MediaScannerConnection.scanFile(
                        context, arrayOf(image.toString()), arrayOf("image/gif", "image/png", "image/jpeg", "image/bmp"),
                        null)

                val uri = FileProvider.getUriForFile(context.applicationContext, BuildConfig.APPLICATION_ID + ".fileprovider", image)

                return uri
            } catch(e: Exception) {
                e.printStackTrace()
                return null
            }
        }


        fun saveFileLegacyStyle(context: Context, file: File, directory: String, filename: String): Uri? {
            val input = FileInputStream(file)

            return saveFileLegacyStyle(context, input, directory, filename)
        }

        fun saveFileLegacyStyle(context: Context, inputStream: InputStream, directory: String, filename: String): Uri? {
            try {
                var output: OutputStream? = null

                val imagesDir = Environment.getExternalStoragePublicDirectory(directory)
                val image = File(imagesDir, filename)
                output = FileOutputStream(image)

                inputStream.use { input ->
                    output.use { output ->
                        input.copyTo(output)
                    }
                }

                MediaScannerConnection.scanFile(
                        context, arrayOf(image.toString()), arrayOf("image/gif", "image/png", "image/jpeg", "image/bmp", getMimeType(filename)),
                        null)

                val uri = FileProvider.getUriForFile(context.applicationContext, BuildConfig.APPLICATION_ID + ".fileprovider", image)

                return uri
            } catch(e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        fun getFileUri(context: Context, file: File): Uri {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                return FileProvider.getUriForFile(context.applicationContext, BuildConfig.APPLICATION_ID + ".fileprovider", file);
            } else {
                return Uri.fromFile(file);
            }
        }

    }
}