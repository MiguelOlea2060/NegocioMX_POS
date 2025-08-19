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
import com.example.negociomx_pos.Utils.FileUploadUtil
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
    private var evidencia1NombreArchivo: String = ""
    private var evidencia2NombreArchivo: String = ""
    private var currentPhotoType: Int = 0 // Para saber qué evidencia estamos capturando
    private var fotoUri: Uri? = null

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

    private fun consultarVehiculo(vin: String) {
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

    private fun capturarEvidencia(numeroEvidencia: Int) {
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

    private fun procesarFoto(uri: Uri) {
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
    }


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
    }

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
        evidencia1NombreArchivo = ""
        evidencia2NombreArchivo = ""
        ocultarSeccionesSOC()
    }
}
