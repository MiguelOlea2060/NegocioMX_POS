package com.example.negociomx_pos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
//import com.example.negociomx_pos.Utils.FileUploadUtil
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.databinding.ActivityPaso1SocBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Paso1SOC_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso1SocBinding
    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: Vehiculo? = null
  //  private var evidencia1NombreArchivo: String = ""
  //  private var evidencia2NombreArchivo: String = ""

    private var evidencia1File: File? = null
    private var evidencia2File: File? = null
    private var evidencia1Capturada: Boolean = false
    private var evidencia2Capturada: Boolean = false
    private var currentPhotoType: Int = 0 // Para saber qué evidencia estamos capturando
    private var fotoUri: Uri? = null
    private var vehiculo: Vehiculo? = null

    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.Id?.toInt()!!// Reemplaza con el ID del usuario actual
    private var fotosExistentes: Int = 0 // Para controlar cuántas fotos ya existen

    // ✅ LAUNCHER PARA ESCÁNER DE CÓDIGOS
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            binding.etVIN.setText(result.contents)
            Toast.makeText(this, "VIN escaneado: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ LAUNCHER PARA CÁMARA
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fotoUri?.let { uri ->
                procesarFoto(uri)
            }
        } else {
            Toast.makeText(this, "Error capturando foto", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ LAUNCHER PARA PERMISOS
    private val permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso1SocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
        verificarPermisos()
    }

    private fun configurarEventos() {
        // ✅ BOTÓN ESCANEAR VIN
        binding.btnEscanearVIN.setOnClickListener {
            iniciarEscaneoVIN()
        }

        // ✅ BOTÓN CONSULTAR VEHÍCULO
        binding.btnConsultarVehiculo.setOnClickListener {
            val vin = binding.etVIN.text.toString().trim()
            if (vin.isNotEmpty()) {
                consultarVehiculo(vin)
            } else {
                Toast.makeText(this, "Ingrese un VIN válido", Toast.LENGTH_SHORT).show()
            }
        }

        // ✅ BOTONES DE EVIDENCIAS
        binding.btnEvidencia1.setOnClickListener {
            capturarEvidencia(1)
        }

        binding.btnEvidencia2.setOnClickListener {
            capturarEvidencia(2)
        }

        // ✅ BOTÓN GUARDAR SOC
        binding.btnGuardarSOC.setOnClickListener {
            guardarSOC()
        }
    }

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarEscaneoVIN() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Escanee el código VIN del vehículo")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)

        barcodeLauncher.launch(options)
    }

   /* private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            try {
                Log.d("Paso1SOC", "🔍 Consultando vehículo con VIN: $vin")

                // Mostrar loading
                Toast.makeText(this@Paso1SOC_Activity, "Consultando vehículo...", Toast.LENGTH_SHORT).show()

                val vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin)

                if (vehiculo != null) {
                    vehiculoActual = vehiculo
                    mostrarInformacionVehiculo(vehiculo)
                    mostrarSeccionesSOC()
                    Toast.makeText(this@Paso1SOC_Activity, "✅ Vehículo encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    ocultarSeccionesSOC()
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }*/
//nueva
    private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            try {
                Log.d("Paso1SOC", "🔍 Consultando vehículo con VIN: $vin")

                Toast.makeText(this@Paso1SOC_Activity, "Consultando vehículo...", Toast.LENGTH_SHORT).show()

                 vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin)
               /* if ( vh != null)
                 vehiculo =  vh
                     dalVehiculo.consultarVehiculoPorVIN(vin)*/

                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    // ✅ CONSULTAR FOTOS EXISTENTES
                    fotosExistentes = dalVehiculo.consultarFotosExistentes(vehiculo?.Id?.toInt()!!)

                    mostrarInformacionVehiculo(vehiculo!!)
                    mostrarSeccionesSOC()

                    // ✅ MOSTRAR INFORMACIÓN SOBRE FOTOS EXISTENTES
                    if (fotosExistentes > 0) {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "✅ Vehículo encontrado. Ya tiene $fotosExistentes foto(s) registrada(s)",
                            Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "✅ Vehículo encontrado. Sin fotos previas",
                            Toast.LENGTH_SHORT).show()
                    }

                } else {
                    ocultarSeccionesSOC()
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarInformacionVehiculo(vehiculo: Vehiculo) {
        binding.apply {
            tvMarcaModelo.text = "Marca y Modelo: ${vehiculo.Marca} ${vehiculo.Modelo}"
            tvAnio.text = "Año: ${vehiculo.Anio}"
            tvColor.text = "Color: ${vehiculo.Color}"
            tvPlaca.text = "Placa: ${vehiculo.Placa}"

            // Mostrar datos SOC existentes
            etOdometro.setText(vehiculo.Odometro.toString())
            etBateria.setText(vehiculo.Bateria.toString())
            cbModoTransporte.isChecked = vehiculo.ModoTransporte
            cbRequiereRecarga.isChecked = vehiculo.RequiereRecarga

            layoutInfoVehiculo.visibility = View.VISIBLE
        }
    }

    private fun mostrarSeccionesSOC() {
        binding.apply {
            layoutSOC.visibility = View.VISIBLE
            layoutEvidencias.visibility = View.VISIBLE
            btnGuardarSOC.visibility = View.VISIBLE
        }
    }

    private fun ocultarSeccionesSOC() {
        binding.apply {
            layoutInfoVehiculo.visibility = View.GONE
            layoutSOC.visibility = View.GONE
            layoutEvidencias.visibility = View.GONE
            btnGuardarSOC.visibility = View.GONE
        }
    }

   /* private fun capturarEvidencia(numeroEvidencia: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            currentPhotoType = numeroEvidencia // Guardar qué evidencia estamos capturando
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "SOC_${numeroEvidencia}_${timeStamp}.jpg"
            val storageDir = File(getExternalFilesDir(null), "SOC_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            // Guardar qué evidencia estamos capturando
            //photoFile.writeText(numeroEvidencia.toString())

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }*/

  /*  private fun capturarEvidencia(numeroEvidencia: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // ✅ VALIDAR LÍMITE DE FOTOS
        val totalFotosPermitidas = if (fotosExistentes == 0) 2 else 2 // Máximo 2 fotos siempre
        val fotosActuales = fotosExistentes +
                (if (evidencia1NombreArchivo.isNotEmpty()) 1 else 0) +
                (if (evidencia2NombreArchivo.isNotEmpty()) 1 else 0)

        if (fotosActuales >= totalFotosPermitidas) {
            Toast.makeText(this,
                "Ya se alcanzó el límite máximo de $totalFotosPermitidas fotos para este vehículo",
                Toast.LENGTH_LONG).show()
            return
        }

        try {
            currentPhotoType = numeroEvidencia
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "SOC_${numeroEvidencia}_${timeStamp}.jpg"
            val storageDir = File(getExternalFilesDir(null), "SOC_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }
*/
  private fun capturarEvidencia(numeroEvidencia: Int) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
          permisoLauncher.launch(Manifest.permission.CAMERA)
          return
      }

      // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA
      if (numeroEvidencia == 1 && evidencia1Capturada) {
          Toast.makeText(this, "Ya tiene evidencia 1 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
          return
      }

      if (numeroEvidencia == 2 && evidencia2Capturada) {
          Toast.makeText(this, "Ya tiene evidencia 2 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
          return
      }

      try {
          currentPhotoType = numeroEvidencia
          val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
          val imageFileName = "SOC_${numeroEvidencia}_${timeStamp}.jpg"
          val storageDir = File(getExternalFilesDir(null), "SOC_Photos")

          if (!storageDir.exists()) {
              storageDir.mkdirs()
          }

          val photoFile = File(storageDir, imageFileName)
          fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

          camaraLauncher.launch(fotoUri)

      } catch (e: Exception) {
          Log.e("Paso1SOC", "Error creando archivo de foto: ${e.message}")
          Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
      }
  }

    private fun obtenerArchivoDesdeUri(uri: Uri): File? {
        return try {
            // Si es una URI de FileProvider, obtener la ruta real
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }

            // Si no funciona, intentar con ContentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempFile = File(getExternalFilesDir(null), "temp_photo_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error obteniendo archivo desde URI: ${e.message}")
            null
        }
    }

    private fun comprimirImagen(archivoOriginal: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath)

            // Calcular nuevo tamaño manteniendo proporción (máximo 2048px)
            val maxSize = 2048
            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Crear archivo comprimido
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso1SOC", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido

        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error comprimiendo imagen: ${e.message}")
            archivoOriginal
        }
    }

    private fun convertirImagenABase64(archivo: File): String? {
        return try {
            val bytes = archivo.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error convirtiendo imagen a Base64: ${e.message}")
            null
        }
    }


    private fun procesarFoto(uri: Uri) {
        try {
            Log.d("Paso1SOC", "📸 Procesando foto para evidencia $currentPhotoType")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(this@Paso1SOC_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ OBTENER EL ARCHIVO LOCAL DESDE LA URI
            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(this@Paso1SOC_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            // ✅ COMPRIMIR IMAGEN SI ES NECESARIO
            val archivoFinal = if (archivoLocal.length() > 4.5 * 1024 * 1024) {
                Log.d("Paso1SOC", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal)
            } else {
                archivoLocal
            }

            // ✅ GUARDAR REFERENCIA DEL ARCHIVO SEGÚN LA EVIDENCIA
            if (currentPhotoType == 1) {
                evidencia1File = archivoFinal
                evidencia1Capturada = true
                binding.tvEstadoEvidencia1.text = "📷"
                Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 1 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
            } else {
                evidencia2File = archivoFinal
                evidencia2Capturada = true
                binding.tvEstadoEvidencia2.text = "📷"
                Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 2 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
            }

            Log.d("Paso1SOC", "✅ Evidencia $currentPhotoType lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso1SOC", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this@Paso1SOC_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

  /*  private fun procesarFoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                Log.d("Paso1SOC", "📸 Procesando foto para evidencia $currentPhotoType")

                val vehiculo = vehiculoActual
                if (vehiculo == null) {
                    Toast.makeText(this@Paso1SOC_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Crear nombre único para el archivo
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val nombreArchivo = "SOC_${vehiculo.VIN}_EV${currentPhotoType}_${timeStamp}.jpg"

                // ✅ CORRECCIÓN: Obtener el archivo real desde la URI
                val archivoLocal = obtenerArchivoDesdeUri(uri)

                if (archivoLocal == null || !archivoLocal.exists()) {
                    Toast.makeText(this@Paso1SOC_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // <CHANGE> Comprimir imagen si es mayor a 4.5MB
                val archivoFinal = if (archivoLocal.length() > 4.5 * 1024 * 1024) {
                    Log.d("Paso1SOC", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                    comprimirImagen(archivoLocal)
                } else {
                    archivoLocal
                }

                // Subir foto al servidor
                Toast.makeText(this@Paso1SOC_Activity, "Subiendo evidencia $currentPhotoType...", Toast.LENGTH_SHORT).show()

                val nombreArchivoSubido = FileUploadUtil.subirFotoAlServidor(
                    archivoFoto = archivoFinal,
                    nombreArchivo = nombreArchivo,
                    vin = vehiculo.VIN
                )

                if (nombreArchivoSubido != null) {
                    // Guardar nombre del archivo según la evidencia
                    if (currentPhotoType == 1) {
                        evidencia1NombreArchivo = nombreArchivoSubido
                        binding.tvEstadoEvidencia1.text = "✅"
                        Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 1 subida al servidor", Toast.LENGTH_SHORT).show()
                    } else {
                        evidencia2NombreArchivo = nombreArchivoSubido
                        binding.tvEstadoEvidencia2.text = "✅"
                        Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 2 subida al servidor", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("Paso1SOC", "✅ Evidencia $currentPhotoType guardada como: $nombreArchivoSubido")
                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error subiendo evidencia $currentPhotoType", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error procesando foto: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }*/


   /* private fun procesarFoto(uri: Uri) {
        lifecycleScope.launch {
            try {
                Log.d("Paso1SOC", "📸 Procesando foto para evidencia $currentPhotoType")

                val vehiculo = vehiculoActual
                if (vehiculo == null) {
                    Toast.makeText(this@Paso1SOC_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Crear nombre único para el archivo
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val nombreArchivo = "SOC_${vehiculo.VIN}_EV${currentPhotoType}_${timeStamp}.jpg"

                // Obtener el archivo local
                val archivoLocal = File(uri.path ?: "")

                if (!archivoLocal.exists()) {
                    Toast.makeText(this@Paso1SOC_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Subir foto al servidor
                Toast.makeText(this@Paso1SOC_Activity, "Subiendo evidencia $currentPhotoType...", Toast.LENGTH_SHORT).show()

                val nombreArchivoSubido = FileUploadUtil.subirFotoAlServidor(
                    archivoFoto = archivoLocal,
                    nombreArchivo = nombreArchivo,
                    vin = vehiculo.VIN
                )

                if (nombreArchivoSubido != null) {
                    // Guardar nombre del archivo según la evidencia
                    if (currentPhotoType == 1) {
                        evidencia1NombreArchivo = nombreArchivoSubido
                        binding.tvEstadoEvidencia1.text = "✅"
                        Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 1 subida al servidor", Toast.LENGTH_SHORT).show()
                    } else {
                        evidencia2NombreArchivo = nombreArchivoSubido
                        binding.tvEstadoEvidencia2.text = "✅"
                        Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 2 subida al servidor", Toast.LENGTH_SHORT).show()
                    }

                    Log.d("Paso1SOC", "✅ Evidencia $currentPhotoType guardada como: $nombreArchivoSubido")
                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error subiendo evidencia $currentPhotoType", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error procesando foto: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }*/
  /*  private fun procesarFoto(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val base64 = convertirBitmapABase64(bitmap)

            // Determinar qué evidencia es basándose en el timestamp
            val file = File(uri.path ?: "")
            val numeroEvidencia = if (file.name.contains("_1_")) 1 else 2

            if (numeroEvidencia == 1) {
                evidencia1Base64 = base64
                binding.tvEstadoEvidencia1.text = "✅"
                Toast.makeText(this, "Evidencia 1 capturada", Toast.LENGTH_SHORT).show()
            } else {
                evidencia2Base64 = base64
                binding.tvEstadoEvidencia2.text = "✅"
                Toast.makeText(this, "Evidencia 2 capturada", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error procesando foto: ${e.message}")
            Toast.makeText(this, "Error procesando foto", Toast.LENGTH_SHORT).show()
        }
    }
*/
  /*  private fun convertirBitmapABase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }*/

   /* private suspend fun subirFotosAlServidor(vehiculo: Vehiculo): Pair<String, String> {
        var evidencia1Nombre = ""
        var evidencia2Nombre = ""

        try {
            // ✅ SUBIR EVIDENCIA 1 SI EXISTE
            if (evidencia1File != null && evidencia1Capturada) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val nombreArchivo1 = "SOC_${vehiculo.VIN}_EV1_${timeStamp}.jpg"

                Toast.makeText(this@Paso1SOC_Activity, "Subiendo evidencia 1...", Toast.LENGTH_SHORT).show()

                val nombreSubido1 = FileUploadUtil.subirFotoAlServidor(
                    archivoFoto = evidencia1File!!,
                    nombreArchivo = nombreArchivo1,
                    vin = vehiculo.VIN
                )

                if (nombreSubido1 != null) {
                    evidencia1Nombre = nombreSubido1
                    Log.d("Paso1SOC", "✅ Evidencia 1 subida: $nombreSubido1")
                } else {
                    throw Exception("Error subiendo evidencia 1")
                }
            }

            // ✅ SUBIR EVIDENCIA 2 SI EXISTE
            if (evidencia2File != null && evidencia2Capturada) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val nombreArchivo2 = "SOC_${vehiculo.VIN}_EV2_${timeStamp}.jpg"

                Toast.makeText(this@Paso1SOC_Activity, "Subiendo evidencia 2...", Toast.LENGTH_SHORT).show()

                val nombreSubido2 = FileUploadUtil.subirFotoAlServidor(
                    archivoFoto = evidencia2File!!,
                    nombreArchivo = nombreArchivo2,
                    vin = vehiculo.VIN
                )

                if (nombreSubido2 != null) {
                    evidencia2Nombre = nombreSubido2
                    Log.d("Paso1SOC", "✅ Evidencia 2 subida: $nombreSubido2")
                } else {
                    throw Exception("Error subiendo evidencia 2")
                }
            }

        } catch (e: Exception) {
            Log.e("Paso1SOC", "💥 Error subiendo fotos: ${e.message}")
            throw e
        }

        return Pair(evidencia1Nombre, evidencia2Nombre)
    }*/


    private fun guardarSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        val odometroText = binding.etOdometro.text.toString().trim()
        val bateriaText = binding.etBateria.text.toString().trim()

        if (odometroText.isEmpty() || bateriaText.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val odometro = odometroText.toIntOrNull() ?: 0
        val bateria = bateriaText.toIntOrNull() ?: 0

        if (bateria < 0 || bateria > 100) {
            Toast.makeText(this, "El nivel de batería debe estar entre 0 y 100", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Guardando SOC y fotos...", Toast.LENGTH_SHORT).show()

                // ✅ 1. INSERTAR DATOS SOC EN LA NUEVA TABLA
                val idPaso1LogVehiculo = dalVehiculo.insertarPaso1LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    odometro = odometro,
                    bateria = bateria,
                    modoTransporte = binding.cbModoTransporte.isChecked,
                    requiereRecarga = binding.cbRequiereRecarga.isChecked,
                    idUsuarioNubeAlta = idUsuarioNubeAlta
                )

                if (idPaso1LogVehiculo > 0) {
                    Log.d("Paso1SOC", "✅ Datos SOC guardados con ID: $idPaso1LogVehiculo")

                    // ✅ 2. GUARDAR FOTOS DIRECTAMENTE EN BD
                    var exitoFotos = true
                    var consecutivo: Short = 1

                    if (evidencia1Capturada && evidencia1File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia1File!!)
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 1,
                            fotoBase64 = fotoBase64
                        )
                        consecutivo++
                    }

                    if (evidencia2Capturada && evidencia2File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia2File!!)
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 2,
                            fotoBase64 = fotoBase64
                        )
                    }

                    if (exitoFotos) {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "✅ SOC y fotos guardados exitosamente en la base de datos",
                            Toast.LENGTH_LONG).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "⚠️ SOC guardado, pero hubo errores guardando las fotos",
                            Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error guardando SOC", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error guardando SOC: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

       /* lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Guardando SOC y fotos...", Toast.LENGTH_SHORT).show()

                // ✅ 1. SUBIR FOTOS AL SERVIDOR PRIMERO
                val (evidencia1Nombre, evidencia2Nombre) = subirFotosAlServidor(vehiculo)

                // ✅ 2. INSERTAR DATOS SOC EN LA NUEVA TABLA
                val idPaso1LogVehiculo = dalVehiculo.insertarPaso1LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    odometro = odometro,
                    bateria = bateria,
                    modoTransporte = binding.cbModoTransporte.isChecked,
                    requiereRecarga = binding.cbRequiereRecarga.isChecked,
                    idUsuarioNubeAlta = idUsuarioNubeAlta
                )

                if (idPaso1LogVehiculo > 0) {
                    Log.d("Paso1SOC", "✅ Datos SOC guardados en Paso1LogVehiculo con ID: $idPaso1LogVehiculo")

                    // ✅ 3. INSERTAR FOTOS EN Paso1LogVehiculoFotos
                    var exitoFotos = true
                    var consecutivo: Short = 1

                    if (evidencia1Capturada && evidencia1Nombre.isNotEmpty()) {
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null, //
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 1 // 1 = evidencia 1
                        )
                        consecutivo++
                    }

                    if (evidencia2Capturada && evidencia2Nombre.isNotEmpty()) {
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 2 // 2 = evidencia 2
                        )
                    }

                    if (exitoFotos) {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "✅ SOC y fotos guardados exitosamente en la base de datos",
                            Toast.LENGTH_LONG).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "⚠️ SOC guardado, pero hubo errores guardando las fotos en BD",
                            Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error guardando SOC", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error guardando SOC: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }*/
    }


   /* private fun guardarSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        val odometroText = binding.etOdometro.text.toString().trim()
        val bateriaText = binding.etBateria.text.toString().trim()

        if (odometroText.isEmpty() || bateriaText.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val odometro = odometroText.toIntOrNull() ?: 0
        val bateria = bateriaText.toIntOrNull() ?: 0

        if (bateria < 0 || bateria > 100) {
            Toast.makeText(this, "El nivel de batería debe estar entre 0 y 100", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Guardando SOC...", Toast.LENGTH_SHORT).show()

                // ✅ 1. INSERTAR EN LA NUEVA TABLA Paso1LogVehiculo
                val idPaso1LogVehiculo = dalVehiculo.insertarPaso1LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    odometro = odometro,
                    bateria = bateria,
                    modoTransporte = binding.cbModoTransporte.isChecked,
                    requiereRecarga = binding.cbRequiereRecarga.isChecked,
                    idUsuarioNubeAlta = idUsuarioNubeAlta
                )

                if (idPaso1LogVehiculo > 0) {
                    Log.d("Paso1SOC", "✅ Datos SOC guardados en Paso1LogVehiculo con ID: $idPaso1LogVehiculo")

                    // ✅ 2. INSERTAR FOTOS EN Paso1LogVehiculoFotos
                    var exitoFotos = true
                    var consecutivo: Short = (fotosExistentes + 1).toShort()

                    if (evidencia1NombreArchivo.isNotEmpty()) {
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null, //
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 1 // 1 = foto de antes
                        )
                        consecutivo++
                    }

                    if (evidencia2NombreArchivo.isNotEmpty()) {
                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null, //
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 2 // 2 = foto de después
                        )
                    }

                    if (exitoFotos) {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "✅ SOC y fotos guardados exitosamente en las nuevas tablas",
                            Toast.LENGTH_LONG).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(this@Paso1SOC_Activity,
                            "⚠️ SOC guardado, pero hubo errores con las fotos",
                            Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error guardando SOC", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error guardando SOC: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }*/

  /*  private fun guardarSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        val odometroText = binding.etOdometro.text.toString().trim()
        val bateriaText = binding.etBateria.text.toString().trim()

        if (odometroText.isEmpty() || bateriaText.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val odometro = odometroText.toIntOrNull() ?: 0
        val bateria = bateriaText.toIntOrNull() ?: 0

        if (bateria < 0 || bateria > 100) {
            Toast.makeText(this, "El nivel de batería debe estar entre 0 y 100", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Guardando SOC...", Toast.LENGTH_SHORT).show()

                val exito = dalVehiculo.actualizarSOC(
                    vin = vehiculo.VIN,
                    odometro = odometro,
                    bateria = bateria,
                    modoTransporte = binding.cbModoTransporte.isChecked,
                    requiereRecarga = binding.cbRequiereRecarga.isChecked,
                    evidencia1 = evidencia1NombreArchivo,
                    evidencia2 = evidencia2NombreArchivo

                )

                if (exito) {
                    Toast.makeText(this@Paso1SOC_Activity, "✅ SOC guardado exitosamente", Toast.LENGTH_LONG).show()
                    limpiarFormulario()
                } else {
                    Toast.makeText(this@Paso1SOC_Activity, "❌ Error guardando SOC", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error guardando SOC: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }*/

   /* private fun limpiarFormulario() {
        binding.apply {
            etVIN.setText("")
            etOdometro.setText("")
            etBateria.setText("")
            cbModoTransporte.isChecked = false
            cbRequiereRecarga.isChecked = false
            tvEstadoEvidencia1.text = "❌"
            tvEstadoEvidencia2.text = "❌"
        }

        vehiculoActual = null
        evidencia1NombreArchivo = ""
        evidencia2NombreArchivo = ""
        ocultarSeccionesSOC()
        fotosExistentes = 0 // Resetear contador de fotos
    }*/
   private fun limpiarFormulario() {
       binding.apply {
           etVIN.setText("")
           etOdometro.setText("")
           etBateria.setText("")
           cbModoTransporte.isChecked = false
           cbRequiereRecarga.isChecked = false
           tvEstadoEvidencia1.text = "❌"
           tvEstadoEvidencia2.text = "❌"
       }

       vehiculoActual = null
       evidencia1File = null
       evidencia2File = null
       evidencia1Capturada = false
       evidencia2Capturada = false
       fotosExistentes = 0
       ocultarSeccionesSOC()
   }


}
