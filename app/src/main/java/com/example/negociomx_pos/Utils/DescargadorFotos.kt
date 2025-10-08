package com.example.negociomx_pos.Utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.example.negociomx_pos.BE.PasoNumLogVehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
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
        registro: PasoNumLogVehiculo,
        onProgress: (String, String) -> Unit,
        onComplete: (Boolean, String) -> Unit
    ) = withContext(Dispatchers.IO) {

        try {
            val manejaGuardadoArchivosEnBD=ParametrosSistema.cfgApp!=null &&
                    ParametrosSistema?.cfgApp!!.ManejaGuardadoArchivosEnBD==true

            var urlVisualArchivos=""
            if(ParametrosSistema.cfgApp!=null)
            {
                urlVisualArchivos=ParametrosSistema?.cfgApp!!.UrlGuardadoArchivos
                if(ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube!!.isNotEmpty())
                    urlVisualArchivos+='/'+ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube!!.replace("~/","")
            }

            Log.d(TAG, "🚀 Iniciando descarga de fotos para VIN: ${registro.VIN}")

            opcionGlobal = OpcionArchivo.PREGUNTAR
            var nombreCarpeta=registro.VIN

            var bllUtil=BLLUtils()
            val dalVeh=DALVehiculo()

            var fotosDescargadas = 0

            var existeFoto= true
            var contador=1
            var contadorFotos=0
            var salir=false
            var totalFotos=registro.CantidadFotos
            do {
                existeFoto=false
                var nombreArchivo = ""
                if(registro.Paso==1) {
                    if (contador == 1 && registro.FechaAltaFoto1.trim().isNotEmpty())
                        existeFoto = true
                    else if (contador == 2 && registro.FechaAltaFoto2.trim().isNotEmpty())
                        existeFoto = true
                    else if (contador == 3 && registro.FechaAltaFoto3.trim().isNotEmpty())
                        existeFoto = true
                    else if (contador == 4 && registro.FechaAltaFoto4.trim().isNotEmpty())
                        existeFoto = true
                }
                else if (registro.Paso==2)
                {
                    if (contador == 1 && registro.TieneFoto1==true)
                        existeFoto = true
                    else if (contador == 2 && registro.TieneFoto2==true)
                        existeFoto = true
                    else if (contador == 3 && registro.TieneFoto3==true)
                        existeFoto = true
                    else if (contador == 4 && registro.TieneFoto4==true)
                        existeFoto = true
                }

                if(existeFoto) {
                    if(contador==1) nombreArchivo=registro.NombreArchivoFoto1
                    else if(contador==2) nombreArchivo=registro.NombreArchivoFoto2
                    else if(contador==3) nombreArchivo=registro.NombreArchivoFoto3
                    else if(contador==4) nombreArchivo=registro.NombreArchivoFoto4
//                    val nombreArchivo = "${registro.VIN}_Paso_${registro.Paso}_${contador}.jpg"
                    withContext(Dispatchers.Main) {
                        onProgress(
                            "Descargando foto $contador de ${totalFotos}",
                            "Procesando: $nombreArchivo"
                        )
                    }
                    val nombreAux=Environment.DIRECTORY_PICTURES+"/"+ nombreCarpeta+"/"
                    val carpetaVIN = File(nombreAux,nombreArchivo)

                    var fotoBase64:String =""
                    if(registro.Paso==1) {
                        if(manejaGuardadoArchivosEnBD) {
                            fotoBase64 = dalVeh.obtenerFotoBase64Paso1(registro.IdVehiculo, contador)!!
                        }
                    }
                    else if(registro.Paso==2) {
                        if (manejaGuardadoArchivosEnBD) {
                            fotoBase64 =dalVeh.obtenerFotoBase64Paso2(registro.IdVehiculo, contador)!!
                        }
                    }
                    else if(registro.Paso==3) {
                            if (manejaGuardadoArchivosEnBD) {
                                fotoBase64 = dalVeh.obtenerFotoBase64Paso3(registro.IdVehiculo)!!
                            }
                        }
                    else if(registro.Paso==4) {
                            if(manejaGuardadoArchivosEnBD) {
                                fotoBase64 = dalVeh.obtenerFotoBase64Paso4(registro.IdVehiculo, contador.toByte())!!
                            }
                        }
                    try {
                        var guardadoCorrecto=false
                        if(manejaGuardadoArchivosEnBD) {
                            var uri = bllUtil.saveBitmapToFile(context, fotoBase64!!, nombreCarpeta, nombreArchivo
                            )
                            guardadoCorrecto=uri!=null
                        }
                        else
                        {
                            val urlCompletaImagen=urlVisualArchivos+'/'+nombreArchivo
                            var bitmap=bllUtil.mLoad(urlCompletaImagen)
                            bllUtil.saveBitmapToFile(context,bitmap!!,nombreCarpeta,nombreArchivo)
                            guardadoCorrecto=true
                        }
                        if (guardadoCorrecto) {
                            fotosDescargadas++
                            Log.d(TAG, "✅ Foto $contador descargada exitosamente")
                        } else {
                            Log.e(TAG, "❌ Error descargando foto $contador")
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "💥 Excepción descargando foto $contador: ${e.message}")
                    }
                    contadorFotos++
                }

                contador++
                if(contadorFotos>=totalFotos) salir=true
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

/*    public fun mLoad(string: String): Bitmap? {
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

    // Function to convert string to URL
    public fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }*/

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