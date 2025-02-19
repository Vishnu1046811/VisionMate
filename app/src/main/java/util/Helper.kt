package util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID

object Helper {
    fun getUriFromBitmap(image: Bitmap, context: Context): Uri? {
        return saveBitmapToStorage(context,bitmap = image, folder = tempDirectory(context), fileName = "edited_frame_with_qr_and_date${UUID.randomUUID()}.png");
    }

    fun tempDirectory(context: Context): File {
        return context.getExternalFilesDir(".Temp")!!
    }


    fun saveBitmapToStorage(context: Context,
        bitmap: Bitmap,
        folder: File,
        fileName: String = UUID.randomUUID().toString()
    ): Uri? {
        var file = folder
        if (!file.exists()) {
            file.mkdirs()
        }
        var isPng = false
        val fName = if (fileName.contains(".png")) {
            isPng = true
            fileName
        } else {
            if (fileName.contains(".jpg")) fileName else "$fileName.jpg"
        }
        file = File(file, fName)
        try {
            val stream: OutputStream = FileOutputStream(file)
            val format = if (isPng) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(format, 100, stream)

            stream.flush()
            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var uri = Uri.fromFile(file)
        //return Uri.fromFile(file)
        return convertFileUriToContentUri(context,Uri.fromFile(file))
    }


    fun convertFileUriToContentUri(context: Context, fileUri: Uri): Uri? {
        // Check if the URI is of type file
        if (fileUri.scheme == "file") {
            // Get the file path from the URI
            val filePath = fileUri.path ?: return null

            // Create a content URI using the custom provider
            return Uri.parse("content://${context.packageName}.fileprovider${filePath}")
        }
        return null // Return null if conversion fails
    }

    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        // Create a ContentValues object to specify the file details
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // Change MIME type if needed
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures") // Save to Pictures directory
        }

        // Insert the image into the MediaStore
        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        // If the URI is not null, write the bitmap to the output stream
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream) // Save as JPEG
                outputStream.flush()
            }
        }

        return uri // Return the content URI of the saved image
    }

}