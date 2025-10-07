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
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.BE.VehiculoPaso1
import com.example.negociomx_pos.BLL.BLLVehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.ApiUploadUtil
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.databinding.ActivityPaso1SocpruebaBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Paso1SOCPrueba_Activity : AppCompatActivity() {

    // Variables para el binding del layout
    private lateinit var binding: ActivityPaso1SocpruebaBinding

    // DAL para acceso a base de datos
    private val dalVehiculo = DALVehiculo()

    // Variables para almacenar información del vehículo
    private var vehiculoActual: Vehiculo? = null
    private var vehiculoPaso1: VehiculoPaso1? = null

    // Variables para el indicador de carga
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // Variables para manejo de fotos
    private var evidencia1File: File? = null
    private var evidencia2File: File? = null
    private var evidencia1Capturada: Boolean = false
    private var evidencia2Capturada: Boolean = false
    private var currentPhotoType: Int = 0
    private var fotoUri: Uri? = null

    // ID del usuario que está usando la app
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.IdUsuario ?: 1

    // ✅ LAUNCHER PARA CÁMARA
    // Este launcher se activa cuando se toma una foto
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            // Si la foto fue tomada exitosamente, procesarla
            fotoUri?.let { uri ->
                procesarFoto(uri)
            }
        } else {
            Toast.makeText(this, "Error capturando foto", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ LAUNCHER PARA PERMISOS
    // Este launcher se activa cuando se solicita permiso de cámara
    private val permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado. No podrá tomar fotos.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando View Binding
        binding = ActivityPaso1SocpruebaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar referencias a elementos del layout
        loadingContainer = binding.loadingContainer
        tvLoadingText = binding.tvLoadingText
        tvLoadingSubtext = binding.tvLoadingSubtext

        // Configurar eventos de los botones
        configurarEventos()

        // Verificar si tenemos permiso de cámara
        verificarPermisos()
    }

    /**
     * Configura todos los eventos de clicks y listeners de la interfaz
     */
    private fun configurarEventos() {
        // Poner foco en el campo de VIN al iniciar
        binding.etVIN.requestFocus()

        // Configurar captura de Enter en el campo VIN
        // Cuando el usuario presiona Enter después de escanear el VIN, se consulta automáticamente
        binding.etVIN.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                verificarVINSuministrado()
                return@setOnKeyListener true
            }
            false
        }

        // Botón para consultar vehículo manualmente
        binding.btnConsultarVehiculo.setOnClickListener {
            verificarVINSuministrado()
        }

        // Botones para capturar evidencias fotográficas
        binding.btnEvidencia1.setOnClickListener {
            capturarEvidencia(1)
        }

        binding.btnEvidencia2.setOnClickListener {
            capturarEvidencia(2)
        }

        // Botón para guardar SOC y subir fotos a la API
        binding.btnGuardarSOC.setOnClickListener {
            guardarSOC()
        }
    }

    /**
     * Verifica si tenemos permiso de cámara, si no lo solicita
     */
    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Verifica que el VIN ingresado sea válido y luego consulta el vehículo
     */
    private fun verificarVINSuministrado() {
        val vin = binding.etVIN.text.toString().trim()

        // Validar que el VIN tenga al menos 17 caracteres (estándar)
        if (vin.isNotEmpty() && vin.length > 16) {
            consultarVehiculo(vin)
        } else {
            Toast.makeText(this, "Ingrese un VIN válido (mínimo 17 caracteres)", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Consulta el vehículo en la base de datos usando el VIN
     */
    private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            val bll = BLLVehiculo()
            try {
                Log.d("Paso1SOCPrueba", "🔍 Consultando vehículo con VIN: $vin")
                mostrarCargaConsulta()

                // Consultar vehículo en la base de datos
                vehiculoPaso1 = dalVehiculo.consultarVehiculoPorVINParaPaso1(vin)

                // Convertir a objeto Vehiculo
                if (vehiculoPaso1 != null) {
                    vehiculoActual = bll.convertToVehiculo(vehiculoPaso1!!)
                }

                if (vehiculoActual != null) {
                    // Si se encontró el vehículo, mostrar su información
                    mostrarInformacionVehiculo(vehiculoActual!!)
                    mostrarSeccionesSOC()

                    Toast.makeText(
                        this@Paso1SOCPrueba_Activity,
                        "✅ Vehículo encontrado",
                        Toast.LENGTH_SHORT
                    ).show()

                    ocultarCargaConsulta()
                } else {
                    // No se encontró el vehículo
                    ocultarSeccionesSOC()
                    Toast.makeText(
                        this@Paso1SOCPrueba_Activity,
                        "❌ Vehículo no encontrado en la base de datos",
                        Toast.LENGTH_LONG
                    ).show()
                    ocultarCargaConsulta()
                    binding.etVIN.selectAll()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOCPrueba", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso1SOCPrueba_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                ocultarCargaConsulta()
                binding.etVIN.selectAll()
            }
        }
    }

    /**
     * Muestra la información del vehículo en la interfaz
     */
    private fun mostrarInformacionVehiculo(vehiculo: Vehiculo) {
        binding.apply {
            tvBlVehiculo.text = "MBL: ${vehiculo.BL}"
            tvMarcaModeloAnnio.text = "${vehiculo.Marca} - ${vehiculo.Modelo}, ${vehiculo.Anio}"
            tvColorExterior.text = "Color Ext.: ${vehiculo.ColorExterior}"
            tvColorInterior.text = "Color Int.: ${vehiculo.ColorInterior}"

            // Mostrar datos SOC existentes (si los hay)
            etOdometro.setText(if (vehiculo.Odometro > 0) vehiculo.Odometro.toString() else "")
            etBateria.setText(if (vehiculo.Bateria > 0) vehiculo.Bateria.toString() else "")
            cbModoTransporte.isChecked = vehiculo.ModoTransporte
            cbRequiereRecarga.isChecked = vehiculo.RequiereRecarga

            layoutInfoVehiculo.visibility = View.VISIBLE
        }
    }

    /**
     * Muestra las secciones de SOC y evidencias
     */
    private fun mostrarSeccionesSOC() {
        binding.apply {
            layoutSOC.visibility = View.VISIBLE
            layoutEvidencias.visibility = View.VISIBLE
            btnGuardarSOC.visibility = View.VISIBLE
        }
    }

    /**
     * Oculta las secciones de SOC y evidencias
     */
    private fun ocultarSeccionesSOC() {
        binding.apply {
            layoutInfoVehiculo.visibility = View.GONE
            layoutSOC.visibility = View.GONE
            layoutEvidencias.visibility = View.GONE
            btnGuardarSOC.visibility = View.GONE
        }
    }

    /**
     * Inicia el proceso de captura de una evidencia fotográfica
     */
    private fun capturarEvidencia(numeroEvidencia: Int) {
        // Verificar permiso de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // Validar que no se capture la misma evidencia dos veces
        if (numeroEvidencia == 1 && evidencia1Capturada) {
            Toast.makeText(this, "Ya tiene evidencia 1 capturada", Toast.LENGTH_SHORT).show()
            return
        }

        if (numeroEvidencia == 2 && evidencia2Capturada) {
            Toast.makeText(this, "Ya tiene evidencia 2 capturada", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            currentPhotoType = numeroEvidencia

            // Crear nombre de archivo temporal
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "SOC_Prueba_${numeroEvidencia}_${timeStamp}.jpg"

            // Crear carpeta para guardar fotos temporales
            val storageDir = File(getExternalFilesDir(null), "SOC_Photos_Prueba")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            // Crear archivo para la foto
            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            // Lanzar la cámara
            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso1SOCPrueba", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Procesa la foto capturada
     */
    private fun procesarFoto(uri: Uri) {
        try {
            Log.d("Paso1SOCPrueba", "📸 Procesando foto para evidencia $currentPhotoType")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener el archivo desde la URI
            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(this, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            // Comprimir la imagen si es muy grande (más de 2.2 MB)
            val tamanoMaximo = (2.2 * 1024 * 1024).toLong()
            val archivoFinal = if (archivoLocal.length() > tamanoMaximo) {
                Log.d("Paso1SOCPrueba", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal)
            } else {
                archivoLocal
            }

            // Guardar referencia del archivo según la evidencia
            when (currentPhotoType) {
                1 -> {
                    evidencia1File = archivoFinal
                    evidencia1Capturada = true
                    binding.tvEstadoEvidencia1.text = "📷"
                    Toast.makeText(this, "✅ Evidencia 1 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    evidencia2File = archivoFinal
                    evidencia2Capturada = true
                    binding.tvEstadoEvidencia2.text = "📷"
                    Toast.makeText(this, "✅ Evidencia 2 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
            }

            Log.d("Paso1SOCPrueba", "✅ Evidencia $currentPhotoType lista para subir a API")

        } catch (e: Exception) {
            Log.e("Paso1SOCPrueba", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Obtiene un archivo File desde una URI
     */
    private fun obtenerArchivoDesdeUri(uri: Uri): File? {
        return try {
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) return file
            }

            // Si no funciona, usar ContentResolver
            val inputStream = contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val tempFile = File(getExternalFilesDir(null), "temp_prueba_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso1SOCPrueba", "Error obteniendo archivo: ${e.message}")
            null
        }
    }

    /**
     * Comprime una imagen para reducir su tamaño
     */
    private fun comprimirImagen(archivoOriginal: File): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath)

            // Calcular nuevo tamaño manteniendo proporción (máximo 3072px)
            val maxSize = 3072
            val ratio = if (bitmap.width > bitmap.height) {
                maxSize.toFloat() / bitmap.width
            } else {
                maxSize.toFloat() / bitmap.height
            }

            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()
            val bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // Guardar imagen comprimida
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_prueba_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso1SOCPrueba", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido
        } catch (e: Exception) {
            Log.e("Paso1SOCPrueba", "Error comprimiendo: ${e.message}")
            archivoOriginal
        }
    }

    /**
     * MetoDO PRINCIPAL: Guarda los datos SOC y sube las fotos a la API
     * Esta es la diferencia principal con Paso1SOC_Activity
     */
    private fun guardarSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero escanea el código QR del vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        val odometroText = binding.etOdometro.text.toString().trim()
        val bateriaText = binding.etBateria.text.toString().trim()

        // Validar campos obligatorios
        if (odometroText.isEmpty() || bateriaText.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val bateria = bateriaText.toIntOrNull() ?: 0
        if (bateria < 0 || bateria > 100) {
            Toast.makeText(this, "La batería debe estar entre 0 y 100", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que al menos se haya capturado una foto
        if (!evidencia1Capturada && !evidencia2Capturada) {
            Toast.makeText(this, "Debe capturar al menos 1 foto antes de guardar", Toast.LENGTH_LONG).show()
            return
        }

        // Mostrar indicador de carga
        mostrarCargaConMensajes()

        lifecycleScope.launch {
            try {
                Log.d("Paso1SOCPrueba", "💾 Iniciando proceso de guardado")

                // ✅ PASO 1: Guardar datos SOC en la base de datos
                val idPaso1LogVehiculo = dalVehiculo.insertarOActualizarPaso1LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    odometro = odometroText.toInt(),
                    bateria = bateria,
                    modoTransporte = binding.cbModoTransporte.isChecked,
                    requiereRecarga = binding.cbRequiereRecarga.isChecked,
                    idUsuarioNubeAlta = idUsuarioNubeAlta
                )

                if (idPaso1LogVehiculo > 0) {
                    Log.d("Paso1SOCPrueba", "✅ Datos SOC guardados con ID: $idPaso1LogVehiculo")

                    // ✅ PASO 2: SUBIR FOTOS A LA API (AQUÍ ESTÁ LA DIFERENCIA PRINCIPAL)
                    var exitoFotos = true
                    val vin = vehiculo.VIN
                    val paso = 1

                    // Subir foto 1 si fue capturada
                    if (evidencia1Capturada && evidencia1File != null) {
                        Log.d("Paso1SOCPrueba", "📤 Subiendo foto 1 a la API...")

                        val resultadoSubida = ApiUploadUtil.subirFoto(
                            file = evidencia1File!!,
                            vin = vin,
                            paso = paso,
                            numeroFoto = 1
                        )

                        if (resultadoSubida.first) {
                            Log.d("Paso1SOCPrueba", "✅ Foto 1 subida exitosamente a la API")

                            // Guardar referencia en BD (sin Base64)
                            dalVehiculo.insertarPaso1LogVehiculoFotos(
                                idPaso1LogVehiculo = idPaso1LogVehiculo,
                                idEntidadArchivoFoto = null,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                consecutivo = 1,
                                posicion = 1,
                                fotoBase64 = null // NO guardamos Base64
                            )
                        } else {
                            exitoFotos = false
                            Log.e("Paso1SOCPrueba", "❌ Error subiendo foto 1: ${resultadoSubida.second}")
                        }
                    }

                    // Subir foto 2 si fue capturada
                    if (evidencia2Capturada && evidencia2File != null) {
                        Log.d("Paso1SOCPrueba", "📤 Subiendo foto 2 a la API...")

                        val resultadoSubida = ApiUploadUtil.subirFoto(
                            file = evidencia2File!!,
                            vin = vin,
                            paso = paso,
                            numeroFoto = 2
                        )

                        if (resultadoSubida.first) {
                            Log.d("Paso1SOCPrueba", "✅ Foto 2 subida exitosamente a la API")

                            dalVehiculo.insertarPaso1LogVehiculoFotos(
                                idPaso1LogVehiculo = idPaso1LogVehiculo,
                                idEntidadArchivoFoto = null,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                consecutivo = 2,
                                posicion = 2,
                                fotoBase64 = null // NO guardamos Base64
                            )
                        } else {
                            exitoFotos = false
                            Log.e("Paso1SOCPrueba", "❌ Error subiendo foto 2: ${resultadoSubida.second}")
                        }
                    }

                    // Ocultar indicador de carga
                    ocultarCarga()

                    // Mostrar mensaje de resultado
                    if (exitoFotos) {
                        Toast.makeText(
                            this@Paso1SOCPrueba_Activity,
                            "✅ SOC y fotos guardados exitosamente en la API",
                            Toast.LENGTH_LONG
                        ).show()
                        limpiarFormulario()
                    } else {
                        Toast.makeText(
                            this@Paso1SOCPrueba_Activity,
                            "⚠️ SOC guardado, pero hubo errores subiendo algunas fotos a la API",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    ocultarCarga()
                    Toast.makeText(this@Paso1SOCPrueba_Activity, "❌ Error guardando SOC en la base de datos", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("Paso1SOCPrueba", "💥 Error en el proceso: ${e.message}")
                Toast.makeText(this@Paso1SOCPrueba_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Limpia el formulario después de guardar exitosamente
     */
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

        ocultarSeccionesSOC()

        // Poner foco nuevamente en el campo VIN
        binding.etVIN.requestFocus()
    }

    /**
     * Muestra el indicador de carga con mensajes dinámicos
     */
    private fun mostrarCargaConMensajes() {
        loadingContainer.visibility = View.VISIBLE
        binding.btnGuardarSOC.isEnabled = false
        binding.btnGuardarSOC.alpha = 0.5f

        val mensajes = arrayOf(
            "Preparando datos..." to "Organizando información",
            "Subiendo fotos a API..." to "Transfiriendo a servidor",
            "Procesando..." to "Guardando en base de datos",
            "Finalizando..." to "Últimos detalles"
        )

        var mensajeIndex = 0
        loadingHandler = Handler(Looper.getMainLooper())

        loadingRunnable = object : Runnable {
            override fun run() {
                if (mensajeIndex < mensajes.size) {
                    tvLoadingText.text = mensajes[mensajeIndex].first
                    tvLoadingSubtext.text = mensajes[mensajeIndex].second
                    mensajeIndex++
                    loadingHandler?.postDelayed(this, 3000)
                }
            }
        }
        loadingRunnable?.let { loadingHandler?.post(it) }
    }

    /**
     * Oculta el indicador de carga
     */
    private fun ocultarCarga() {
        loadingContainer.visibility = View.GONE
        binding.btnGuardarSOC.isEnabled = true
        binding.btnGuardarSOC.alpha = 1.0f
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }

    /**
     * Muestra indicador de carga durante la consulta
     */
    private fun mostrarCargaConsulta() {
        loadingContainer.visibility = View.VISIBLE
        binding.btnConsultarVehiculo.isEnabled = false
        binding.btnConsultarVehiculo.alpha = 0.5f
    }

    /**
     * Oculta indicador de carga después de la consulta
     */
    private fun ocultarCargaConsulta() {
        loadingContainer.visibility = View.GONE
        binding.btnConsultarVehiculo.isEnabled = true
        binding.btnConsultarVehiculo.alpha = 1.0f
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}