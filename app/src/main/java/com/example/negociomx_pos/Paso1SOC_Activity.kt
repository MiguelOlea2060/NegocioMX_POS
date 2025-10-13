package com.example.negociomx_pos

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.databinding.ActivityPaso1SocBinding
import com.journeyapps.barcodescanner.ScanContract
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import androidx.core.view.isVisible
import com.example.negociomx_pos.BE.VehiculoPaso1
import com.example.negociomx_pos.BLL.BLLVehiculo
import com.example.negociomx_pos.Utils.ApiUploadUtil
import com.example.negociomx_pos.Utils.BLLUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext



class Paso1SOC_Activity : AppCompatActivity() {
    private lateinit var binding: ActivityPaso1SocBinding
    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: Vehiculo? = null
    private var vehiculoPaso1: VehiculoPaso1? = null
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null
    private var evidencia1File: File? = null
    private var evidencia2File: File? = null
    private var evidencia1Capturada: Boolean = false
    private var evidencia2Capturada: Boolean = false
    private var currentPhotoType: Int = 0
    private var fotoUri: Uri? = null
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.IdUsuario!!
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                fotoUri?.let { uri ->
                    procesarFoto(uri)
                }
            } else {
                Toast.makeText(this, "Error capturando foto", Toast.LENGTH_SHORT).show()
            }
        }
    private val permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "Permiso de cámara concedido", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    private var fotosExistentes: Int = 0
    private var vezActual: Short = 0
    private var esPrimeraVez: Boolean = true
    private var puedeCapturarFotos: Boolean = true
    private var idPasoNumLogVehiculoNotificacion: Int = 0
    private var idPasoNumLogVehiculoExistente: Int = 0
    //private var tieneRegistroSOC: Boolean = false
    //private var evidencia3File: File? = null
    //private var evidencia4File: File? = null
    //private var evidencia3Capturada: Boolean = false
    //private var evidencia4Capturada: Boolean = false
    //private var esSegundaEntrada: Boolean = false
   // private var esTerceraEntradaOMas: Boolean = false
   // private var puedeCapturarFotos12: Boolean = true
   // private var puedeCapturarFotos34: Boolean = false
    //private var idPaso1LogVehiculoExistente: Int = -1
    var bllUtil: BLLUtils?=null

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show()
        } else {
            binding.etVIN.setText(result.contents)
            Toast.makeText(this, "VIN escaneado: ${result.contents}", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var btnGuardar: Button
    private var vehiculo: Vehiculo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaso1SocBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bllUtil= BLLUtils()
        configurarEventos()
        verificarPermisos()
    }

    private fun configurarEventos() {
        binding.etVIN.requestFocus()
        binding.etVIN.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                verificaVINSuministrado()
                return@setOnKeyListener true
            }
            false
        }
        binding.etOdometro.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                binding.etBateria.selectAll()
                binding.etBateria.requestFocus()
                return@setOnKeyListener true
            }
            false
        }
        binding.btnConsultarVehiculo.setOnClickListener {
            verificaVINSuministrado()
        }
