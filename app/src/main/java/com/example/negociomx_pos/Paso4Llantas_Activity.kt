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
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.ParametrosSistema
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Paso4Llantas_Activity : AppCompatActivity() {

    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: Vehiculo? = null

    // Variables de UI
    private lateinit var etVIN: EditText
    private lateinit var btnConsultarVehiculo: Button
    private lateinit var layoutInfoVehiculo: LinearLayout
    private lateinit var layoutLlantas: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private lateinit var btnGuardarPaso4: Button
    private lateinit var tvMensajeInfo: TextView

    // Variables de información del vehículo
    private lateinit var tvBlVehiculo: TextView
    private lateinit var tvMarcaModeloAnnio: TextView
    private lateinit var tvColorExterior: TextView
    private lateinit var tvColorInterior: TextView
    private lateinit var tvTipoCombustible: TextView
    private lateinit var tvTipoVehiculo: TextView

    // Variables de llantas
    private lateinit var ivLlanta1: ImageView
    private lateinit var ivLlanta2: ImageView
    private lateinit var ivLlanta3: ImageView
    private lateinit var ivLlanta4: ImageView
    private lateinit var cbLlanta1: CheckBox
    private lateinit var cbLlanta2: CheckBox
    private lateinit var cbLlanta3: CheckBox
    private lateinit var cbLlanta4: CheckBox

    // Variables para manejo de loading
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // Variables para control de datos
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.Id?.toInt()!!
    private var datosExistentes: Map<Byte, Pair<Boolean, Boolean>> = emptyMap() // Posicion -> (Verificada, TieneFoto)

    // Variables para captura de fotos (DESHABILITADAS INICIALMENTE)
    private var fotoHabilitada: Boolean = false
    private var posicionFotoActual: Byte = 0
    private var fotoUri: Uri? = null

    // ✅ LAUNCHER PARA CÁMARA (PREPARADO PERO DESHABILITADO)
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && fotoHabilitada) {
            fotoUri?.let { uri ->
                procesarFoto(uri, posicionFotoActual)
            }
        } else {
            Toast.makeText(this, "Captura de foto deshabilitada", Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_paso4_llantas)

        inicializarVistas()
        configurarEventos()
        verificarPermisos()
    }

    private fun inicializarVistas() {
        // Controles principales
        etVIN = findViewById(R.id.etVIN)
        btnConsultarVehiculo = findViewById(R.id.btnConsultarVehiculo)
        layoutInfoVehiculo = findViewById(R.id.layoutInfoVehiculo)
        layoutLlantas = findViewById(R.id.layoutLlantas)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)
        btnGuardarPaso4 = findViewById(R.id.btnGuardarPaso4)
        tvMensajeInfo = findViewById(R.id.tvMensajeInfo)

        // Información del vehículo
        tvBlVehiculo = findViewById(R.id.tvBlVehiculo)
        tvMarcaModeloAnnio = findViewById(R.id.tvMarcaModeloAnnio)
        tvColorExterior = findViewById(R.id.tvColorExterior)
        tvColorInterior = findViewById(R.id.tvColorInterior)
        tvTipoCombustible = findViewById(R.id.tvTipoCombustible)
        tvTipoVehiculo = findViewById(R.id.tvTipoVehiculo)

        // Llantas
        ivLlanta1 = findViewById(R.id.ivLlanta1)
        ivLlanta2 = findViewById(R.id.ivLlanta2)
        ivLlanta3 = findViewById(R.id.ivLlanta3)
        ivLlanta4 = findViewById(R.id.ivLlanta4)
        cbLlanta1 = findViewById(R.id.cbLlanta1)
        cbLlanta2 = findViewById(R.id.cbLlanta2)
        cbLlanta3 = findViewById(R.id.cbLlanta3)
        cbLlanta4 = findViewById(R.id.cbLlanta4)
    }

    private fun configurarEventos() {
        // ✅ CONFIGURAR FOCUS EN VIN
        etVIN.requestFocus()

        // ✅ CAPTURA DE ENTER EN EL VIN
        etVIN.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                verificaVINSuministrado()
                return@setOnKeyListener true
            }
            false
        }

        // ✅ BOTÓN CONSULTAR VEHÍCULO
        btnConsultarVehiculo.setOnClickListener {
            verificaVINSuministrado()
        }

        // ✅ EVENTOS DE IMÁGENES DE LLANTAS (DESHABILITADOS INICIALMENTE)
        ivLlanta1.setOnClickListener { manejarClicLlanta(1) }
        ivLlanta2.setOnClickListener { manejarClicLlanta(2) }
        ivLlanta3.setOnClickListener { manejarClicLlanta(3) }
        ivLlanta4.setOnClickListener { manejarClicLlanta(4) }

        // ✅ BOTÓN GUARDAR
        btnGuardarPaso4.setOnClickListener {
            guardarPaso4()
        }
    }

    private fun verificaVINSuministrado() {
        val vin = etVIN.text.toString().trim()
        if (vin.isNotEmpty() && vin.length > 16) {
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
                Log.d("Paso4LLANTAS", "🔍 Consultando vehículo con VIN: $vin")
                mostrarCargaConsulta()

                Toast.makeText(this@Paso4Llantas_Activity, "Consultando vehículo...", Toast.LENGTH_SHORT).show()

                val vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin)
                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    // ✅ CONSULTAR DATOS PASO4 EXISTENTES
                    datosExistentes = dalVehiculo.consultarPaso4Existente(vehiculo.Id.toInt())

                    mostrarInformacionVehiculo(vehiculo)
                    mostrarSeccionLlantas()
                    cargarDatosExistentes()

                    Toast.makeText(
                        this@Paso4Llantas_Activity,
                        "✅ Vehículo encontrado. ${datosExistentes.size} llantas registradas",
                        Toast.LENGTH_SHORT
                    ).show()

                    ocultarCargaConsulta()
                } else {
                    ocultarSecciones()
                    Toast.makeText(this@Paso4Llantas_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                    ocultarCargaConsulta()
                    etVIN.selectAll()
                }

            } catch (e: Exception) {
                Log.e("Paso4LLANTAS", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso4Llantas_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                ocultarCargaConsulta()
                etVIN.selectAll()
            }
        }
    }

    private fun mostrarInformacionVehiculo(vehiculo: Vehiculo) {
        tvBlVehiculo.text = "MBL: ${vehiculo.BL}"
        tvMarcaModeloAnnio.text = "${vehiculo.Marca} - ${vehiculo.Modelo}, ${vehiculo.Anio}"
        tvColorExterior.text = "Color Ext.: ${vehiculo.ColorExterior}"
        tvColorInterior.text = "Color Int.: ${vehiculo.ColorInterior}"
        tvTipoCombustible.text = "Combustible: ${vehiculo.TipoCombustible}"
        tvTipoVehiculo.text = "Tipo de Vehiculo: ${vehiculo.TipoVehiculo}"

        layoutInfoVehiculo.visibility = View.VISIBLE
    }

    private fun mostrarSeccionLlantas() {
        layoutLlantas.visibility = View.VISIBLE
        btnGuardarPaso4.visibility = View.VISIBLE
    }

    private fun ocultarSecciones() {
        layoutInfoVehiculo.visibility = View.GONE
        layoutLlantas.visibility = View.GONE
        btnGuardarPaso4.visibility = View.GONE
    }

    private fun cargarDatosExistentes() {
        // Cargar checkboxes según datos existentes
        cbLlanta1.isChecked = datosExistentes[1]?.first ?: false
        cbLlanta2.isChecked = datosExistentes[2]?.first ?: false
        cbLlanta3.isChecked = datosExistentes[3]?.first ?: false
        cbLlanta4.isChecked = datosExistentes[4]?.first ?: false

        // Actualizar mensaje informativo
        val totalLlantas = datosExistentes.size
        val totalFotos = datosExistentes.values.count { it.second }
        tvMensajeInfo.text = "ℹ️ $totalLlantas llantas registradas, $totalFotos con foto. Captura deshabilitada."
    }

    private fun manejarClicLlanta(posicion: Int) {
        if (!fotoHabilitada) {
            Toast.makeText(this, "Captura de fotos deshabilitada temporalmente", Toast.LENGTH_SHORT).show()
            return
        }

        val posicionByte = posicion.toByte()
        val tieneFoto = datosExistentes[posicionByte]?.second ?: false

        if (tieneFoto) {
            // Mostrar foto existente
            mostrarFotoExistente(posicionByte)
        } else {
            // Capturar nueva foto
            capturarFotoLlanta(posicionByte)
        }
    }

    private fun capturarFotoLlanta(posicion: Byte) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "Paso4_Llanta${posicion}_${timeStamp}.jpg"
            val storageDir = File(getExternalFilesDir(null), "Paso4_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            posicionFotoActual = posicion

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarFoto(uri: Uri, posicion: Byte) {
        // Implementar procesamiento de foto cuando se habilite
        Toast.makeText(this, "Foto capturada para llanta $posicion (función en desarrollo)", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarFotoExistente(posicion: Byte) {
        val vehiculo = vehiculoActual ?: return

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso4Llantas_Activity, "Cargando foto...", Toast.LENGTH_SHORT).show()

                val fotoBase64 = dalVehiculo.obtenerFotoBase64Paso4(vehiculo.Id.toInt(), posicion)

                if (fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    mostrarDialogoFoto(fotoBase64, posicion)
                } else {
                    Toast.makeText(this@Paso4Llantas_Activity, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("Paso4LLANTAS", "Error cargando foto: ${e.message}")
                Toast.makeText(this@Paso4Llantas_Activity, "Error cargando foto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoFoto(fotoBase64: String, posicion: Byte) {
        try {
            val decodedBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                val dialog = android.app.AlertDialog.Builder(this)
                val imageView = android.widget.ImageView(this)

                imageView.setImageBitmap(bitmap)
                imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true

                val nombrePosicion = when (posicion) {
                    1.toByte() -> "Delantera Izquierda"
                    2.toByte() -> "Delantera Derecha"
                    3.toByte() -> "Trasera Izquierda"
                    4.toByte() -> "Trasera Derecha"
                    else -> "Posición $posicion"
                }

                dialog.setTitle("Paso 4 - Llanta $nombrePosicion")
                dialog.setView(imageView)
                dialog.setPositiveButton("Cerrar") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }

                val alertDialog = dialog.create()
                alertDialog.show()

                val window = alertDialog.window
                window?.setLayout(
                    (resources.displayMetrics.widthPixels * 0.9).toInt(),
                    (resources.displayMetrics.heightPixels * 0.7).toInt()
                )

            } else {
                Toast.makeText(this, "Error decodificando la imagen", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error mostrando foto: ${e.message}")
            Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun guardarPaso4() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCargaConMensajes()

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso4Llantas_Activity, "Guardando verificación de llantas...", Toast.LENGTH_SHORT).show()

                // Insertar registro principal si no existe
                var idPaso4LogVehiculo = -1
                if (datosExistentes.isEmpty()) {
                    idPaso4LogVehiculo = dalVehiculo.insertarPaso4LogVehiculo(
                        idVehiculo = vehiculo.Id.toInt(),
                        idUsuarioNubeAlta = idUsuarioNubeAlta
                    )
                }

                var exitoso = true

                // Guardar estado de cada llanta
                if (idPaso4LogVehiculo > 0 || datosExistentes.isNotEmpty()) {
                    val checkboxes = mapOf(
                        1.toByte() to cbLlanta1.isChecked,
                        2.toByte() to cbLlanta2.isChecked,
                        3.toByte() to cbLlanta3.isChecked,
                        4.toByte() to cbLlanta4.isChecked
                    )

                    for ((posicion, verificada) in checkboxes) {
                        val yaExiste = datosExistentes.containsKey(posicion)
                        if (!yaExiste) {
                            val resultado = dalVehiculo.insertarFotoPaso4(
                                idPaso4LogVehiculo = idPaso4LogVehiculo,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                posicion = posicion,
                                verificada = verificada,
                                fotoBase64 = null // Sin foto por ahora
                            )
                            if (!resultado) exitoso = false
                        }
                    }
                }

                ocultarCarga()

                if (exitoso) {
                    Toast.makeText(this@Paso4Llantas_Activity,
                        "✅ Verificación de llantas guardada exitosamente",
                        Toast.LENGTH_LONG).show()

                    // Actualizar datos existentes
                    datosExistentes = dalVehiculo.consultarPaso4Existente(vehiculo.Id.toInt())
                    cargarDatosExistentes()
                } else {
                    Toast.makeText(this@Paso4Llantas_Activity,
                        "❌ Error guardando verificación",
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("Paso4LLANTAS", "💥 Error guardando Paso 4: ${e.message}")
                Toast.makeText(this@Paso4Llantas_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarCargaConMensajes() {
        loadingContainer.visibility = View.VISIBLE
        btnGuardarPaso4.isEnabled = false
        btnGuardarPaso4.alpha = 0.5f

        val mensajes = arrayOf(
            "Preparando datos..." to "Organizando información de llantas",
            "Guardando verificaciones..." to "Actualizando base de datos",
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
                    loadingHandler?.postDelayed(this, 1500)
                }
            }
        }
        loadingRunnable?.let { loadingHandler?.post(it) }
    }

    private fun ocultarCarga() {
        loadingContainer.visibility = View.GONE
        btnGuardarPaso4.isEnabled = true
        btnGuardarPaso4.alpha = 1.0f

        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun mostrarCargaConsulta() {
        loadingContainer.visibility = View.VISIBLE
        btnConsultarVehiculo.isEnabled = false
        btnConsultarVehiculo.alpha = 0.5f
    }

    private fun ocultarCargaConsulta() {
        loadingContainer.visibility = View.GONE
        btnConsultarVehiculo.isEnabled = true
        btnConsultarVehiculo.alpha = 1.0f
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}