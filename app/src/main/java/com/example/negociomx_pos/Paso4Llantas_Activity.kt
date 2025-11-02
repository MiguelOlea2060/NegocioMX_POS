package com.example.negociomx_pos

import android.Manifest
import android.app.Activity
import android.content.Context
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
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.VehiculoPaso4
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.ApiUploadUtil
import com.example.negociomx_pos.Utils.BLLUtils
import com.example.negociomx_pos.Utils.ParametrosSistema
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Paso4Llantas_Activity : AppCompatActivity() {

    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: VehiculoPaso4? = null

    // Variables de UI
    private lateinit var etVIN: EditText
    private lateinit var btnConsultarVehiculo: Button
    private lateinit var layoutInfoVehiculo: LinearLayout
    private lateinit var layoutLlantas: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private lateinit var btnGuardarPaso4: Button
   // private lateinit var tvMensajeInfo: TextView

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
    private lateinit var btnRegresar:ImageView
    // Variables para manejo de loading
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // Variables para control de datos
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.IdUsuario!!

    // ✅ VARIABLES PARA CAPTURA DE FOTOS (ADAPTADAS DE PASO3)
    private var fotoHabilitada: Boolean = true // HABILITADA AHORA
    private var posicionFotoActual: Byte = 0
    private var fotoUri: Uri? = null


    // Variables para foto de evidencia del botón
    private lateinit var btnEvidenciaPaso4: Button
    private lateinit var tvEstadoEvidenciaPaso4: TextView
    private var fotoEvidenciaCapturada: Boolean = false
    private var archivoFotoEvidencia: File? = null

    var bllUtil:BLLUtils?=null
    // Variables para manejo de fotos por posición
    private val fotosCapturadas = mutableMapOf<Byte, File>() // Posicion -> Archivo
    private val estadoCaptura = mutableMapOf<Byte, Boolean>() // Posicion -> Capturada

    // ✅ LAUNCHER PARA CÁMARA (PREPARADO PERO DESHABILITADO)
    // ✅ LAUNCHER PARA CÁMARA (ADAPTADO DE PASO3)
    private val camaraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            fotoUri?.let { uri ->
                procesarFoto(uri, posicionFotoActual)
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
        setContentView(R.layout.activity_paso4_llantas)

        bllUtil= BLLUtils()

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
        // Botón de evidencia
        btnEvidenciaPaso4 = findViewById(R.id.btnEvidenciaPaso4)
        tvEstadoEvidenciaPaso4 = findViewById(R.id.tvEstadoEvidenciaPaso4)

        btnRegresar=findViewById(R.id.btnRegresarPaso4)
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
        if(ParametrosSistema.GuardaFotosPaso4) {
            ivLlanta1.setOnClickListener { manejarClicLlanta(1) }
            ivLlanta2.setOnClickListener { manejarClicLlanta(2) }
            ivLlanta3.setOnClickListener { manejarClicLlanta(3) }
            ivLlanta4.setOnClickListener { manejarClicLlanta(4) }
        }

        // ✅ BOTÓN GUARDAR
        btnGuardarPaso4.setOnClickListener {
            guardarPaso4()
        }


        btnRegresar.setOnClickListener {
            finish()
        }
        btnEvidenciaPaso4.setOnClickListener {
            // <CHANGE> Verificar estado del botón antes de ejecutar acción
            if (!btnEvidenciaPaso4.isEnabled) {
                Toast.makeText(this, "No se capturó foto en este registro", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (vehiculoActual?.TieneFoto1 == true) {
                verFotoEvidenciaExistente()
            } else {
                capturarFotoEvidencia()
            }
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
                val vehiculo = dalVehiculo.consultarVehiculoPorVINParaPaso4(vin)
                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    mostrarInformacionVehiculo(vehiculo)
                    mostrarSeccionLlantas()
                    cargarDatosExistentes()
                    cargarDatosExistentes()
                    configurarBotonEvidenciaSegunFoto()

                    activaDesactivaControlesParaEdicion(true)
                    if(vehiculoActual?.IdPaso4LogVehiculo!!>0)
                        activaDesactivaControlesParaEdicion(false)

                    configurarBotonGuardar()

                    var verificadas:Int=0
                    if(vehiculo.Verificada1==true)verificadas++
                    if(vehiculo.Verificada2==true)verificadas++
                    if(vehiculo.Verificada3==true)verificadas++
                    if(vehiculo.Verificada4==true)verificadas++
                    Toast.makeText(
                        this@Paso4Llantas_Activity,
                        "✅ Vehículo encontrado. ${verificadas} llantas verificadas",
                        Toast.LENGTH_SHORT
                    ).show()

                    ocultarCargaConsulta()
                    hideKeyboard()
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

    private fun activaDesactivaControlesParaEdicion(enabled:Boolean) {
        cbLlanta1.isEnabled = enabled
        cbLlanta2.isEnabled = enabled
        cbLlanta3.isEnabled = enabled
        cbLlanta4.isEnabled = enabled
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun configurarBotonGuardar() {
        when {
            (vehiculoActual  !=null && vehiculoActual?.IdPaso4LogVehiculo!!>0)-> {
                btnGuardarPaso4.text = "⬅️ ATRÁS"


            }
            else -> {
                btnGuardarPaso4.text = "💾 GUARDAR"

            }
        }
    }

    private fun configurarBotonEvidenciaSegunFoto() {
        vehiculoActual?.let { paso4 ->
            // <CHANGE> Verificar si ya existe registro guardado
            val tieneRegistroGuardado = paso4.IdPaso4LogVehiculo!! > 0

            if (paso4.TieneFoto1 == true) {
                // Ya tiene foto guardada - Modo VER
                btnEvidenciaPaso4.text = "👁️ Ver Foto"
                btnEvidenciaPaso4.isEnabled = true
                btnEvidenciaPaso4.alpha = 1.0f
                tvEstadoEvidenciaPaso4.text = "📷"
            } else if (tieneRegistroGuardado) {
                // Tiene registro pero NO tiene foto - Modo DESHABILITADO
                btnEvidenciaPaso4.text = "❌ Sin Foto"
                btnEvidenciaPaso4.isEnabled = false
                btnEvidenciaPaso4.alpha = 0.5f
                tvEstadoEvidenciaPaso4.text = "❌"
            } else {
                // No tiene registro - Modo CAPTURAR
                btnEvidenciaPaso4.text = "📷 Foto Evidencia"
                btnEvidenciaPaso4.isEnabled = true
                btnEvidenciaPaso4.alpha = 1.0f
                tvEstadoEvidenciaPaso4.text = "❌"
            }
        } ?: run {
            // Si no hay vehículo, configurar para captura
            btnEvidenciaPaso4.text = "📷 Foto Evidencia"
            btnEvidenciaPaso4.isEnabled = true
            btnEvidenciaPaso4.alpha = 1.0f
            tvEstadoEvidenciaPaso4.text = "❌"
        }
    }

    private fun mostrarInformacionVehiculo(vehiculo: VehiculoPaso4) {
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
        cbLlanta1.isChecked = vehiculoActual?.Verificada1==true
        cbLlanta2.isChecked = vehiculoActual?.Verificada2==true
        cbLlanta3.isChecked = vehiculoActual?.Verificada3==true
        cbLlanta4.isChecked = vehiculoActual?.Verificada4==true

        // Actualizar mensaje informativo
        var totalVerificadas = 0
        var totalFotos = 0
        if(vehiculoActual?.Verificada1==true)totalVerificadas++
        if(vehiculoActual?.Verificada2==true)totalVerificadas++
        if(vehiculoActual?.Verificada3==true)totalVerificadas++
        if(vehiculoActual?.Verificada4==true)totalVerificadas++

        if(vehiculoActual?.TieneFoto1==true)totalFotos++
        if(vehiculoActual?.TieneFoto2==true)totalFotos++
        if(vehiculoActual?.TieneFoto3==true)totalFotos++
        if(vehiculoActual?.TieneFoto4==true)totalFotos++
       // tvMensajeInfo.text = "ℹ️ $totalVerificadas llantas verificadas, $totalFotos  fotos capturadas."


        // <CHANGE> Actualizar estado de foto de evidencia
        if (vehiculoActual?.TieneFoto1 == true) {
            tvEstadoEvidenciaPaso4.text = "✅"
        } else {
            tvEstadoEvidenciaPaso4.text = "❌"
        }
    }

    private fun manejarClicLlanta(posicion: Int) {
        val posicionByte = posicion.toByte()
        var tieneFoto = false
        if(posicion==1 && vehiculoActual?.TieneFoto1==true)tieneFoto=true
        else if(posicion==2 && vehiculoActual?.TieneFoto2==true)tieneFoto=true
        else if(posicion==3 && vehiculoActual?.TieneFoto3==true)tieneFoto=true
        else if(posicion==4 && vehiculoActual?.TieneFoto4==true)tieneFoto=true

        val fotoCapturada = estadoCaptura[posicionByte] ?: false

        if (tieneFoto) {
            // Mostrar foto existente guardada en BD
            mostrarFotoExistente(posicionByte)
        } else if (fotoCapturada) {
            // Mostrar foto capturada pero no guardada
            mostrarFotoCapturadaLocal(posicionByte)
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

        // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA
        if (estadoCaptura[posicion] == true) {
            Toast.makeText(this, "Ya tiene foto capturada para esta llanta. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ VALIDAR SI YA EXISTE EN BD
        if ((vehiculoActual?.TieneFoto1 == true && posicion.toInt()==1) ||
            (vehiculoActual?.TieneFoto2==true && posicion.toInt()==2) ||
            (vehiculoActual?.TieneFoto3==true && posicion.toInt()==3) ||
            (vehiculoActual?.TieneFoto4==true && posicion.toInt()==4)) {
            Toast.makeText(this, "Esta llanta ya tiene foto registrada", Toast.LENGTH_LONG).show()
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


    private fun capturarFotoEvidencia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // Validar que haya un vehículo consultado
        if (vehiculoActual == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar si ya tiene foto capturada
        if (fotoEvidenciaCapturada) {
            Toast.makeText(this, "Ya tiene foto de evidencia capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar si ya existe en BD
        if (vehiculoActual?.TieneFoto1 == true) {
            Toast.makeText(this, "Ya existe foto de evidencia registrada", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "${vehiculoActual?.VIN}_Paso_4_Foto_1.jpg"
            val storageDir = File(getExternalFilesDir(null), "Paso4_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)
            posicionFotoActual = 1 // Posición 1 para la foto de evidencia

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error creando archivo de foto evidencia: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarFoto(uri: Uri, posicion: Byte) {
        try {
            Log.d("Paso4LLANTAS", "📸 Procesando foto llanta posición $posicion")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(this@Paso4Llantas_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(this@Paso4Llantas_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoFinal = if (archivoLocal.length() > 1.8 * 1024 * 1024) {
                Log.d("Paso4LLANTAS", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal, posicion)
            } else {
                archivoLocal
            }

           /* fotosCapturadas[posicion] = archivoFinal
            estadoCaptura[posicion] = true

            // Actualizar UI para mostrar que la foto fue capturada
            actualizarEstadoFotoUI(posicion, true)

            Toast.makeText(this@Paso4Llantas_Activity, "✅ Foto llanta $posicion capturada (sin guardar)", Toast.LENGTH_SHORT).show()

            Log.d("Paso4LLANTAS", "✅ Foto llanta $posicion lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this@Paso4Llantas_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }*/


            // <CHANGE> Diferenciar entre foto de evidencia (posición 1) y fotos de llantas
            if (posicion == 1.toByte()) {
                // Es la foto de evidencia del botón
                archivoFotoEvidencia = archivoFinal
                fotoEvidenciaCapturada = true
                tvEstadoEvidenciaPaso4.text = "✅"
                Toast.makeText(this@Paso4Llantas_Activity, "✅ Foto de evidencia capturada (sin guardar)", Toast.LENGTH_SHORT).show()
            } else {
                // Es foto de llanta individual
                fotosCapturadas[posicion] = archivoFinal
                estadoCaptura[posicion] = true
                actualizarEstadoFotoUI(posicion, true)
                Toast.makeText(this@Paso4Llantas_Activity, "✅ Foto llanta $posicion capturada (sin guardar)", Toast.LENGTH_SHORT).show()
            }

            Log.d("Paso4LLANTAS", "✅ Foto posición $posicion lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this@Paso4Llantas_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

    private fun verFotoEvidenciaExistente() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso4Llantas_Activity, "Cargando foto de evidencia...", Toast.LENGTH_SHORT).show()

                var fotoBase64: String? = null

                // ✅ VERIFICAR SI DEBE CARGAR DESDE BD O DESDE WEB API
                if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true) {
                    // Cargar desde Base de Datos
                    fotoBase64 = dalVehiculo.obtenerFotoBase64Paso4(vehiculo.Id.toInt(), 1)
                }

                if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true &&
                    fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    // Mostrar desde Base64
                    mostrarDialogoFoto(fotoBase64, 1)
                } else if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == false) {
                    // Cargar desde Web API
                    val nombreArchivo = "${vehiculoActual?.VIN}_Paso_4_Foto_1.jpg"
                    val urlCompletoFoto = ParametrosSistema.cfgApp?.UrlGuardadoArchivos + '/' +
                            ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube?.replace("~/", "") +
                            '/' + nombreArchivo
                    mostrarDialogoFotoFromUrl(urlCompletoFoto, 1)
                } else {
                    Toast.makeText(
                        this@Paso4Llantas_Activity,
                        "No se pudo cargar la foto de evidencia",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Paso4LLANTAS", "Error cargando foto de evidencia: ${e.message}")
                Toast.makeText(
                    this@Paso4Llantas_Activity,
                    "Error cargando foto: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun mostrarDialogoFotoFromUrl(url: String, posicion: Byte) {
        try {
            // ✅ CARGAR IMAGEN DESDE URL EN BACKGROUND THREAD
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                bllUtil?.mLoad(url)
            }

            if (bitmap != null) {
                // Crear diálogo personalizado
                val dialog = android.app.AlertDialog.Builder(this@Paso4Llantas_Activity)
                val imageView = android.widget.ImageView(this@Paso4Llantas_Activity)

                // Configurar ImageView
                imageView.setImageBitmap(bitmap)
                imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true

                val nombrePosicion = when (posicion) {
                    1.toByte() -> "Foto de Evidencia"
                    2.toByte() -> "Delantera Derecha"
                    3.toByte() -> "Trasera Izquierda"
                    4.toByte() -> "Trasera Derecha"
                    else -> "Posición $posicion"
                }

                // Configurar diálogo
                dialog.setTitle("Paso 4 - $nombrePosicion")
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
                Toast.makeText(
                    this@Paso4Llantas_Activity,
                    "Error decodificando la imagen",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error mostrando foto desde URL: ${e.message}")
            Toast.makeText(
                this@Paso4Llantas_Activity,
                "Error mostrando foto",
                Toast.LENGTH_SHORT
            ).show()
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
                // ✅ MANEJAR BOTÓN ATRÁS
                if (vehiculoActual!=null && vehiculoActual?.IdPaso4LogVehiculo!!.toInt()>0) {
                    ocultarCarga()
                    finish() // Cerrar actividad
                    return@launch
                }

                Toast.makeText(this@Paso4Llantas_Activity, "Guardando verificación de llantas...", Toast.LENGTH_SHORT).show()
                // Insertar registro principal si no existe
                var idPaso4LogVehiculo = -1
                if (vehiculoActual!=null) {
                    idPaso4LogVehiculo = dalVehiculo.insertarPaso4LogVehiculo(
                        idVehiculo = vehiculo.Id.toInt(),
                        idUsuarioNubeAlta = idUsuarioNubeAlta
                    )
                }

                var exitoso = true
                // Guardar estado de cada llanta CON FOTOS
                if (idPaso4LogVehiculo > 0 || vehiculoActual!=null) {
                    val checkboxes = mapOf(
                        1.toByte() to cbLlanta1.isChecked,
                        2.toByte() to cbLlanta2.isChecked,
                        3.toByte() to cbLlanta3.isChecked,
                        4.toByte() to cbLlanta4.isChecked
                    )
                   /* for ((posicion, verificada) in checkboxes) {
                        var yaExiste = false
                        if(vehiculoActual?.TieneFoto1==true && posicion.toInt()==1)yaExiste=true
                        else if(vehiculoActual?.TieneFoto2==true && posicion.toInt()==2)yaExiste=true
                        else if(vehiculoActual?.TieneFoto3==true && posicion.toInt()==3)yaExiste=true
                        else if(vehiculoActual?.TieneFoto4==true && posicion.toInt()==4)yaExiste=true
                        if (!yaExiste) {
                            // Obtener foto si fue capturada
                            var fotoBase64: String? = null
                            val archivoFoto = fotosCapturadas[posicion]
                            if (archivoFoto != null) {
                                fotoBase64 =bllUtil?.convertirImagenABase64(archivoFoto)
                            }

                            val resultado = dalVehiculo.insertarFotoPaso4(
                                idPaso4LogVehiculo = idPaso4LogVehiculo,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                posicion = posicion,
                                verificada = verificada,
                                fotoBase64 = fotoBase64
                            )
                            if (!resultado) exitoso = false
                        }
                    }*/

                    // <CHANGE> Primero guardar la foto de evidencia si fue capturada (posición 1)
                 /*   if (fotoEvidenciaCapturada && archivoFotoEvidencia != null && vehiculoActual?.TieneFoto1 != true) {
                        val fotoBase64Evidencia = bllUtil?.convertirImagenABase64(archivoFotoEvidencia!!)
                        val resultadoEvidencia = dalVehiculo.insertarFotoPaso4(
                            idPaso4LogVehiculo = idPaso4LogVehiculo,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            posicion = 1,
                            verificada = cbLlanta1.isChecked,
                            fotoBase64 = fotoBase64Evidencia
                        )
                        if (!resultadoEvidencia) exitoso = false
                    }

// Luego guardar las fotos de las llantas (posiciones 2, 3, 4)
                    for ((posicion, verificada) in checkboxes) {
                        // <CHANGE> Saltar posición 1 si ya se guardó la foto de evidencia
                        if (posicion == 1.toByte() && fotoEvidenciaCapturada) {
                            continue
                        }

                        var yaExiste = false
                        if(vehiculoActual?.TieneFoto1==true && posicion.toInt()==1)yaExiste=true
                        else if(vehiculoActual?.TieneFoto2==true && posicion.toInt()==2)yaExiste=true
                        else if(vehiculoActual?.TieneFoto3==true && posicion.toInt()==3)yaExiste=true
                        else if(vehiculoActual?.TieneFoto4==true && posicion.toInt()==4)yaExiste=true
                        if (!yaExiste) {
                            // Obtener foto si fue capturada
                            var fotoBase64: String? = null
                            val archivoFoto = fotosCapturadas[posicion]
                            if (archivoFoto != null) {
                                fotoBase64 =bllUtil?.convertirImagenABase64(archivoFoto)
                            }

                            val resultado = dalVehiculo.insertarFotoPaso4(
                                idPaso4LogVehiculo = idPaso4LogVehiculo,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                posicion = posicion,
                                verificada = verificada,
                                fotoBase64 = fotoBase64
                            )
                            if (!resultado) exitoso = false
                        }
                    }*/


                    // <CHANGE> Primero guardar la foto de evidencia si fue capturada (posición 1)
                    if (fotoEvidenciaCapturada && archivoFotoEvidencia != null && vehiculoActual?.TieneFoto1 != true) {
                        // ✅ GENERAR NOMBRE DE ARCHIVO
                        var nombreArchivo = "${vehiculoActual?.VIN}_Paso_4_Foto_1.jpg"

                        // ✅ CONVERTIR IMAGEN A BASE64
                        var fotoBase64Evidencia: String? = bllUtil?.convertirImagenABase64(archivoFotoEvidencia!!)

                        // ✅ VALIDAR SI DEBE GUARDAR EN API O EN BASE DE DATOS
                        if (ParametrosSistema.cfgApp != null &&
                            ParametrosSistema.cfgApp!!.ManejaGuardadoArchivosEnBD == false) {
                            // Subir a API Web
                            val urlBase = ParametrosSistema.cfgApp!!.UrlGuardadoArchivos + '/' +
                                    ParametrosSistema.cfgApp!!.UrlAPIControllerGuardadoArchivos

                            val resultadoSubida = ApiUploadUtil.subirFoto(
                                urlBase = urlBase,
                                nombreArchivo = nombreArchivo,
                                file = archivoFotoEvidencia!!,
                                vin = vehiculoActual!!.VIN,
                                paso = 4,
                                numeroFoto = 1
                            )

                            // Si la subida fue exitosa, NO guardar en BD (setear fotoBase64 a null)
                            if (resultadoSubida.first) {
                                fotoBase64Evidencia = null
                                Log.d("Paso4LLANTAS", "✅ Foto evidencia subida a API exitosamente")
                            } else {
                                Log.e("Paso4LLANTAS", "❌ Error subiendo foto evidencia a API: ${resultadoSubida.second}")
                            }
                        }

                        val resultadoEvidencia = dalVehiculo.insertarFotoPaso4(
                            idPaso4LogVehiculo = idPaso4LogVehiculo,
                            idUsuarioNubeAlta = idUsuarioNubeAlta,
                            posicion = 1,
                            verificada = cbLlanta1.isChecked,
                            fotoBase64 = fotoBase64Evidencia,
                            nombreArchivo=nombreArchivo
                        )
                        if (!resultadoEvidencia) exitoso = false
                    }

// Luego guardar las fotos de las llantas (posiciones 2, 3, 4)
                    for ((posicion, verificada) in checkboxes) {
                        // <CHANGE> Saltar posición 1 si ya se guardó la foto de evidencia
                        if (posicion == 1.toByte() && fotoEvidenciaCapturada) {
                            continue
                        }

                        var yaExiste = false
                        if(vehiculoActual?.TieneFoto1==true && posicion.toInt()==1)yaExiste=true
                        else if(vehiculoActual?.TieneFoto2==true && posicion.toInt()==2)yaExiste=true
                        else if(vehiculoActual?.TieneFoto3==true && posicion.toInt()==3)yaExiste=true
                        else if(vehiculoActual?.TieneFoto4==true && posicion.toInt()==4)yaExiste=true

                        if (!yaExiste) {
                            // Obtener foto si fue capturada
                            var fotoBase64: String? = null
                            val archivoFoto = fotosCapturadas[posicion]

                            if (archivoFoto != null) {
                                // ✅ GENERAR NOMBRE DE ARCHIVO PARA CADA POSICIÓN
                                val nombreArchivo = "${vehiculoActual?.VIN}_Paso_4_Foto_${posicion}.jpg"

                                // ✅ CONVERTIR IMAGEN A BASE64
                                fotoBase64 = bllUtil?.convertirImagenABase64(archivoFoto)

                                // ✅ VALIDAR SI DEBE GUARDAR EN API O EN BASE DE DATOS
                                if (ParametrosSistema.cfgApp != null &&
                                    ParametrosSistema.cfgApp!!.ManejaGuardadoArchivosEnBD == false) {
                                    // Subir a API Web
                                    val urlBase = ParametrosSistema.cfgApp!!.UrlGuardadoArchivos + '/' +
                                            ParametrosSistema.cfgApp!!.UrlAPIControllerGuardadoArchivos

                                    val resultadoSubida = ApiUploadUtil.subirFoto(
                                        urlBase = urlBase,
                                        nombreArchivo = nombreArchivo,
                                        file = archivoFoto,
                                        vin = vehiculoActual!!.VIN,
                                        paso = 4,
                                        numeroFoto = posicion.toInt()
                                    )

                                    // Si la subida fue exitosa, NO guardar en BD (setear fotoBase64 a null)
                                    if (resultadoSubida.first) {
                                        fotoBase64 = null
                                        Log.d("Paso4LLANTAS", "✅ Foto llanta $posicion subida a API exitosamente")
                                    } else {
                                        Log.e("Paso4LLANTAS", "❌ Error subiendo foto llanta $posicion a API: ${resultadoSubida.second}")
                                    }
                                }
                            }

                            val resultado = dalVehiculo.insertarFotoPaso4(
                                idPaso4LogVehiculo = idPaso4LogVehiculo,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                posicion = posicion,
                                verificada = verificada,
                                fotoBase64 = fotoBase64,
                                nombreArchivo = ""
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

                    // <CHANGE> Actualizar estado del vehículo después de guardar
                    if (fotoEvidenciaCapturada) {
                        vehiculoActual?.TieneFoto1 = true
                    }

                    // <CHANGE> Reconfigurar botón para modo VER
                    configurarBotonEvidenciaSegunFoto()

                    // Limpiar fotos capturadas después de guardar exitosamente
                    limpiarControles()
                }else {
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

    private fun limpiarControles() {
        fotosCapturadas.clear()
        estadoCaptura.clear()

        ocultarSecciones()
        cbLlanta1.isChecked=false
        cbLlanta2.isChecked=false
        cbLlanta3.isChecked=false
        cbLlanta4.isChecked=false
        // <CHANGE> Limpiar foto de evidencia
        fotoEvidenciaCapturada = false
        archivoFotoEvidencia = null
        tvEstadoEvidenciaPaso4.text = "❌"

        etVIN.setText("")
        etVIN.requestFocus()
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

    // ✅ MÉTODOS AUXILIARES ADAPTADOS DE PASO3
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
                val tempFile = File(getExternalFilesDir(null), "temp_paso4_photo_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error obteniendo archivo desde URI: ${e.message}")
            null
        }
    }

    private fun comprimirImagen(archivoOriginal: File, posicion: Byte): File {
        return try {
            val bitmap = BitmapFactory.decodeFile(archivoOriginal.absolutePath)

            val maxSize = 1800
            var ratio: Float = 1.0F
            if (bitmap.width > bitmap.height)
                ratio = maxSize.toFloat() / bitmap.width
            else
                ratio = maxSize.toFloat() / bitmap.height

            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            val bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_paso4_llanta${posicion}_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso4LLANTAS", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido

        } catch (e: Exception) {
            Log.e("Paso4LLANTAS", "Error comprimiendo imagen: ${e.message}")
            archivoOriginal
        }
    }

    private fun actualizarEstadoFotoUI(posicion: Byte, capturada: Boolean) {
        val nombrePosicion = when (posicion) {
            1.toByte() -> "Delantera Izq"
            2.toByte() -> "Delantera Der"
            3.toByte() -> "Trasera Izq"
            4.toByte() -> "Trasera Der"
            else -> "Posición $posicion"
        }

        // Actualizar mensaje informativo
        val totalCapturadas = estadoCaptura.values.count { it }
        var totalGuardadas = 0
        if (vehiculoActual?.TieneFoto1==true)totalGuardadas++
        if (vehiculoActual?.TieneFoto2==true)totalGuardadas++
        if (vehiculoActual?.TieneFoto3==true)totalGuardadas++
        if (vehiculoActual?.TieneFoto4==true)totalGuardadas++
   //     tvMensajeInfo.text = "ℹ️ $totalGuardadas fotos guardadas, $totalCapturadas nuevas capturadas. Toque imagen para ver/capturar."
    }

    private fun mostrarFotoCapturadaLocal(posicion: Byte) {
        val archivo = fotosCapturadas[posicion]
        if (archivo != null && archivo.exists()) {
            try {
                val bitmap = BitmapFactory.decodeFile(archivo.absolutePath)
                if (bitmap != null) {
                    mostrarDialogoFotoLocal(bitmap, posicion)
                } else {
                    Toast.makeText(this, "Error cargando foto capturada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Paso4LLANTAS", "Error mostrando foto local: ${e.message}")
                Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoFotoLocal(bitmap: Bitmap, posicion: Byte) {
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

        dialog.setTitle("Paso 4 - Llanta $nombrePosicion (Sin Guardar)")
        dialog.setView(imageView)
        dialog.setPositiveButton("Retomar Foto") { _, _ ->
            // Permitir retomar la foto
            estadoCaptura[posicion] = false
            fotosCapturadas.remove(posicion)
            capturarFotoLlanta(posicion)
        }
        dialog.setNegativeButton("Cerrar") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog = dialog.create()
        alertDialog.show()

        val window = alertDialog.window
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            (resources.displayMetrics.heightPixels * 0.7).toInt()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}