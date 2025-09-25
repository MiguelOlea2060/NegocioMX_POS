package com.example.negociomx_pos.Utils

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.DAL.DALPaso1SOC
import com.example.negociomx_pos.DAL.DALVehiculo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DescargadorFotos(private val context: Context) {

    companion object {
        private const val TAG = "DescargadorFotos"
        private const val CARPETA_BASE = "NegocioMX_Fotos"
    }

    enum class OpcionArchivo {
        PREGUNTAR, REEMPLAZAR_TODO, OMITIR_TODO
    }

    private var opcionGlobal: OpcionArchivo = OpcionArchivo.PREGUNTAR

    suspend fun descargarFotosVehiculo(
        registro: Paso1SOCItem,
        onProgress: (String, String) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) = withContext(Dispatchers.IO) {

        try {
            Log.d(TAG, "🚀 Iniciando descarga de fotos para VIN: ${registro.VIN}")

            opcionGlobal = OpcionArchivo.PREGUNTAR
            var nombreCarpeta=registro.VIN

            var bllUtil=BLLUtils()
            val dalVeh=DALVehiculo()

            var fotosDescargadas = 0
            var errores = 0

            var existeFoto= true
            var contadorFotos=1
            var salir=false
            var paso=1
            var totalFotos=registro.CantidadFotos
            do {
                existeFoto=false
                if(contadorFotos==1 && registro.FechaAltaFoto1.trim().isNotEmpty())
                    existeFoto=true
                else if (contadorFotos==2 && registro.FechaAltaFoto2.trim().isNotEmpty())
                    existeFoto=true
                else if (contadorFotos==3 && registro.FechaAltaFoto3.trim().isNotEmpty())
                    existeFoto=true
                else if(contadorFotos==4 && registro.FechaAltaFoto4.trim().isNotEmpty())
                    existeFoto=true

                if(existeFoto) {
                    val nombreArchivo = "${registro.VIN}_Paso_${paso}_${contadorFotos}.jpg"
                    withContext(Dispatchers.Main) {
                        onProgress(
                            "Descargando foto $contadorFotos de ${totalFotos}",
                            "Procesando: $nombreArchivo"
                        )
                    }

                    val nombreAux=Environment.DIRECTORY_PICTURES+"/"+ nombreCarpeta+"/"
                    val carpetaVIN = File(nombreAux,nombreArchivo)
                    if (carpetaVIN.isFile || carpetaVIN.exists()) {
                        val lista = carpetaVIN.listFiles()
                        if(lista!=null)
                        {

                        }
                    }
                    val fotoBase64 = dalVeh.obtenerFotoBase64Paso1(registro.IdVehiculo,contadorFotos)
                    try {
                        var uri = bllUtil.saveBitmapToFile(context, fotoBase64!!,nombreCarpeta,nombreArchivo)
                        if (uri!=null) {
                            fotosDescargadas++
                            Log.d(TAG, "✅ Foto $contadorFotos descargada exitosamente")
                        } else {
                            errores++
                            Log.e(TAG, "❌ Error descargando foto $contadorFotos")
                        }

                    } catch (e: Exception) {
                        errores++
                        Log.e(TAG, "💥 Excepción descargando foto $contadorFotos: ${e.message}")
                    }
                }
                else
                    salir=true

                contadorFotos++
            }while(!salir)

            val mensaje = buildString {
                append("✅ Descarga completada para VIN: ${registro.VIN}\n\n")
                append("📊 RESUMEN:\n")
                append("📥 Fotos descargadas: $fotosDescargadas\n")
                append("\n📁 UBICACIÓN:\n")
                append("Almacenamiento interno de la app\n")
                append("Carpeta: ${registro.VIN}\n\n")
                append("📱 PARA ACCEDER A LAS FOTOS:\n")
                append("1. Abre el Administrador de archivos\n")
                append("2. Ve a 'Almacenamiento interno'\n")
                append("5. Ve a 'files' > '${registro.VIN}'\n\n")
                append("💡 Las fotos están guardadas dentro\n")
            }

            withContext(Dispatchers.Main) {
                onComplete(fotosDescargadas > 0, mensaje)
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 Error general en descarga: ${e.message}")
            withContext(Dispatchers.Main) {
                onComplete(false, "Error general: ${e.message}")
            }
        }
    }

    private fun crearCarpetaVIN(vin: String): File? {
        return try {
            // ✅ USAR DIRECTORIO INTERNO DE LA APP - NO REQUIERE PERMISOS
            val directorioBase = File(context.filesDir, CARPETA_BASE)
            val carpetaVIN = File(directorioBase, vin)

            if (!carpetaVIN.exists()) {
                val creada = carpetaVIN.mkdirs()
                if (creada) {
                    Log.d(TAG, "📁 Carpeta creada: ${carpetaVIN.absolutePath}")
                } else {
                    Log.e(TAG, "❌ No se pudo crear carpeta: ${carpetaVIN.absolutePath}")
                    return null
                }
            } else {
                Log.d(TAG, "📁 Carpeta ya existe: ${carpetaVIN.absolutePath}")
            }

            carpetaVIN
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error creando carpeta: ${e.message}")
            null
        }
    }

    private suspend fun manejarArchivoExistente(nombreArchivo: String, archivo: File): OpcionArchivo {
        return when (opcionGlobal) {
            OpcionArchivo.REEMPLAZAR_TODO -> OpcionArchivo.REEMPLAZAR_TODO
            OpcionArchivo.OMITIR_TODO -> OpcionArchivo.OMITIR_TODO
            OpcionArchivo.PREGUNTAR -> {
                withContext(Dispatchers.Main) {
                    mostrarDialogoArchivoExistente(nombreArchivo, archivo)
                }
            }
        }
    }

    private suspend fun mostrarDialogoArchivoExistente(nombreArchivo: String, archivo: File): OpcionArchivo {
        return withContext(Dispatchers.Main) {
            suspendCoroutine { continuation ->
                val tamaño = archivo.length() / 1024
                val mensaje = "El archivo '$nombreArchivo' ya existe (${tamaño}KB).\n¿Qué desea hacer?"

                // ✅ VARIABLE PARA CONTROLAR SI YA SE RESUMIÓ
                var yaResumido = false

                AlertDialog.Builder(context)
                    .setTitle("Archivo Existente")
                    .setMessage(mensaje)
                    .setPositiveButton("Reemplazar") { _, _ ->
                        if (!yaResumido) {
                            yaResumido = true
                            continuation.resume(OpcionArchivo.PREGUNTAR)
                        }
                    }
                    .setNegativeButton("Omitir") { _, _ ->
                        if (!yaResumido) {
                            yaResumido = true
                            continuation.resume(OpcionArchivo.PREGUNTAR)
                        }
                    }
                    .setNeutralButton("Reemplazar Todo") { _, _ ->
                        if (!yaResumido) {
                            yaResumido = true
                            opcionGlobal = OpcionArchivo.REEMPLAZAR_TODO
                            continuation.resume(OpcionArchivo.REEMPLAZAR_TODO)
                        }
                    }
                    .setOnDismissListener {
                        if (!yaResumido) {  // ✅ AHORA USA LA VARIABLE LOCAL
                            yaResumido = true
                            continuation.resume(OpcionArchivo.PREGUNTAR)
                        }
                    }
                    .show()
            }
        }
    }
    private suspend fun descargarArchivo(url: String, archivoDestino: File): Boolean {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream: InputStream = connection.inputStream
                val outputStream = FileOutputStream(archivoDestino)

                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }

                outputStream.close()
                inputStream.close()
                connection.disconnect()

                true
            } else {
                Log.e(TAG, "❌ Error HTTP: ${connection.responseCode} para URL: $url")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error descargando archivo: ${e.message}")
            false
        }
    }
}