// ✅ SOLO CAPTURAR FOTOS - NO VER FOTOS EXISTENTES
        binding.btnEvidencia1.setOnClickListener {
            capturarEvidencia(1)
        }

        binding.btnEvidencia2.setOnClickListener {
            capturarEvidencia(2)
        }
        binding.btnGuardarSOC.setOnClickListener {
            guardarSOC()
        }
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)


    }
    private fun verificaVINSuministrado() {
        val vin = binding.etVIN.text.toString().trim()
        if (vin.isNotEmpty() && vin.length > 16) {
            consultarVehiculo(vin)
        } else {
            Toast.makeText(this, "Ingrese un VIN válido", Toast.LENGTH_SHORT).show()
        }
    }
    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            val bll = BLLVehiculo()
            try {
                Log.d("Paso1SOC", "🔍 Consultando vehículo con VIN: $vin")
                mostrarCargaConsulta()
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Consultando vehículo...",
                    Toast.LENGTH_SHORT
                ).show()

                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                vehiculoPaso1 = dalVehiculo.consultarVehiculoPorVINParaPaso1(vin, fecha)

                if (vehiculoPaso1 != null) {
                    vehiculo = bll.convertToVehiculo(vehiculoPaso1!!)
                }

                vehiculo?.IdPaso1LogVehiculo?.let {
                    if (it >= 0 ) {
                        vehiculoActual = vehiculo

                        // ✅ OBTENER EL VALOR DE VEZ DEL VEHÍCULO
                        vezActual = vehiculo?.Vez ?: 0
                        esPrimeraVez = (vezActual == 0.toShort())

                        // ✅ OBTENER IDs DE NOTIFICACIÓN
                        idPasoNumLogVehiculoNotificacion = vehiculo?.IdPasoNumLogVehiculoNotificacion ?: 0
                        idPasoNumLogVehiculoExistente = vehiculo?.IdPasoNumLogVehiculo ?: 0

                        Log.d("Paso1SOC", "📊 Vez actual: $vezActual, Es primera vez: $esPrimeraVez")

                        // ✅ MOSTRAR INFORMACIÓN DEL VEHÍCULO
                        mostrarInformacionVehiculo(vehiculo!!)
                        mostrarSeccionesSOC()

                        // ✅ CONFIGURAR CAMPOS Y BOTONES SEGÚN VEZ
                        configurarCamposSegunVez()
                        configurarBotonesSegunFotos()

                        // ✅ CONTAR FOTOS EXISTENTES (SOLO POSICIONES 1 Y 2)
                        fotosExistentes = vehiculoPaso1?.FotosPosicion1!! + vehiculoPaso1?.FotosPosicion2!!

                        // ✅ MENSAJE AL USUARIO SEGÚN EL MODO
                        val tieneNotificacionActiva = (vezActual > 0 && idPasoNumLogVehiculoNotificacion > 0)

                        val mensaje = if (!tieneNotificacionActiva) {
                            "ℹ️ Vehículo encontrado - MODO CONSULTA (Sin notificación activa)"
                        } else if (esPrimeraVez) {
                            "✅ Vehículo encontrado. Primera entrada - Capture al menos 1 foto"
                        } else {
                            "✅ Vehículo encontrado. Entrada #${vezActual + 1} - Actualice batería y capture al menos 1 foto"
                        }
                        Toast.makeText(this@Paso1SOC_Activity, mensaje, Toast.LENGTH_LONG).show()
                        Toast.makeText(this@Paso1SOC_Activity, mensaje, Toast.LENGTH_LONG).show()

                        ocultarCargaConsulta()

                    } else {
                        ocultarSeccionesSOC()
                        Toast.makeText(
                            this@Paso1SOC_Activity,
                            "❌ Vehículo no encontrado",
                            Toast.LENGTH_LONG
                        ).show()
                        ocultarCargaConsulta()
                        binding.etVIN.selectAll()
                    }
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
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

            // Mostrar datos SOC existentes
            etOdometro.setText(vehiculo.Odometro.toString())
            etBateria.setText(vehiculo.Bateria.toString())
            cbModoTransporte.isChecked = vehiculo.ModoTransporte
            cbRequiereRecarga.isChecked = vehiculo.RequiereRecarga

            layoutInfoVehiculo.visibility = View.VISIBLE
        }
    }

    private fun mostrarDatosSOCExistentes(datosSOC: Vehiculo) {
        binding.apply {
            // Mostrar datos existentes
            etOdometro.setText(datosSOC.Odometro.toString())
            etBateria.setText(datosSOC.Bateria.toString())
            cbModoTransporte.isChecked = datosSOC.ModoTransporte
            cbRequiereRecarga.isChecked = datosSOC.RequiereRecarga

            // Hacer campos de solo lectura
            etOdometro.isEnabled = false
            etBateria.isEnabled = false
            cbModoTransporte.isEnabled = false
            cbRequiereRecarga.isEnabled = false

            // Cambiar color para indicar que son de solo lectura
            etOdometro.alpha = 0.7f
            etBateria.alpha = 0.7f
            cbModoTransporte.alpha = 0.7f
            cbRequiereRecarga.alpha = 0.7f
        }

        Toast.makeText(this, "ℹ️ Este vehículo ya tiene datos SOC registrados", Toast.LENGTH_LONG)
            .show()
    }

    private fun verFotoExistente(posicion: Int, nombreArchivo:String) {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Cargando foto...", Toast.LENGTH_SHORT)
                    .show()

                var fotoBase64:String? =null
                if(ParametrosSistema.cfgApp!=null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD==true)
                    fotoBase64= dalVehiculo.obtenerFotoBase64Paso1(vehiculo.Id.toInt(), posicion)

                if (ParametrosSistema.cfgApp!=null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD==true &&
                    fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    mostrarDialogoFoto(fotoBase64, posicion)
                }
                else if (ParametrosSistema.cfgApp!=null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD==false) {
                    val urlCompletoFoto=ParametrosSistema.cfgApp?.UrlGuardadoArchivos+'/'+
                            ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube?.replace("~/","")+
                            '/'+nombreArchivo
                    mostrarDialogoFotoFromUrl(urlCompletoFoto, posicion)
                }
                else {
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "No se pudo cargar la foto",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "Error cargando foto: ${e.message}")
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Error cargando foto: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarDialogoFoto(fotoBase64: String, posicion: Int) {
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
                dialog.setTitle("Evidencia $posicion")
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
            Log.e("Paso1SOC", "Error mostrando foto: ${e.message}")
            Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoFotoFromUrl(url: String, posicion: Int) {
        lifecycleScope.launch {

            try {
                // Convertir Base64 a Bitmap
                val bitmap = withContext(Dispatchers.IO) {
                    bllUtil?.mLoad(url)
                }

                if (bitmap != null) {
                    // Crear diálogo personalizado
                    val dialog = android.app.AlertDialog.Builder(this@Paso1SOC_Activity)
                    val imageView = android.widget.ImageView(this@Paso1SOC_Activity)

                    // Configurar ImageView
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    imageView.adjustViewBounds = true

                    // Configurar diálogo
                    dialog.setTitle("Evidencia $posicion")
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
                    Toast.makeText(this@Paso1SOC_Activity, "Error decodificando la imagen", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "Error mostrando foto: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error mostrando foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarSeccionesSOC() {
        binding.apply {
            layoutSOC.visibility = View.VISIBLE
            layoutEvidencias.visibility = View.VISIBLE
            btnGuardarSOC.visibility = View.VISIBLE
        }
    }

  /*  private fun configurarCamposSegunVez() {
        binding.apply {
            if (esPrimeraVez && vehiculoActual?.IdPaso1LogVehiculo == 0) {
                // ✅ PRIMERA VEZ (Vez = 0): Todos los campos habilitados
                etOdometro.isEnabled = true
                etBateria.isEnabled = true
                cbModoTransporte.isEnabled = true
                cbRequiereRecarga.isEnabled = true

                etOdometro.alpha = 1.0f
                etBateria.alpha = 1.0f
                cbModoTransporte.alpha = 1.0f
                cbRequiereRecarga.alpha = 1.0f

                puedeCapturarFotos = true

                // Limpiar campos para nueva entrada
                etOdometro.setText("")
                etBateria.setText("")
                cbModoTransporte.isChecked = false
                cbRequiereRecarga.isChecked = false

                etOdometro.requestFocus()

                Log.d("Paso1SOC", "✅ Modo Primera Vez: Todos los campos habilitados")

            } else {
                // ✅ VEZ >= 1: Solo Batería y Requiere Recarga habilitados
                etOdometro.isVisible = false
                txtOdometro.isVisible = false

                if(vehiculoActual?.IdPasoNumLogVehiculoNotificacion!!>0) {
                    etBateria.isEnabled = true
                    cbModoTransporte.isVisible = false
                    cbRequiereRecarga.isEnabled = true

                    // etOdometro.alpha = 0.5f
                    etBateria.alpha = 1.0f
                    //  cbModoTransporte.alpha = 0.5f
                    cbRequiereRecarga.alpha = 1.0f

                    puedeCapturarFotos = true

                    // Mantener valores existentes en campos bloqueados
                    if (vehiculoActual?.Odometro != null && vehiculoActual?.Odometro!! > 0) {
                        etOdometro.setText(vehiculoActual?.Odometro.toString())
                    }
                    if (vehiculoActual?.ModoTransporte != null) {
                        cbModoTransporte.isChecked = vehiculoActual?.ModoTransporte!!
                    }

                    // Limpiar solo los campos editables
                    etBateria.setText("")
                    cbRequiereRecarga.isChecked = false

                    // Enfocar en Batería para entrada rápida
                    etBateria.requestFocus()
                    etBateria.selectAll()
                }
                Log.d("Paso1SOC", "✅ Modo Subsecuente (Vez $vezActual): Solo Batería y Requiere Recarga habilitados")
            }
        }
    }
*/
    private fun ocultarSeccionesSOC() {
        binding.apply {
            layoutInfoVehiculo.visibility = View.GONE
            layoutSOC.visibility = View.GONE
            layoutEvidencias.visibility = View.GONE
            btnGuardarSOC.visibility = View.GONE
        }
    }

    private fun capturarEvidencia(numeroEvidencia: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // ✅ VALIDAR QUE SOLO SEA FOTO 1 O 2
        if (numeroEvidencia !in 1..2) {
            Toast.makeText(this, "Solo se permiten fotos 1 y 2", Toast.LENGTH_SHORT).show()
            return
        }

// ✅ VALIDAR SI PUEDE CAPTURAR FOTOS
        if (!puedeCapturarFotos) {
            Toast.makeText(this, "No puede capturar más fotos en este momento", Toast.LENGTH_LONG).show()
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
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
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
            val maxSize = 3072
//            val maxSize = 2048
//            val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            var ratio: Float = 1.0F
            if (bitmap.width > bitmap.height)
                ratio = maxSize.toFloat() / bitmap.width
            else
                ratio = maxSize.toFloat() / bitmap.height

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
        try {
            Log.d("Paso1SOC", "📸 Procesando foto para evidencia $currentPhotoType")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Error: No hay vehículo seleccionado",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Error: Archivo de foto no encontrado",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            var tamanoFisicoArchivo: Long = archivoLocal.length()
            var tamanoFisicoMaxArchivo: Long = (2.2 * 1024 * 1024).toLong()
            val archivoFinal = if (tamanoFisicoArchivo > tamanoFisicoMaxArchivo) {
                Log.d("Paso1SOC", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal)
            } else {
                archivoLocal
            }

            // ✅ GUARDAR REFERENCIA DEL ARCHIVO SEGÚN LA EVIDENCIA (SOLO 1 Y 2)
            when (currentPhotoType) {
                1 -> {
                    evidencia1File = archivoFinal
                    evidencia1Capturada = true
                    binding.tvEstadoEvidencia1.text = "📷"
                    Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 1 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    evidencia2File = archivoFinal
                    evidencia2Capturada = true
                    binding.tvEstadoEvidencia2.text = "📷"
                    Toast.makeText(this@Paso1SOC_Activity, "✅ Evidencia 2 capturada (sin guardar)", Toast.LENGTH_SHORT).show()
                }
            }
            Log.d("Paso1SOC", "✅ Evidencia $currentPhotoType lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso1SOC", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(
                this@Paso1SOC_Activity,
                "Error procesando foto: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun mostrarCargaConMensajes() {
        // Mostrar loading
        loadingContainer.visibility = View.VISIBLE
        binding.btnGuardarSOC.isEnabled = false
        binding.btnGuardarSOC.alpha = 0.5f

        // Mensajes dinámicos para mantener al usuario informado
        val mensajes = arrayOf(
            "Preparando datos..." to "Organizando información",
            "Comprimiendo fotos..." to "Optimizando imágenes",
            "Enviando a servidor..." to "Transfiriendo información",
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
                    loadingHandler?.postDelayed(this, 3000) // Cambiar cada 3 segundos
                }
            }
        }
        loadingRunnable?.let { loadingHandler?.post(it) }
    }

    private fun ocultarCarga() {
        loadingContainer.visibility = View.GONE
        binding.btnGuardarSOC.isEnabled = true
        binding.btnGuardarSOC.alpha = 1.0f

        // Limpiar handlers
        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun mostrarCargaConsulta() {
        // Mostrar loading
        loadingContainer.visibility = View.VISIBLE
        binding.btnConsultarVehiculo.isEnabled = false
        binding.btnConsultarVehiculo.alpha = 0.5f
    }

    private fun ocultarCargaConsulta() {
        loadingContainer.visibility = View.GONE
        binding.btnConsultarVehiculo.isEnabled = true
        binding.btnConsultarVehiculo.alpha = 1.0f

        // Limpiar handlers
        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun guardarSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero escanea el codigo QR", Toast.LENGTH_SHORT).show()
            return
        }

        val bateriaText = binding.etBateria.text.toString().trim()

        // ✅ VALIDAR CAMPOS SEGÚN VEZ
        if (esPrimeraVez) {
            // Primera vez: validar todos los campos
            val odometroText = binding.etOdometro.text.toString().trim()

            if (odometroText.isEmpty() || bateriaText.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                binding.etOdometro.selectAll()
                binding.etOdometro.requestFocus()
                return
            }
        } else {
            // Vez >= 1: solo validar batería
            if (bateriaText.isEmpty()) {
                Toast.makeText(this, "Ingrese el nivel de batería", Toast.LENGTH_SHORT).show()
                binding.etBateria.selectAll()
                binding.etBateria.requestFocus()
                return
            }
        }

        // ✅ SOLO GUARDAR VALORES QUE EL USUARIO INGRESÓ
// Si el campo está deshabilitado, guardar 0 o false
        val odometro = if (binding.etOdometro.isEnabled) {
            binding.etOdometro.text.toString().trim().toIntOrNull() ?: 0
        } else {
            0  // Campo deshabilitado = guardar 0
        }

        val bateria = if (binding.etBateria.isEnabled) {
            bateriaText.toIntOrNull() ?: 0
        } else {
            0  // Campo deshabilitado = guardar 0
        }

        val modoTransporte = if (binding.cbModoTransporte.isEnabled) {
            binding.cbModoTransporte.isChecked
        } else {
            false  // Campo deshabilitado = guardar false
        }

        val requiereRecarga = if (binding.cbRequiereRecarga.isEnabled) {
            binding.cbRequiereRecarga.isChecked
        } else {
            false  // Campo deshabilitado = guardar false
        }

        if (bateria < 0 || bateria > 100) {
            Toast.makeText(this, "El nivel de batería debe estar entre 0 y 100", Toast.LENGTH_SHORT)
                .show()
            binding.etBateria.selectAll()
            binding.etBateria.requestFocus()
            return
        }
// Justo antes de lifecycleScope.launch {
        mostrarCargaConMensajes()
        lifecycleScope.launch {
            try {


// ✅ VALIDAR FOTOS MÍNIMAS SEGÚN ENTRADA
                val validacionFotos = validarFotosMinimas()
                if (!validacionFotos.first) {
                    ocultarCarga()
                    Toast.makeText(this@Paso1SOC_Activity, validacionFotos.second, Toast.LENGTH_LONG).show()
                    return@launch
                }

                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Guardando SOC y fotos...",
                    Toast.LENGTH_SHORT
                ).show()

                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                // ✅ CONFIGURAR URL PARA API DE SUBIDA DE ARCHIVOS
                var urlBase = ParametrosSistema.cfgApp!!.UrlGuardadoArchivos + '/' +
                        ParametrosSistema.cfgApp!!.UrlAPIControllerGuardadoArchivos

                var nombreArchivo = ""

                // ✅ SIEMPRE INSERTAR NUEVO REGISTRO CON VEZ
                val idPaso1LogVehiculo = dalVehiculo.insertarPaso1LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    odometro = odometro,  // Ya calculado arriba según si está habilitado
                    bateria = bateria,  // Ya calculado arriba según si está habilitado
                    modoTransporte = modoTransporte,  // Ya calculado arriba según si está habilitado
                    requiereRecarga = requiereRecarga,  // Ya calculado arriba según si está habilitado
                    idUsuarioNubeAlta = idUsuarioNubeAlta,
                    vez = vezActual,
                    fechaMovimiento = fechaActual,
                    idPasoNumLogVehiculoNotificacion = vehiculo.IdPasoNumLogVehiculoNotificacion
                )

                if (idPaso1LogVehiculo > 0) {
                    Log.d("Paso1SOC", "✅ Datos SOC guardados con ID: $idPaso1LogVehiculo")

                    // ✅ 2. GUARDAR SOLO FOTOS 1 Y 2
                    var exitoFotos = true
                    var consecutivo: Short = 1

                    if (evidencia1Capturada && evidencia1File != null) {
                        var fotoBase64 = bllUtil?.convertirImagenABase64(evidencia1File!!)
                        val nombreArchivo = "${vehiculoActual?.VIN}_Paso_1_Foto_1_Vez_${vezActual}.jpg"

                        if (ParametrosSistema.cfgApp != null &&
                            ParametrosSistema.cfgApp!!.ManejaGuardadoArchivosEnBD == false) {
                            val resultadoSubida = ApiUploadUtil.subirFoto(
                                urlBase = urlBase,
                                nombreArchivo = nombreArchivo,
                                file = evidencia1File!!,
                                vin = vehiculoActual!!.VIN,
                                paso = 1,
                                numeroFoto = 1
                            )
                            if (resultadoSubida.first) {
                                fotoBase64 = null
                            }
                        }

                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 1,
                            fotoBase64 = fotoBase64,
                            fechaMovimiento = fechaActual,
                            nombreArchivo = nombreArchivo
                        )
                        consecutivo++
                    }

                    if (evidencia2Capturada && evidencia2File != null) {
                        var fotoBase64 = bllUtil?.convertirImagenABase64(evidencia2File!!)
                        val nombreArchivo = "${vehiculoActual?.VIN}_Paso_1_Foto_2_Vez_${vezActual}.jpg"

                        if (ParametrosSistema.cfgApp != null &&
                            ParametrosSistema.cfgApp!!.ManejaGuardadoArchivosEnBD == false) {
                            val resultadoSubida = ApiUploadUtil.subirFoto(
                                urlBase = urlBase,
                                nombreArchivo = nombreArchivo,
                                file = evidencia2File!!,
                                vin = vehiculoActual!!.VIN,
                                paso = 1,
                                numeroFoto = 2
                            )
                            if (resultadoSubida.first) {
                                fotoBase64 = null
                            }
                        }

                        exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                            idPaso1LogVehiculo = idPaso1LogVehiculo,
                            idEntidadArchivoFoto = null,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            consecutivo = consecutivo,
                            posicion = 2,
                            fotoBase64 = fotoBase64,
                            fechaMovimiento = fechaActual,
                            nombreArchivo = nombreArchivo
                        )
                    }


                    if (exitoFotos) {
                        ocultarCarga()
                        Toast.makeText(
                            this@Paso1SOC_Activity,
                            "✅ SOC y fotos guardados exitosamente en la base de datos",
                            Toast.LENGTH_LONG
                        ).show()
                        limpiarFormulario()
                    } else {
                        ocultarCarga()
                        Toast.makeText(
                            this@Paso1SOC_Activity,
                            "⚠️ SOC guardado, pero hubo errores guardando las fotos",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    ocultarCarga()
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "❌ Error guardando SOC",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("Paso1SOC", "💥 Error guardando SOC: ${e.message}")
                Toast.makeText(this@Paso1SOC_Activity, "Error: ${e.message}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun validarFotosMinimas(): Pair<Boolean, String> {
        // ✅ SIEMPRE REQUERIR MÍNIMO 1 FOTO (FOTO 1 O FOTO 2)
        val fotosCapturadas = (if (evidencia1Capturada) 1 else 0) + (if (evidencia2Capturada) 1 else 0)

        return if (fotosCapturadas < 1) {
            Pair(false, "Debe capturar al menos 1 foto (Foto 1 o Foto 2)")
        } else {
            Pair(true, "")
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
            tvEstadoEvidencia3.text = "❌"
            tvEstadoEvidencia4.text = "❌"

            // Restaurar campos editables
            etOdometro.isEnabled = true
            etBateria.isEnabled = true
            cbModoTransporte.isEnabled = true
            cbRequiereRecarga.isEnabled = true
            etOdometro.alpha = 1.0f
            etBateria.alpha = 1.0f
            cbModoTransporte.alpha = 1.0f
            cbRequiereRecarga.alpha = 1.0f

            // Ocultar evidencias adicionales
            layoutEvidencia3.visibility = View.GONE
            layoutEvidencia4.visibility = View.GONE
        }

        vehiculoActual = null
        evidencia1File = null
        evidencia2File = null
        evidencia1Capturada = false
        evidencia2Capturada = false
        fotosExistentes = 0
        vehiculoPaso1 = null
        // ✅ RESETEAR VARIABLES DE CONTROL NUEVAS
        vezActual = 0
        esPrimeraVez = true
        puedeCapturarFotos = true
        idPasoNumLogVehiculoNotificacion = 0
        idPasoNumLogVehiculoExistente = 0
        ocultarSeccionesSOC()
    }

  /*private fun configurarBotonesSegunFotos() {
      if (vehiculoActual == null) return

      binding.apply {
          // ✅ SOLO BOTONES PARA CAPTURAR - SIN LÓGICA DE "VER"

          // Botón evidencia 1 - siempre disponible
          btnEvidencia1.text = "📷 Foto 1"
          btnEvidencia1.isEnabled = puedeCapturarFotos
          tvEstadoEvidencia1.text = if (evidencia1Capturada) "📷" else "❌"

          // Botón evidencia 2 - siempre disponible
          btnEvidencia2.text = "📷 Foto 2"
          btnEvidencia2.isEnabled = puedeCapturarFotos
          tvEstadoEvidencia2.text = if (evidencia2Capturada) "📷" else "❌"

          // ✅ OCULTAR BOTONES 3 Y 4 PERMANENTEMENTE
          layoutEvidencia3.visibility = View.GONE
          layoutEvidencia4.visibility = View.GONE

          Log.d("Paso1SOC", "✅ Botones configurados para solo captura")
      }
  }*/

   /* private fun configurarBotonesSegunFotos() {
        if (vehiculoActual == null) return

        binding.apply {
            // ✅ SI NO PUEDE CAPTURAR FOTOS (MODO LECTURA), DESHABILITAR BOTONES
            if (!puedeCapturarFotos) {
                btnEvidencia1.text = "🚫 Foto 1"
                btnEvidencia1.isEnabled = false
                btnEvidencia1.alpha = 0.5f
                tvEstadoEvidencia1.text = "❌"

                btnEvidencia2.text = "🚫 Foto 2"
                btnEvidencia2.isEnabled = false
                btnEvidencia2.alpha = 0.5f
                tvEstadoEvidencia2.text = "❌"

                layoutEvidencia3.visibility = View.GONE
                layoutEvidencia4.visibility = View.GONE

                Log.d("Paso1SOC", "🚫 Botones de fotos deshabilitados (modo lectura)")
                return@apply
            }

            // ✅ RESTO DEL CÓDIGO EXISTENTE (botones habilitados normalmente)
            btnEvidencia1.text = "📷 Foto 1"
            btnEvidencia1.isEnabled = true
            btnEvidencia1.alpha = 1.0f
            tvEstadoEvidencia1.text = if (evidencia1Capturada) "📷" else "❌"

            btnEvidencia2.text = "📷 Foto 2"
            btnEvidencia2.isEnabled = true
            btnEvidencia2.alpha = 1.0f
            tvEstadoEvidencia2.text = if (evidencia2Capturada) "📷" else "❌"

            layoutEvidencia3.visibility = View.GONE
            layoutEvidencia4.visibility = View.GONE

            Log.d("Paso1SOC", "✅ Botones configurados para captura")
        }
    }*/
   private fun configurarBotonesSegunFotos() {
       if (vehiculoActual == null) return

       binding.apply {
           // ✅ SI NO PUEDE CAPTURAR FOTOS (MODO LECTURA), DESHABILITAR BOTONES
           if (!puedeCapturarFotos) {
               btnEvidencia1.text = "🚫 Foto 1"
               btnEvidencia1.isEnabled = false
               btnEvidencia1.alpha = 0.5f
               tvEstadoEvidencia1.text = "❌"

               btnEvidencia2.text = "🚫 Foto 2"
               btnEvidencia2.isEnabled = false
               btnEvidencia2.alpha = 0.5f
               tvEstadoEvidencia2.text = "❌"

               layoutEvidencia3.visibility = View.GONE
               layoutEvidencia4.visibility = View.GONE

               Log.d("Paso1SOC", "🚫 Botones de fotos deshabilitados (modo lectura)")
               return@apply
           }

           // ✅ RESTO DEL CÓDIGO EXISTENTE (botones habilitados normalmente)
           btnEvidencia1.text = "📷 Foto 1"
           btnEvidencia1.isEnabled = true
           btnEvidencia1.alpha = 1.0f
           tvEstadoEvidencia1.text = if (evidencia1Capturada) "📷" else "❌"

           btnEvidencia2.text = "📷 Foto 2"
           btnEvidencia2.isEnabled = true
           btnEvidencia2.alpha = 1.0f
           tvEstadoEvidencia2.text = if (evidencia2Capturada) "📷" else "❌"

           layoutEvidencia3.visibility = View.GONE
           layoutEvidencia4.visibility = View.GONE

           Log.d("Paso1SOC", "✅ Botones configurados para captura")
       }
   }
  private fun configurarCamposSegunVez() {
      binding.apply {
          // ✅ VERIFICAR SI HAY NOTIFICACIÓN ACTIVA
          val tieneNotificacionActiva = (vezActual > 0 && idPasoNumLogVehiculoNotificacion > 0)

          if (!tieneNotificacionActiva) {
              // ✅ MODO SOLO LECTURA - NO HAY NOTIFICACIÓN ACTIVA
              Log.d("Paso1SOC", "⚠️ MODO SOLO LECTURA: Sin notificación activa")

              // Deshabilitar TODOS los campos
              etOdometro.isEnabled = false
              etBateria.isEnabled = false
              cbModoTransporte.isEnabled = false
              cbRequiereRecarga.isEnabled = false

              // Cambiar aspecto visual para indicar que son de solo lectura
              etOdometro.alpha = 0.6f
              etBateria.alpha = 0.6f
              cbModoTransporte.alpha = 0.6f
              cbRequiereRecarga.alpha = 0.6f

              // Deshabilitar captura de fotos
              puedeCapturarFotos = false

              // Cargar y mostrar ÚLTIMOS datos registrados
              cargarUltimosDatosSOC()

              // Cambiar botón Guardar por botón Regresar
              btnGuardarSOC.text = "⬅️ REGRESAR"
              btnGuardarSOC.backgroundTintList = android.content.res.ColorStateList.valueOf(
                  android.graphics.Color.parseColor("#FF9800")  // Color naranja
              )
              btnGuardarSOC.visibility = View.VISIBLE

              // Cambiar la acción del botón
              btnGuardarSOC.setOnClickListener {
                  finish()  // Regresar a la pantalla anterior
              }

              Toast.makeText(
                  this@Paso1SOC_Activity,
                  "ℹ️ Modo consulta: No hay notificación activa para este vehículo",
                  Toast.LENGTH_LONG
              ).show()

              return@apply
          }

          // ✅ MODO EDICIÓN - HAY NOTIFICACIÓN ACTIVA
          if (esPrimeraVez && vehiculoActual?.IdPaso1LogVehiculo == 0) {
              // PRIMERA VEZ (Vez = 0): Todos los campos habilitados
              etOdometro.isEnabled = true
              etBateria.isEnabled = true
              cbModoTransporte.isEnabled = true
              cbRequiereRecarga.isEnabled = true

              etOdometro.alpha = 1.0f
              etBateria.alpha = 1.0f
              cbModoTransporte.alpha = 1.0f
              cbRequiereRecarga.alpha = 1.0f

              puedeCapturarFotos = true

              // Limpiar campos para nueva entrada
              etOdometro.setText("")
              etBateria.setText("")
              cbModoTransporte.isChecked = false
              cbRequiereRecarga.isChecked = false

              // Mostrar todos los campos
              etOdometro.isVisible = true
              txtOdometro.isVisible = true
              cbModoTransporte.isVisible = true

              etOdometro.requestFocus()

              // Configurar botón Guardar normal
              btnGuardarSOC.text = "💾 GUARDAR"
              btnGuardarSOC.backgroundTintList = android.content.res.ColorStateList.valueOf(
                  android.graphics.Color.parseColor("#4CAF50")  // Color verde
              )
              btnGuardarSOC.setOnClickListener {
                  guardarSOC()
              }

              Log.d("Paso1SOC", "✅ Modo Primera Vez: Todos los campos habilitados")

          }

          else {
              // VEZ >= 1: Solo Batería y Requiere Recarga habilitados
              etOdometro.isVisible = false
              txtOdometro.isVisible = false
              cbModoTransporte.isVisible = false

              etOdometro.isEnabled = false
              cbModoTransporte.isEnabled = false

              etBateria.isEnabled = true
              cbRequiereRecarga.isEnabled = true

              etBateria.alpha = 1.0f
              cbRequiereRecarga.alpha = 1.0f

              puedeCapturarFotos = true

              // Cargar valores existentes en campos deshabilitados
              if (vehiculoActual?.Odometro != null && vehiculoActual?.Odometro!! > 0) {
                  etOdometro.setText(vehiculoActual?.Odometro.toString())
              }
              if (vehiculoActual?.ModoTransporte != null) {
                  cbModoTransporte.isChecked = vehiculoActual?.ModoTransporte!!
              }

              // Limpiar solo los campos editables
              etBateria.setText("")
              cbRequiereRecarga.isChecked = false

              // Enfocar en Batería para entrada rápida
              etBateria.requestFocus()
              etBateria.selectAll()

              // Configurar botón Guardar normal
              btnGuardarSOC.text = "💾 GUARDAR"
              btnGuardarSOC.backgroundTintList = android.content.res.ColorStateList.valueOf(
                  android.graphics.Color.parseColor("#4CAF50")  // Color verde
              )
              btnGuardarSOC.setOnClickListener {
                  guardarSOC()
              }

              Log.d("Paso1SOC", "✅ Modo Subsecuente (Vez $vezActual): Solo Batería y Requiere Recarga habilitados")
          }
      }
  }


    private fun cargarUltimosDatosSOC() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) return

        lifecycleScope.launch {
            try {
                Log.d("Paso1SOC", "📊 Cargando últimos datos SOC para consulta")

                // Consultar últimos datos de la BD
      //          val ultimosDatos = dalVehiculo. consultarVehiculoPorVINParaPaso1(vehiculo.Id.toInt())

                val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val ultimosDatos = dalVehiculo.consultarVehiculoPorVINParaPaso1(vehiculoActual!!.VIN, fecha)

                if (ultimosDatos != null) {
                    binding.apply {
                        // Mostrar datos según si es primera o subsecuente entrada
                        if (vehiculoActual?.IdPaso1LogVehiculo == 0) {
                            // Primera entrada: Mostrar TODOS los datos
                            etOdometro.setText(ultimosDatos.Odometro.toString())
                            etBateria.setText(ultimosDatos.Bateria.toString())
                            cbModoTransporte.isChecked = ultimosDatos.ModoTransporte
                            cbRequiereRecarga.isChecked = ultimosDatos.RequiereRecarga

                            // Mostrar todos los campos
                            etOdometro.isVisible = true
                            txtOdometro.isVisible = true
                            cbModoTransporte.isVisible = true

                        } else {
                            // Subsecuente: Solo mostrar Batería y Requiere Recarga
                            etBateria.setText(ultimosDatos.Bateria.toString())
                            cbRequiereRecarga.isChecked = ultimosDatos.RequiereRecarga

                            // Ocultar campos no relevantes
                            etOdometro.isVisible = false
                            txtOdometro.isVisible = false
                            cbModoTransporte.isVisible = false
                        }
                    }

                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "✅ Últimos datos cargados (solo consulta)",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "⚠️ No hay datos previos para este vehículo",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Paso1SOC", "Error cargando últimos datos: ${e.message}")
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Error cargando datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}
