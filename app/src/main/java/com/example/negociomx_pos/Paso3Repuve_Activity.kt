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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.Paso3LogVehiculo
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.BE.VehiculoPaso3
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.BLLUtils
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.databinding.ActivityPaso3RepuveBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.negociomx_pos.Utils.ApiUploadUtil

class Paso3Repuve_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityPaso3RepuveBinding
    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: VehiculoPaso3? = null

    // Variables para manejo de loading
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // Variables para foto
    private var evidenciaFile: File? = null
    private var evidenciaCapturada: Boolean = false
    private var fotoUri: Uri? = null


    // Variables para control de datos
    private var idUsuarioNubeAlta: Int = ParametrosSistema.usuarioLogueado.IdUsuario

    var bllUtil:BLLUtils?=null

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
        binding = ActivityPaso3RepuveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bllUtil=BLLUtils()

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

        // ✅ BOTÓN DE EVIDENCIA
        binding.btnEvidencia.setOnClickListener {
            if (vehiculoActual?.TieneFoto == true) {
                verFotoExistente()
            } else {
                capturarEvidencia()
            }
        }

        // ✅ BOTÓN GUARDAR
        binding.btnGuardarPaso3.setOnClickListener {
            guardarPaso3()
        }
        binding.btnRegresarPaso3.setOnClickListener {
            finish()
        }


        // ✅ CONFIGURAR LOADING CONTAINER
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun configurarBotonGuardar() {
        when {
            vehiculoActual!=null && vehiculoActual?.IdPaso3LogVehiculo!!>0 -> {
                binding.btnGuardarPaso3.text = "⬅️ ATRÁS"
            }
            else -> {
                binding.btnGuardarPaso3.text = "💾 GUARDAR"
            }
        }
    }

    private fun consultarVehiculo(vin: String) {
        lifecycleScope.launch {
            try {
                Log.d("Paso3REPUVE", "🔍 Consultando vehículo con VIN: $vin")
                mostrarCargaConsulta()

                Toast.makeText(this@Paso3Repuve_Activity, "Consultando vehículo...", Toast.LENGTH_SHORT).show()

                val vehiculo = dalVehiculo.consultarVehiculoPorVINParaPaso3(vin)
                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    mostrarInformacionVehiculo(vehiculo)
                    mostrarSeccionEvidencia()
                    configurarBotonSegunFoto()
                    configurarBotonGuardar()

                    if (vehiculoActual?.TieneFoto == true) {
                        Toast.makeText(
                            this@Paso3Repuve_Activity,
                            "✅ Vehículo encontrado. Ya tiene foto REPUVE registrada",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this@Paso3Repuve_Activity,
                            "✅ Vehículo encontrado. Sin foto REPUVE previa",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                    ocultarCargaConsulta()
                    hideKeyboard()
                } else {
                    ocultarSecciones()
                    Toast.makeText(this@Paso3Repuve_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                    ocultarCargaConsulta()
                    binding.etVIN.selectAll()
                }

            } catch (e: Exception) {
                Log.e("Paso3REPUVE", "💥 Error consultando vehículo: ${e.message}")
                Toast.makeText(this@Paso3Repuve_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                ocultarCargaConsulta()
                binding.etVIN.selectAll()
            }
        }
    }

    private fun mostrarInformacionVehiculo(vehiculo: VehiculoPaso3) {
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

    private fun mostrarSeccionEvidencia() {
        binding.apply {
            layoutEvidencia.visibility = View.VISIBLE
            btnGuardarPaso3.visibility = View.VISIBLE
        }
    }

    private fun ocultarSecciones() {
        binding.apply {
            layoutInfoVehiculo.visibility = View.GONE
            layoutEvidencia.visibility = View.GONE
            btnGuardarPaso3.visibility = View.GONE
        }
    }

   private fun configurarBotonSegunFoto() {
       vehiculoActual?.let { paso3 ->
           if (paso3.TieneFoto) {
               binding.btnEvidencia.text = "👁️ Ver Repuve"
               binding.tvEstadoEvidencia.text = "📷"
             //  binding.btnGuardarPaso3.alpha = 0.5f

           } else {
               binding.btnEvidencia.text = "📷 Repuve"
               binding.tvEstadoEvidencia.text = "❌"
           }
       } ?: run {
           // Si no hay registro existente, configurar para nueva foto
           binding.btnEvidencia.text = "📷 Repuve"
           binding.tvEstadoEvidencia.text = "❌"
       }
   }

    private fun capturarEvidencia() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA
        if (evidenciaCapturada) {
            Toast.makeText(this, "Ya tiene evidencia capturada. Presione Guardar para confirmar.", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ VALIDAR SI YA EXISTE EN BD
        if (vehiculoActual?.TieneFoto == true) {
            Toast.makeText(this, "Este vehículo ya tiene foto REPUVE registrada", Toast.LENGTH_LONG).show()
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "Paso3_REPUVE_${timeStamp}.jpg"
            val storageDir = File(getExternalFilesDir(null), "Paso3_Photos")

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val photoFile = File(storageDir, imageFileName)
            fotoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", photoFile)

            camaraLauncher.launch(fotoUri)

        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "Error creando archivo de foto: ${e.message}")
            Toast.makeText(this, "Error preparando cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun procesarFoto(uri: Uri) {
        try {
            Log.d("Paso3REPUVE", "📸 Procesando foto REPUVE")

            val vehiculo = vehiculoActual
            if (vehiculo == null) {
                Toast.makeText(this@Paso3Repuve_Activity, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoLocal = obtenerArchivoDesdeUri(uri)

            if (archivoLocal == null || !archivoLocal.exists()) {
                Toast.makeText(this@Paso3Repuve_Activity, "Error: Archivo de foto no encontrado", Toast.LENGTH_SHORT).show()
                return
            }

            val archivoFinal = if (archivoLocal.length() > 1.8 * 1024 * 1024) {
                Log.d("Paso3REPUVE", "📦 Comprimiendo imagen de ${archivoLocal.length()} bytes")
                comprimirImagen(archivoLocal)
            } else {
                archivoLocal
            }

            evidenciaFile = archivoFinal
            evidenciaCapturada = true
            binding.tvEstadoEvidencia.text = "📷"

            // ✅ ACTUALIZAR ESTADO SIN VISTA PREVIA
            binding.tvEstadoEvidencia.text = "📷"


            Toast.makeText(this@Paso3Repuve_Activity, "✅ Foto REPUVE capturada (sin guardar)", Toast.LENGTH_SHORT).show()

            Log.d("Paso3REPUVE", "✅ Evidencia REPUVE lista para guardar")

        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "💥 Error procesando foto: ${e.message}")
            Toast.makeText(this@Paso3Repuve_Activity, "Error procesando foto: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun verFotoExistente() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso3Repuve_Activity, "Cargando foto...", Toast.LENGTH_SHORT).show()

                var fotoBase64: String? = null

                // ✅ VERIFICAR SI DEBE CARGAR DESDE BD O DESDE WEB API
                if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true) {
                    // Cargar desde Base de Datos
                    fotoBase64 = dalVehiculo.obtenerFotoBase64Paso3(vehiculo.Id.toInt())
                }

                if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true &&
                    fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    // Mostrar desde Base64
                    mostrarDialogoFoto(fotoBase64)
                } else if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == false) {
                    // Cargar desde Web API
                    val urlCompletoFoto = ParametrosSistema.cfgApp?.UrlGuardadoArchivos + '/' +
                            ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube?.replace("~/", "") +
                            '/' + vehiculoActual?.NombreArchivoFoto
                    mostrarDialogoFotoFromUrl(urlCompletoFoto)
                } else {
                    Toast.makeText(
                        this@Paso3Repuve_Activity,
                        "No se pudo cargar la foto",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Paso3REPUVE", "Error cargando foto: ${e.message}")
                Toast.makeText(
                    this@Paso3Repuve_Activity,
                    "Error cargando foto: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun mostrarDialogoFoto(fotoBase64: String) {
        try {
            val decodedBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                val dialog = android.app.AlertDialog.Builder(this)
                val imageView = android.widget.ImageView(this)

                imageView.setImageBitmap(bitmap)
                imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true

                dialog.setTitle("Paso 3 - Foto REPUVE")
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
            Log.e("Paso3REPUVE", "Error mostrando foto: ${e.message}")
            Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoFotoFromUrl(url: String) {
        lifecycleScope.launch {
            try {
                // ✅ CARGAR IMAGEN DESDE URL EN BACKGROUND THREAD
                val bitmap = withContext(Dispatchers.IO) {
                    bllUtil?.mLoad(url)
                }

                if (bitmap != null) {
                    // Crear diálogo personalizado
                    val dialog = android.app.AlertDialog.Builder(this@Paso3Repuve_Activity)
                    val imageView = android.widget.ImageView(this@Paso3Repuve_Activity)

                    // Configurar ImageView
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    imageView.adjustViewBounds = true

                    // Configurar diálogo
                    dialog.setTitle("Paso 3 - Foto REPUVE")
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
                        this@Paso3Repuve_Activity,
                        "Error decodificando la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {
                Log.e("Paso3REPUVE", "Error mostrando foto: ${e.message}")
                Toast.makeText(
                    this@Paso3Repuve_Activity,
                    "Error mostrando foto",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun guardarPaso3() {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Primero consulte un vehículo", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ VALIDAR QUE TENGA LA FOTO OBLIGATORIA
        if (vehiculoActual?.IdPaso3LogVehiculo==0 && ( !evidenciaCapturada || evidenciaFile == null)) {
            Toast.makeText(this, "Debe tomar la foto REPUVE obligatoria", Toast.LENGTH_LONG).show()
            return
        }

        // ✅ VALIDAR QUE NO EXISTA YA EN BD
        if (vehiculoActual?.IdPaso3LogVehiculo==0 && vehiculoActual?.TieneFoto == true) {
            Toast.makeText(this, "Este vehículo ya tiene foto REPUVE registrada", Toast.LENGTH_LONG).show()
            return
        }

        mostrarCargaConMensajes()
        lifecycleScope.launch {
            try {
                if (vehiculoActual!=null && vehiculoActual?.IdPaso3LogVehiculo!!.toInt()>0) {
                    ocultarCarga()
                    finish() // Cerrar actividad
                    return@launch
                }

                Toast.makeText(this@Paso3Repuve_Activity, "Guardando foto REPUVE...", Toast.LENGTH_SHORT).show()

         /*       val fotoBase64 =bllUtil?.convertirImagenABase64(evidenciaFile!!)
                if (fotoBase64 != null) {
                    val idPaso3LogVehiculo = dalVehiculo.insertarPaso3LogVehiculo(
                        idVehiculo = vehiculo.Id.toInt(),
                        idUsuarioNube = idUsuarioNubeAlta,
                        fotoBase64 = fotoBase64
                    )

                    ocultarCarga()

                    if (idPaso3LogVehiculo > 0) {
                        Toast.makeText(this@Paso3Repuve_Activity,
                            "✅ Foto REPUVE guardada exitosamente",
                            Toast.LENGTH_LONG).show()

                        // ✅ ACTUALIZAR ESTADO DESPUÉS DE GUARDAR
                        vehiculoActual?.IdPaso3LogVehiculo = idPaso3LogVehiculo
                        vehiculoActual?.IdVehiculo = vehiculoActual!!.Id.toInt()
                        vehiculoActual?.TieneFoto = true

                        // ✅ RECONFIGURAR BOTÓN PARA MODO VER
                        binding.btnEvidencia.text = "👁️ Ver Foto REPUVE"
                        binding.tvEstadoEvidencia.text = "📷"
                        binding.btnGuardarPaso3.alpha = 0.5f
                        binding.tvMensajeInfo.text = "✅ Foto REPUVE guardada - No se puede modificar"

                        // ✅ LIMPIAR VARIABLES DE CAPTURA
                        evidenciaFile = null
                        evidenciaCapturada = false
                    }else {
                        Toast.makeText(this@Paso3Repuve_Activity,
                            "❌ Error guardando foto REPUVE",
                            Toast.LENGTH_LONG).show()
                    }
                } else {
                    ocultarCarga()
                    Toast.makeText(this@Paso3Repuve_Activity,
                        "❌ Error procesando la imagen",
                        Toast.LENGTH_LONG).show()
                }*/

                // ✅ CONFIGURAR URL BASE PARA API
                val urlBase = ParametrosSistema.cfgApp!!.UrlGuardadoArchivos + '/' +
                        ParametrosSistema.cfgApp!!.UrlAPIControllerGuardadoArchivos

// ✅ GENERAR NOMBRE DE ARCHIVO
                val nombreArchivo = "${vehiculoActual?.VIN}_Paso_3_Foto_1.jpg"
// ✅ CONVERTIR IMAGEN A BASE64
                var fotoBase64: String? = bllUtil?.convertirImagenABase64(evidenciaFile!!)

// ✅ VALIDAR SI DEBE GUARDAR EN API O EN BASE DE DATOS
                if (ParametrosSistema.cfgApp != null &&
                    ParametrosSistema.cfgApp!!.ManejaGuardadoArchivosEnBD == false) {
                    // Subir a API Web
                    val resultadoSubida = ApiUploadUtil.subirFoto(
                        urlBase = urlBase,
                        nombreArchivo = nombreArchivo,
                        file = evidenciaFile!!,
                        vin = vehiculoActual!!.VIN,
                        paso = 3,
                        numeroFoto = 1
                    )

                    // Si la subida fue exitosa, NO guardar en BD (setear fotoBase64 a null)
                    if (resultadoSubida.first) {
                        fotoBase64 = null
                        Log.d("Paso3REPUVE", "✅ Foto subida a API exitosamente")
                    } else {
                        Log.e("Paso3REPUVE", "❌ Error subiendo a API: ${resultadoSubida.second}")
                    }
                }

                val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
// ✅ GUARDAR REGISTRO EN BD (con o sin Base64 según configuración)
                val idPaso3LogVehiculo = dalVehiculo.insertarPaso3LogVehiculo(
                    idVehiculo = vehiculo.Id.toInt(),
                    idUsuarioNube = idUsuarioNubeAlta,
                    fotoBase64 = fotoBase64.toString(),
                    nombreArchivo= nombreArchivo,
                    fechaMovimiento = fechaActual
                )
                ocultarCarga()

                if (idPaso3LogVehiculo > 0) {
                    val mensajeGuardado = if (fotoBase64 != null) {
                        "✅ Foto REPUVE guardada exitosamente en Base de Datos"
                    } else {
                        "✅ Foto REPUVE guardada exitosamente en Web API"
                    }

                    Toast.makeText(this@Paso3Repuve_Activity,
                        mensajeGuardado,
                        Toast.LENGTH_LONG).show()

                    // ✅ ACTUALIZAR ESTADO DESPUÉS DE GUARDAR
                    vehiculoActual?.IdPaso3LogVehiculo = idPaso3LogVehiculo
                    vehiculoActual?.IdVehiculo = vehiculoActual!!.Id.toInt()
                    vehiculoActual?.TieneFoto = true
                    vehiculoActual?.NombreArchivoFoto = nombreArchivo

                    // ✅ RECONFIGURAR BOTÓN PARA MODO VER
                    binding.btnEvidencia.text = "👁️ Ver Foto REPUVE"
                    binding.tvEstadoEvidencia.text = "📷"
                   // binding.btnGuardarPaso3.alpha = 0.5f


                    // ✅ LIMPIAR VARIABLES DE CAPTURA
                    evidenciaFile = null
                    evidenciaCapturada = false
                } else {
                    Toast.makeText(this@Paso3Repuve_Activity,
                        "❌ Error guardando foto REPUVE",
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("Paso3REPUVE", "💥 Error guardando Paso 3: ${e.message}")
                Toast.makeText(this@Paso3Repuve_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // ✅ MÉTODOS AUXILIARES (REUTILIZADOS DE PASOS ANTERIORES)
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
                val tempFile = File(getExternalFilesDir(null), "temp_paso3_photo_$timeStamp.jpg")

                tempFile.outputStream().use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()

                return tempFile
            }

            null
        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "Error obteniendo archivo desde URI: ${e.message}")
            null
        }
    }

    private fun comprimirImagen(archivoOriginal: File): File {
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
            val archivoComprimido = File(getExternalFilesDir(null), "compressed_paso3_$timeStamp.jpg")

            val outputStream = FileOutputStream(archivoComprimido)
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()

            bitmap.recycle()
            bitmapRedimensionado.recycle()

            Log.d("Paso3REPUVE", "✅ Imagen comprimida: ${archivoComprimido.length()} bytes")
            archivoComprimido

        } catch (e: Exception) {
            Log.e("Paso3REPUVE", "Error comprimiendo imagen: ${e.message}")
            archivoOriginal
        }
    }

    private fun mostrarCargaConMensajes() {
        loadingContainer.visibility = View.VISIBLE
        binding.btnGuardarPaso3.isEnabled = false
        binding.btnGuardarPaso3.alpha = 0.5f

        val mensajes = arrayOf(
            "Preparando foto..." to "Organizando evidencia",
            "Comprimiendo imagen..." to "Optimizando calidad",
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
        binding.btnGuardarPaso3.isEnabled = true
        binding.btnGuardarPaso3.alpha = 1.0f

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
            tvEstadoEvidencia.text = "❌"
            btnEvidencia.text = "📷 Foto REPUVE"
            btnEvidencia.backgroundTintList = ContextCompat.getColorStateList(this@Paso3Repuve_Activity, android.R.color.holo_orange_dark)

        }

        vehiculoActual = null
        evidenciaFile = null
        evidenciaCapturada = false
        ocultarSecciones()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}