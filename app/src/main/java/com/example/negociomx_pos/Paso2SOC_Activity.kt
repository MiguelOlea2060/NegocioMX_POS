package com.example.negociomx_pos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.Paso2LogVehiculo
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.databinding.ActivityPaso2SocBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Paso2SOC_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso2SocBinding
    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: Vehiculo? = null

    // Variables para manejo de loading
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // Variables para fotos
    private var evidencia1File: File? = null
    private var evidencia2File: File? = null
    private var evidencia3File: File? = null
    private var evidencia4File: File? = null
    private var evidencia1Capturada: Boolean = false
    private var evidencia2Capturada: Boolean = false
    private var evidencia3Capturada: Boolean = false
    private var evidencia4Capturada: Boolean = false
    private var currentPhotoType: Int = 0
    private var fotoUri: Uri? = null

    // Variables para control de datos
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.Id?.toInt()!!
    private var paso2LogVehiculoExistente: Paso2LogVehiculo? = null

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
        binding = ActivityPaso2SocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
        verificarPermisos()
    }

    private fun configurarEventos() {
        // ✅ CONFIGURAR FOCUS EN VIN
        binding.etVIN.requestFocus()

        // ✅ CAPTURA DE ENTER EN EL VIN
        binding.etVIN.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                verificaVINSuministrado()
                return@setOnKeyListener true
            }
            false
        }

        // ✅ BOTÓN CONSULTAR VEHÍCULO
        binding.btnConsultarVehiculo.setOnClickListener {
            verificaVINSuministrado()
        }

        // ✅ BOTONES DE EVIDENCIAS
        binding.btnEvidencia1.setOnClickListener {
            if (paso2LogVehiculoExistente?.TieneFoto1 == true) {
                verFotoExistente(1)
            } else {
                capturarEvidencia(1)
            }
        }

        binding.btnEvidencia2.setOnClickListener {
            if (paso2LogVehiculoExistente?.TieneFoto2 == true) {
                verFotoExistente(2)
            } else {
                capturarEvidencia(2)
            }
        }

        binding.btnEvidencia3.setOnClickListener {
            if (paso2LogVehiculoExistente?.TieneFoto3 == true) {
                verFotoExistente(3)
            } else {
                capturarEvidencia(3)
            }
        }

        binding.btnEvidencia4.setOnClickListener {
            if (paso2LogVehiculoExistente?.TieneFoto4 == true) {
                verFotoExistente(4)
            } else {
                capturarEvidencia(4)
            }
        }

        // ✅ BOTÓN GUARDAR
        binding.btnGuardarPaso2.setOnClickListener {
            guardarPaso2()
        }

        // ✅ CONFIGURAR LOADING CONTAINER
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)
    }

    private fun verificaVINSuministrado() {
        val vin = binding.etVIN.text.toString().trim()
        if (vin.isNotEmpty()) {
            consultarVehiculo(vin)
        } else {
            Toast.makeText(this, "Ingrese un VIN válido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            try {
                Log.d("Paso2SOC", "🔍 Consultando vehículo con VIN: $vin")
                mostrarCargaConsulta()

                Toast.makeText(this@Paso2SOC_Activity, "Consultando vehículo...", Toast.LENGTH_SHORT).show()

                val vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin)
                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    // ✅ CONSULTAR FOTOS PASO2 EXISTENTES
                    paso2LogVehiculoExistente = dalVehiculo.consultarFotosPaso2Existentes(vehiculo.Id.toInt())

                    mostrarInformacionVehiculo(vehiculo)
                    mostrarSeccionEvidencias()
                    configurarBotonesSegunFotos()

                    val totalFotos = contarFotosExistentes()
                    if (totalFotos > 0) {
                        Toast.makeText(
                            this@Paso2SOC_Activity,
                            "✅ Vehículo encontrado. Ya tiene $totalFotos foto(s) del Paso 2 registrada(s)",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@Paso2SOC_Activity,
                            "✅ Vehículo encontrado. Sin fotos del Paso 2 previas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    ocultarCargaConsulta()
                } else {
                    ocultarSecciones()
                    Toast.makeText(this@Paso2SOC_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                    ocultarCargaConsulta()
                    binding.etVIN.selectAll()
                }

            } catch (e: Exception) {
                Log.e("Paso2SOC", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso2SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                ocultarCargaConsulta()
                binding.etVIN.selectAll()
            }
        }
    }

    private fun mostrarInformacionVehiculo(vehiculo: Vehiculo) {
        binding.apply {
            tvBlVehiculo.text = "MBL: ${vehiculo.BL}"
            tvMarcaModeloAnnio.text = "${vehiculo.Marca} - ${vehiculo.Modelo}, ${vehiculo.Anio}"
            tvColorExterior.text = "Color Ext.: ${vehiculo.ColorExterior}"
            tvColorInterior.text = "Color Int.: ${vehiculo.ColorInterior}"
            tvTipoCombustible.text = "Combustible: ${vehiculo.TipoCombustible}"
            tvTipoVehiculo.text = "Tipo de Vehiculo: ${vehiculo.TipoVehiculo}"

            layoutInfoVehiculo.visibility = View.VISIBLE
        }
    }

    private fun mostrarSeccionEvidencias() {
        binding.apply {
            layoutEvidencias.visibility = View.VISIBLE
            btnGuardarPaso2.visibility = View.VISIBLE
        }
    }

    private fun ocultarSecciones() {
        binding.apply {
            layoutInfoVehiculo.visibility = View.GONE
            layoutEvidencias.visibility = View.GONE
            btnGuardarPaso2.visibility = View.GONE
        }
    }

    private fun configurarBotonesSegunFotos() {
        paso2LogVehiculoExistente?.let { paso2 ->
            // Configurar botón evidencia 1
            if (paso2.TieneFoto1) {
                binding.btnEvidencia1.text = "👁️ Ver Foto 1"
                binding.tvEstadoEvidencia1.text = "📷"
            } else {
                binding.btnEvidencia1.text = "📷 Foto 1"
                binding.tvEstadoEvidencia1.text = "❌"
            }

            // Configurar botón evidencia 2
            if (paso2.TieneFoto2) {
                binding.btnEvidencia2.text = "👁️ Ver Foto 2"
                binding.tvEstadoEvidencia2.text = "📷"
            } else {
                binding.btnEvidencia2.text = "📷 Foto 2"
                binding.tvEstadoEvidencia2.text = "❌"
            }

            // Configurar botón evidencia 3
            if (paso2.TieneFoto3) {
                binding.btnEvidencia3.text = "👁️ Ver Foto 3"
                binding.tvEstadoEvidencia3.text = "📷"
            } else {
                binding.btnEvidencia3.text = "📷 Foto 3"
                binding.tvEstadoEvidencia3.text = "❌"
            }

            // Configurar botón evidencia 4
            if (paso2.TieneFoto4) {
                binding.btnEvidencia4.text = "👁️ Ver Foto 4"
                binding.tvEstadoEvidencia4.text = "📷"
            } else {
                binding.btnEvidencia4.text = "📷 Foto 4"
                binding.tvEstadoEvidencia4.text = "❌"
            }
        }
    }

    private fun contarFotosExistentes(): Int {
        return paso2LogVehiculoExistente?.let { paso2 ->
            var count = 0
            if (paso2.TieneFoto1) count++
            if (paso2.TieneFoto2) count++
            if (paso2.TieneFoto3) count++
            if (paso2.TieneFoto4) count++
            count
        } ?: 0
    }

    private fun capturarEvidencia(numeroEvidencia: Int) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA
        when (numeroEvidencia) {
            1 -> if (evidencia1Capturada) {
                Toast.makeText(this, "Ya tiene evidencia 1 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
                return
            }
            2 -> if (evidencia2Capturada) {
                Toast.makeText(this, "Ya tiene evidencia 2 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
                return
            }
            3 -> if (evidencia3Capturada) {
                Toast.makeText(this, "Ya tiene evidencia 3 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
                return
            }
            4 -> if (evidencia4Capturada) {
                Toast.makeText(this, "Ya tiene evidencia 4 capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        try {
            currentPhotoType = numeroEvidencia
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "Paso2_${numeroEvidencia}_${timeStamp}.jpg"
            val storageDir = File(getExternalFilesDir(null), "Paso2_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso2SOC", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarFoto(uri: Uri) {
        try {
            Log.d("Paso2SOC", "📸 Procesando foto para evidencia $currentPhotoType")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(this@Paso2SOC_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(this@Paso2SOC_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoFinal = if (archivoLocal.length() > 4.5 * 1024 * 1024) {
                Log.d("Paso2SOC", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal)
            } else {
                archivoLocal
            }

            // ✅ GUARDAR REFERENCIA DEL ARCHIVO SEGÚN LA EVIDENCIA
            when (currentPhotoType) {
                1 -> {
                    evidencia1File = archivoFinal
                    evidencia1Capturada = true
                    binding.tvEstadoEvidencia1.text = "📷"
                    Toast.makeText(this@Paso2SOC_Activity, "✅ Evidencia 1 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    evidencia2File = archivoFinal
                    evidencia2Capturada = true
                    binding.tvEstadoEvidencia2.text = "📷"
                    Toast.makeText(this@Paso2SOC_Activity, "✅ Evidencia 2 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
                3 -> {
                    evidencia3File = archivoFinal
                    evidencia3Capturada = true
                    binding.tvEstadoEvidencia3.text = "📷"
                    Toast.makeText(this@Paso2SOC_Activity, "✅ Evidencia 3 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
                4 -> {
                    evidencia4File = archivoFinal
                    evidencia4Capturada = true
                    binding.tvEstadoEvidencia4.text = "📷"
                    Toast.makeText(this@Paso2SOC_Activity, "✅ Evidencia 4 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
            }

            Log.d("Paso2SOC", "✅ Evidencia $currentPhotoType lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso2SOC", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this@Paso2SOC_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verFotoExistente(numeroFoto: Int) {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso2SOC_Activity, "Cargando foto...", Toast.LENGTH_SHORT).show()

                val fotoBase64 = dalVehiculo.obtenerFotoBase64Paso2(vehiculo.Id.toInt(), numeroFoto)

                if (fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    mostrarDialogoFoto(fotoBase64, numeroFoto)
                } else {
                    Toast.makeText(this@Paso2SOC_Activity, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("Paso2SOC", "Error cargando foto: ${e.message}")
                Toast.makeText(this@Paso2SOC_Activity, "Error cargando foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoFoto(fotoBase64: String, numeroFoto: Int) {
        try {
            // Convertir Base64 a Bitmap
            val decodedBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                // Crear diálogo personalizado
                val dialog = android.app.AlertDialog.Builder(this)
                val imageView = android.widget.ImageView(this)

                // Configurar ImageView
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true

                // Configurar diálogo
                dialog.setTitle("Paso 2 - Evidencia $numeroFoto")
                dialog.setView(imageView)
                dialog.setPositiveButton("Cerrar") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }

                val alertDialog = dialog.create()
                alertDialog.show()

                // Ajustar tamaño del diálogo
                val window = alertDialog.window
                window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.9).toInt(),
                    (resources.displayMetrics.heightPixels * 0.7).toInt()
                )

            } else {
                Toast.makeText(this, "Error decodificando la imagen", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("Paso2SOC", "Error mostrando foto: ${e.message}")
            Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarPaso2() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ VALIDAR QUE TENGA AL MENOS 1 FOTO
        val totalFotosCapturadas = listOf(evidencia1Capturada, evidencia2Capturada, evidencia3Capturada, evidencia4Capturada).count { it }
        if (totalFotosCapturadas == 0) {
            Toast.makeText(this, "Debe capturar al menos 1 foto para guardar", Toast.LENGTH_LONG).show()
            return
        }

        mostrarCargaConMensajes()

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso2SOC_Activity, "Guardando evidencias del Paso 2...", Toast.LENGTH_SHORT).show()

                // ✅ 1. CREAR O OBTENER REGISTRO PASO2
                var idPaso2LogVehiculo = paso2LogVehiculoExistente?.IdPaso2LogVehiculo ?: 0

                if (idPaso2LogVehiculo == 0) {
                    // Crear nuevo registro
                    idPaso2LogVehiculo = dalVehiculo.insertarPaso2LogVehiculo(
                        idVehiculo = vehiculo.Id.toInt(),
                        idUsuarioNube = idUsuarioNubeAlta
                    )
                }

                if (idPaso2LogVehiculo > 0) {
                    Log.d("Paso2SOC", "✅ Registro Paso2 con ID: $idPaso2LogVehiculo")

                    // ✅ 2. GUARDAR FOTOS
                    var exitoFotos = true

                    if (evidencia1Capturada && evidencia1File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia1File!!)
                        if (fotoBase64 != null) {
                            exitoFotos = exitoFotos && dalVehiculo.actualizarFotoPaso2(idPaso2LogVehiculo, 1, fotoBase64)
                        }
                    }

                    if (evidencia2Capturada && evidencia2File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia2File!!)
                        if (fotoBase64 != null) {
                            exitoFotos = exitoFotos && dalVehiculo.actualizarFotoPaso2(idPaso2LogVehiculo, 2, fotoBase64)
                        }
                    }

                    if (evidencia3Capturada && evidencia3File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia3File!!)
                        if (fotoBase64 != null) {
                            exitoFotos = exitoFotos && dalVehiculo.actualizarFotoPaso2(idPaso2LogVehiculo, 3, fotoBase64)
                        }
                    }

                    if (evidencia4Capturada && evidencia4File != null) {
                        val fotoBase64 = convertirImagenABase64(evidencia4File!!)
                        if (fotoBase64 != null) {
                            exitoFotos = exitoFotos && dalVehiculo.actualizarFotoPaso2(idPaso2LogVehiculo, 4, fotoBase64)
                        }
                    }

                    ocultarCarga()

                    if (exitoFotos) {
                        Toast.makeText(this@Paso2SOC_Activity,
                            "✅ Evidencias del Paso 2 guardadas exitosamente ($totalFotosCapturadas foto(s))",
                            Toast.LENGTH_LONG).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(this@Paso2SOC_Activity,
                            "⚠️ Algunas fotos no se pudieron guardar",
                            Toast.LENGTH_LONG).show()
                    }

                } else {
                    ocultarCarga()
                    Toast.makeText(this@Paso2SOC_Activity, "❌ Error creando registro del Paso 2", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("Paso2SOC", "💥 Error guardando Paso 2: ${e.message}")
                Toast.makeText(this@Paso2SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ MÉTODOS AUXILIARES (REUTILIZADOS DEL PASO 1)
    private fun obtenerArchivoDesdeUri(uri: Uri): File? {
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
                val tempFile = File(getExternalFilesDir(null), "temp_paso2_photo_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso2SOC", "Error obteniendo archivo desde URI: ${e.message}")
            null
        }
    }

    private fun comprimirImagen(archivoOriginal: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath)

            val maxSize = 2048
            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_paso2_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso2SOC", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido

        } catch (e: Exception) {
            Log.e("Paso2SOC", "Error comprimiendo imagen: ${e.message}")
            archivoOriginal
        }
    }

    private fun convertirImagenABase64(archivo: File): String? {
        return try {
            val bytes = archivo.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Paso2SOC", "Error convirtiendo imagen a Base64: ${e.message}")
            null
        }
    }

    private fun mostrarCargaConMensajes() {
        loadingContainer.visibility = View.VISIBLE
        binding.btnGuardarPaso2.isEnabled = false
        binding.btnGuardarPaso2.alpha = 0.5f

        val mensajes = arrayOf(
            "Preparando fotos..." to "Organizando evidencias",
            "Comprimiendo imágenes..." to "Optimizando calidad",
            "Enviando a servidor..." to "Guardando en base de datos",
            "Finalizando..." to "Completando proceso"
        )

        var mensajeIndex = 0
        loadingHandler = Handler(Looper.getMainLooper())

        loadingRunnable = object : Runnable {
            override fun run() {
                if (mensajeIndex < mensajes.size) {
                    tvLoadingText.text = mensajes[mensajeIndex].first
                    tvLoadingSubtext.text = mensajes[mensajeIndex].second
                    mensajeIndex++
                    loadingHandler?.postDelayed(this, 2000)
                }
            }
        }
        loadingRunnable?.let { loadingHandler?.post(it) }
    }

    private fun ocultarCarga() {
        loadingContainer.visibility = View.GONE
        binding.btnGuardarPaso2.isEnabled = true
        binding.btnGuardarPaso2.alpha = 1.0f

        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun mostrarCargaConsulta() {
        loadingContainer.visibility = View.VISIBLE
        binding.btnConsultarVehiculo.isEnabled = false
        binding.btnConsultarVehiculo.alpha = 0.5f
    }

    private fun ocultarCargaConsulta() {
        loadingContainer.visibility = View.GONE
        binding.btnConsultarVehiculo.isEnabled = true
        binding.btnConsultarVehiculo.alpha = 1.0f
    }

    private fun limpiarFormulario() {
        binding.apply {
            etVIN.setText("")
            tvEstadoEvidencia1.text = "❌"
            tvEstadoEvidencia2.text = "❌"
            tvEstadoEvidencia3.text = "❌"
            tvEstadoEvidencia4.text = "❌"
        }

        vehiculoActual = null
        evidencia1File = null
        evidencia2File = null
        evidencia3File = null
        evidencia4File = null
        evidencia1Capturada = false
        evidencia2Capturada = false
        evidencia3Capturada = false
        evidencia4Capturada = false
        paso2LogVehiculoExistente = null
        ocultarSecciones()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}
