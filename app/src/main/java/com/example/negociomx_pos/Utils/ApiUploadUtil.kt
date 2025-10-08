package com.example.negociomx_pos.Utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class ApiUploadUtil {
    companion object {
        // URL de tu API para subir archivos
        //private const val BASE_URL = "https://softsystemmx.com/api/Upload/UploadFile"
        private const val TAG = "ApiUploadUtil"

        /**
         * Sube un archivo de foto al servidor
         * @param file Archivo de imagen a subir
         * @param vin VIN del vehículo (ejemplo: LGXCE6CC1T0063901)
         * @param paso Número de paso (1, 2, 3, 4)
         * @param numeroFoto Número de foto (1, 2, 3, 4)
         * @return Pair<Boolean, String> - (éxito, mensaje/url)
         */
        suspend fun subirFoto(
            urlBase:String,
            file: File,
            vin: String,
            paso: Int,
            numeroFoto: Int
        ): Pair<Boolean, String> = withContext(Dispatchers.IO) {

            // Crear nombre de archivo según el formato especificado
            // Ejemplo: LGXCE6CC1T0063901_Paso_1_Foto_1.jpg
            val nombreArchivo = "${vin}_Paso_${paso}_Foto_${numeroFoto}.jpg"

            try {
                Log.d(TAG, "📤 Iniciando subida de foto: $nombreArchivo")
                Log.d(TAG, "📦 Tamaño del archivo: ${file.length()} bytes")
                Log.d(TAG, "🌐 URL destino: $urlBase")

                // Crear cliente HTTP con timeouts configurados
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                // Crear el cuerpo de la petición multipart/form-data
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        nombreArchivo,
                        file.asRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                // Crear la petición HTTP POST
                val request = Request.Builder()
                    .url(urlBase)
                    .post(requestBody)
                    .build()

                // Ejecutar la petición
                val response = client.newCall(request).execute()

                // Verificar si fue exitosa
                return@withContext if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    Log.d(TAG, "✅ Foto subida exitosamente: $nombreArchivo")
                    Log.d(TAG, "📥 Respuesta del servidor: $responseBody")
                    Pair(true, "Foto subida exitosamente: $nombreArchivo")
                } else {
                    val errorBody = response.body?.string() ?: "Error desconocido"
                    Log.e(TAG, "❌ Error al subir foto. Código HTTP: ${response.code}")
                    Log.e(TAG, "❌ Mensaje de error: $errorBody")
                    Pair(false, "Error al subir foto: ${response.code} - $errorBody")
                }

            } catch (e: IOException) {
                Log.e(TAG, "💥 Error de red al subir foto: ${e.message}")
                e.printStackTrace()
                return@withContext Pair(false, "Error de conexión: ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error inesperado al subir foto: ${e.message}")
                e.printStackTrace()
                return@withContext Pair(false, "Error inesperado: ${e.message}")
            }
        }

        /**
         * Verifica si el servidor está accesible
         * Útil para comprobar la conexión antes de intentar subir
         */
        suspend fun verificarConexion(): Boolean = withContext(Dispatchers.IO) {
            return@withContext try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder()
                    .url("https://softsystemmx.com")
                    .head()
                    .build()

                val response = client.newCall(request).execute()
                val conectado = response.isSuccessful

                Log.d(TAG, if (conectado) "✅ Servidor accesible" else "❌ Servidor no accesible")
                conectado
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error verificando conexión: ${e.message}")
                false
            }
        }
    }
}