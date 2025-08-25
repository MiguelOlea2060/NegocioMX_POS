package com.example.negociomx_pos.Utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

object FileUploadUtil {

    // ⚙️ CONFIGURA LA RUTA DE TU SERVIDOR DONDE QUIERES GUARDAR LAS FOTOS
    private const val SERVER_UPLOAD_URL = "http://192.168.1.162:8080/upload_soc_photos.php"

    suspend fun subirFotoAlServidor(
        archivoFoto: File,
        nombreArchivo: String,
        vin: String
    ): String? = withContext(Dispatchers.IO) {

        try {
            Log.d("FileUpload", "📤 Subiendo foto: $nombreArchivo")
            Log.d("FileUpload", "📁 Archivo existe: ${archivoFoto.exists()}")
            Log.d("FileUpload", "📏 Tamaño archivo: ${archivoFoto.length()} bytes")

            if (!archivoFoto.exists()) {
                Log.e("FileUpload", "❌ El archivo no existe: ${archivoFoto.absolutePath}")
                return@withContext null
            }

            val url = URL(SERVER_UPLOAD_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.doInput = true
            connection.useCaches = false
            connection.connectTimeout = 30000 // 30 segundos
            connection.readTimeout = 30000 // 30 segundos

            val boundary = "----WebKitFormBoundary" + System.currentTimeMillis()
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val outputStream = connection.outputStream
            val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

            // Enviar VIN
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"vin\"\r\n\r\n")
            writer.append("$vin\r\n")

            // Enviar archivo
            writer.append("--$boundary\r\n")
            writer.append("Content-Disposition: form-data; name=\"archivo\"; filename=\"$nombreArchivo\"\r\n")
            writer.append("Content-Type: image/jpeg\r\n\r\n")
            writer.flush()

            // Copiar archivo
            val fileInputStream = FileInputStream(archivoFoto)
            val buffer = ByteArray(4096)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            fileInputStream.close()

            writer.append("\r\n--$boundary--\r\n")
            writer.close()

            val responseCode = connection.responseCode
            Log.d("FileUpload", "📊 Código respuesta: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                Log.d("FileUpload", "✅ Respuesta servidor: $response")
                return@withContext nombreArchivo
            } else {
                val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Sin detalles"
                Log.e("FileUpload", "❌ Error subiendo foto. Código: $responseCode, Error: $errorResponse")
                return@withContext null
            }

        } catch (e: Exception) {
            Log.e("FileUpload", "💥 Error subiendo foto: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}
