package com.example.negociomx_pos.Utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BLLUtils {
/*    public fun comprimirImagen(archivoOriginal: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath)

            val maxSize = 3072
            var ratio: Float = 1.0F
            if (bitmap.width > bitmap.height)
                ratio = maxSize.toFloat() / bitmap.width
            else
                ratio = maxSize.toFloat() / bitmap.height

            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_paso3_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso3REPUVE", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido

        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "Error comprimiendo imagen: ${e.message}")
            archivoOriginal
        }
    }*/

    /*private fun obtenerArchivoDesdeUri(uri: Uri): File? {
        return try {
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }

            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempFile = File(getExternalFilesDir(null), "temp_paso3_photo_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "Error obteniendo archivo desde URI: ${e.message}")
            null
        }
    }*/

    public fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    public fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            //Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    public fun saveBitmapToFile(context: Context, fotoBase64: String,nombreCarpeta:String, nombreArchivo: String): Uri? {
        var imgUri: Uri?=null
        val decodedBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
        var bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        try {
            if (bitmap != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imgUri =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +"/"+nombreCarpeta)
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val mkDir=File(Environment.DIRECTORY_PICTURES+"/"+nombreCarpeta)
                if(!mkDir.exists())
                    mkDir.mkdirs()

                imgUri = context. contentResolver.insert(imgUri, contentValues)!!

                if (imgUri != null) {
                    val outputStream =context. contentResolver.openOutputStream(imgUri,"wt")
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    outputStream?.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(imgUri!!, contentValues, null, null)
                }
            }
        }
        catch (ex:Exception)
        {
            var cadena=ex.message.toString()
            cadena+=""
        }
        return imgUri
    }

    public fun saveBitmapToFile(context: Context,bitmap: Bitmap ,nombreCarpeta:String, nombreArchivo: String): Uri? {
        var imgUri: Uri?=null
        try {
            if (bitmap != null) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    imgUri =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    imgUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES +"/"+nombreCarpeta)
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val mkDir=File(Environment.DIRECTORY_PICTURES+"/"+nombreCarpeta)
                if(!mkDir.exists())
                    mkDir.mkdirs()

                imgUri = context. contentResolver.insert(imgUri, contentValues)!!

                if (imgUri != null) {
                    val outputStream =context. contentResolver.openOutputStream(imgUri,"wt")
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }

                    outputStream?.close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(imgUri!!, contentValues, null, null)
                }
            }
        }
        catch (ex:Exception)
        {
            var cadena=ex.message.toString()
            cadena+=""
        }
        return imgUri
    }

    public fun convertirImagenABase64(archivo: File): String? {
        return try {
            val bytes = archivo.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("BLLUtils", "Error convirtiendo imagen a Base64: ${e.message}")
            null
        }
    }

}