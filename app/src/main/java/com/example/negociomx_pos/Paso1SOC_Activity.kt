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
import com.example.negociomx_pos.BE.StatusFotoVehiculo
import com.example.negociomx_pos.BE.Vehiculo
import com.example.negociomx_pos.DAL.DALVehiculo
//import com.example.negociomx_pos.Utils.FileUploadUtil
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
import androidx.core.util.rangeTo


class Paso1SOC_Activity : AppCompatActivity() {
    //Paso1
    private lateinit var binding: ActivityPaso1SocBinding
    private val dalVehiculo = DALVehiculo()
    private var vehiculoActual: Vehiculo? = null

    //  private var evidencia1NombreArchivo: String = ""
    //  private var evidencia2NombreArchivo: String = ""
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private lateinit var btnGuardar: Button
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    private var evidencia1File: File? = null
    private var evidencia2File: File? = null
    private var evidencia1Capturada: Boolean = false
    private var evidencia2Capturada: Boolean = false
    private var currentPhotoType: Int = 0 // Para saber qué evidencia estamos capturando
    private var fotoUri: Uri? = null
    private var vehiculo: Vehiculo? = null

    private var idUsuarioNubeAlta: Int =
        ParametrosSistema.usuarioLogueado.Id?.toInt()!!// Reemplaza con el ID del usuario actual
    private var fotosExistentes: Int = 0 // Para controlar cuántas fotos ya existen
    private var status: StatusFotoVehiculo? = null

    //Control de consulta de foto
    private var tieneRegistroSOC: Boolean = false
    private var evidencia3File: File? = null
    private var evidencia4File: File? = null
    private var evidencia3Capturada: Boolean = false
    private var evidencia4Capturada: Boolean = false


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
    private val camaraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                fotoUri?.let { uri ->
                    procesarFoto(uri)
                }
            } else {
                Toast.makeText(this, "Error capturando foto", Toast.LENGTH_SHORT).show()
            }
        }

    // ✅ LAUNCHER PARA PERMISOS
    private val permisoLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
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
        binding.etVIN.requestFocus()

        // Configurando Captura de enter en el QR del VIN
        binding.etVIN.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                verificaVINSuministrado()
                return@setOnKeyListener true
            }
            false
        }
        // Configurando Captura de enter en el QR del VIN
        binding.etOdometro.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                binding.etBateria.selectAll()
                binding.etBateria.requestFocus()
                return@setOnKeyListener true
            }
            false
        }
        // ✅ BOTÓN CONSULTAR VEHÍCULO
        binding.btnConsultarVehiculo.setOnClickListener {
            verificaVINSuministrado()
        }

        // ✅ BOTONES DE EVIDENCIAS
        /*binding.btnEvidencia1.setOnClickListener {
            capturarEvidencia(1)
        }

        binding.btnEvidencia2.setOnClickListener {
            capturarEvidencia(2)
        }*/
        // ✅ BOTONES DE EVIDENCIAS
        binding.btnEvidencia1.setOnClickListener {
            if (status?.FotosPosicion1!! > 0) {
                verFotoExistente(1)
            } else {
                capturarEvidencia(1)
            }
        }

        binding.btnEvidencia2.setOnClickListener {
            if (status?.FotosPosicion2!! > 0) {
                verFotoExistente(2)
            } else {
                capturarEvidencia(2)
            }
        }

        // ✅ BOTÓN GUARDAR SOC
        binding.btnGuardarSOC.setOnClickListener {
            guardarSOC()
        }
        // Cambiar de:
        //   btnGuardar = findViewById(R.id.btnGuardarSOC)

// A: (solo si tienes esta línea, si no, ignórala)
// Ya no necesitas esta línea porque usas binding.btnGuardarSOC

        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)

        // ✅ BOTONES DE EVIDENCIAS ADICIONALES
        binding.btnEvidencia3.setOnClickListener {
            if (status?.FotosPosicion3!! > 0) {
                verFotoExistente(3)
            } else {
                capturarEvidencia(3)
            }
        }

        binding.btnEvidencia4.setOnClickListener {
            if (status?.FotosPosicion4!! > 0) {
                verFotoExistente(4)
            } else {
                capturarEvidencia(4)
            }
        }
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
                mostrarCargaConsulta()

                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Consultando vehículo...",
                    Toast.LENGTH_SHORT
                ).show()

                /*   vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin) //Sustitucion para ver las foto y evitar modificar datos ya existentes
                  if (vehiculo != null) {
                      vehiculoActual = vehiculo

                      // ✅ CONSULTAR FOTOS EXISTENTES
                      status = dalVehiculo.consultarFotosExistentes(vehiculo?.Id?.toInt()!!)

                      mostrarInformacionVehiculo(vehiculo!!)
                      mostrarSeccionesSOC()

                      // ✅ MOSTRAR INFORMACIÓN SOBRE FOTOS EXISTENTES
                      if(status!=null) {
                          fotosExistentes=status?.FotosPosicion1!!+status?.FotosPosicion2!!+
                                  status?.FotosPosicion3!!+status?.FotosPosicion4!!

                          if (fotosExistentes > 0) {
                              Toast.makeText(
                                  this@Paso1SOC_Activity,
                                  "✅ Vehículo encontrado. Ya tiene $fotosExistentes foto(s) registrada(s)",
                                  Toast.LENGTH_LONG
                              ).show()
                          } else {
                              Toast.makeText(
                                  this@Paso1SOC_Activity,
                                  "✅ Vehículo encontrado. Sin fotos previas",
                                  Toast.LENGTH_SHORT
                              ).show()
                          }
                      }
                      ocultarCargaConsulta()
                  } else {
                      ocultarSeccionesSOC()
                      Toast.makeText(this@Paso1SOC_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                      ocultarCargaConsulta()
                  }*/

                vehiculo = dalVehiculo.consultarVehiculoPorVIN(vin)
                if (vehiculo != null) {
                    vehiculoActual = vehiculo

                    // ✅ CONSULTAR DATOS SOC EXISTENTES
                    val datosSOCExistentes =
                        dalVehiculo.consultarDatosSOCExistentes(vehiculo?.Id?.toInt()!!)

                    // ✅ CONSULTAR FOTOS EXISTENTES
                    status = dalVehiculo.consultarFotosExistentes(vehiculo?.Id?.toInt()!!)

                    mostrarInformacionVehiculo(vehiculo!!)

                    // ✅ MOSTRAR DATOS SOC SI EXISTEN
                    if (datosSOCExistentes != null) {
                        tieneRegistroSOC = true
                        mostrarDatosSOCExistentes(datosSOCExistentes)
                    } else {
                        binding.apply {
                            etOdometro.isEnabled = true
                            etBateria.isEnabled = true
                            cbModoTransporte.isEnabled = true
                            cbRequiereRecarga.isEnabled = true

                            etOdometro.requestFocus()
                            etOdometro.selectAll()
                        }
                    }


                    mostrarSeccionesSOC()

                    // ✅ CONFIGURAR BOTONES DE FOTOS SEGÚN ESTADO
                    configurarBotonesSegunFotos()

                    if (status != null) {
                        fotosExistentes = status?.FotosPosicion1!! + status?.FotosPosicion2!! +
                                status?.FotosPosicion3!! + status?.FotosPosicion4!!

                        if (fotosExistentes > 0) {
                            Toast.makeText(
                                this@Paso1SOC_Activity,
                                "✅ Vehículo encontrado. Ya tiene $fotosExistentes foto(s) registrada(s)",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@Paso1SOC_Activity,
                                "✅ Vehículo encontrado. Sin fotos previas",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
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

    private fun verFotoExistente(posicion: Int) {
        val vehiculo = vehiculoActual
        if (vehiculo == null) {
            Toast.makeText(this, "Error: No hay vehículo seleccionado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Toast.makeText(this@Paso1SOC_Activity, "Cargando foto...", Toast.LENGTH_SHORT)
                    .show()

                val fotoBase64 = dalVehiculo.obtenerFotoBase64(vehiculo.Id.toInt(), posicion)

                if (fotoBase64 != null && fotoBase64.isNotEmpty()) {
                    mostrarDialogoFoto(fotoBase64, posicion)
                } else {
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
    /* private fun capturarEvidencia(numeroEvidencia: Int) {
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
     }*/


    private fun capturarEvidencia(numeroEvidencia: Int) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA PARA POSICIONES 1 Y 2
        if (numeroEvidencia == 1 && evidencia1Capturada) {
            Toast.makeText(
                this,
                "Ya tiene evidencia 1 capturada. Presione Guardar para confirmar.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (numeroEvidencia == 2 && evidencia2Capturada) {
            Toast.makeText(
                this,
                "Ya tiene evidencia 2 capturada. Presione Guardar para confirmar.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // ✅ VALIDAR SI YA TIENE FOTO CAPTURADA PARA POSICIONES 3 Y 4
        if (numeroEvidencia == 3 && evidencia3Capturada) {
            Toast.makeText(
                this,
                "Ya tiene evidencia 3 capturada. Presione Guardar para confirmar.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (numeroEvidencia == 4 && evidencia4Capturada) {
            Toast.makeText(
                this,
                "Ya tiene evidencia 4 capturada. Presione Guardar para confirmar.",
                Toast.LENGTH_SHORT
            ).show()
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

    private fun convertirImagenABase64(archivo: File): String? {
        return try {
            val bytes = archivo.readBytes()
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("Paso1SOC", "Error convirtiendo imagen a Base64: ${e.message}")
            null
        }
    }


    /*  private fun procesarFoto(uri: Uri) {
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
      }*/

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

            // ✅ GUARDAR REFERENCIA DEL ARCHIVO SEGÚN LA EVIDENCIA
            when (currentPhotoType) {
                1 -> {
                    evidencia1File = archivoFinal
                    evidencia1Capturada = true
                    binding.tvEstadoEvidencia1.text = "📷"
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "✅ Evidencia 1 capturada (sin guardar)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                2 -> {
                    evidencia2File = archivoFinal
                    evidencia2Capturada = true
                    binding.tvEstadoEvidencia2.text = "📷"
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "✅ Evidencia 2 capturada (sin guardar)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                3 -> {
                    evidencia3File = archivoFinal
                    evidencia3Capturada = true
                    binding.tvEstadoEvidencia3.text = "📷"
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "✅ Evidencia 3 capturada (sin guardar)",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                4 -> {
                    evidencia4File = archivoFinal
                    evidencia4Capturada = true
                    binding.tvEstadoEvidencia4.text = "📷"
                    Toast.makeText(
                        this@Paso1SOC_Activity,
                        "✅ Evidencia 4 capturada (sin guardar)",
                        Toast.LENGTH_SHORT
                    ).show()
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

        // Mensajes específicos para consulta de vehículo
        /*        val mensajes = arrayOf(
                    "Buscando vehículo..." to "Consultando base de datos",
                    "Verificando VIN..." to "Validando información",
                    "Cargando datos..." to "Obteniendo detalles del vehículo",
                    "Consultando fotos..." to "Verificando imágenes existentes"
                )*/

        var mensajeIndex = 0
        //loadingHandler = Handler(Looper.getMainLooper())

        /*loadingRunnable = object : Runnable {
            override fun run() {
                if (mensajeIndex < mensajes.size) {
                    tvLoadingText.text = mensajes[mensajeIndex].first
                    tvLoadingSubtext.text = mensajes[mensajeIndex].second
                    mensajeIndex++
                    loadingHandler?.postDelayed(this, 2000) // Cambiar cada 2 segundos (más rápido)
                }
            }
        }*/
        //loadingRunnable?.let { loadingHandler?.post(it) }
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

        val odometroText = binding.etOdometro.text.toString().trim()
        val bateriaText = binding.etBateria.text.toString().trim()

        if (odometroText.isEmpty() || bateriaText.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos obligatorios", Toast.LENGTH_SHORT)
                .show()

            binding.etOdometro.selectAll()
            binding.etOdometro.requestFocus()
            return
        }

        val odometro = odometroText.toIntOrNull() ?: 0
        val bateria = bateriaText.toIntOrNull() ?: 0

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
                Toast.makeText(
                    this@Paso1SOC_Activity,
                    "Guardando SOC y fotos...",
                    Toast.LENGTH_SHORT
                ).show()

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

                    // Solo permitir guardar nuevas fotos si no tiene registro SOC previo
                    if (!tieneRegistroSOC) {
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
                            consecutivo++
                        }
                    } else {
                        // Si ya tiene registro SOC, solo permitir fotos 3 y 4
                        if (evidencia3Capturada && evidencia3File != null) {
                            val fotoBase64 = convertirImagenABase64(evidencia3File!!)
                            exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                                idPaso1LogVehiculo = idPaso1LogVehiculo,
                                idEntidadArchivoFoto = null,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                consecutivo = consecutivo,
                                posicion = 3,
                                fotoBase64 = fotoBase64
                            )
                            consecutivo++
                        }

                        if (evidencia4Capturada && evidencia4File != null) {
                            val fotoBase64 = convertirImagenABase64(evidencia4File!!)
                            exitoFotos = exitoFotos && dalVehiculo.insertarPaso1LogVehiculoFotos(
                                idPaso1LogVehiculo = idPaso1LogVehiculo,
                                idEntidadArchivoFoto = null,
                                idUsuarioNubeAlta = idUsuarioNubeAlta,
                                consecutivo = consecutivo,
                                posicion = 4,
                                fotoBase64 = fotoBase64
                            )
                        }
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
         evidencia1File = null
         evidencia2File = null
         evidencia1Capturada = false
         evidencia2Capturada = false
         fotosExistentes = 0
         ocultarSeccionesSOC()
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
        evidencia3File = null
        evidencia4File = null
        evidencia1Capturada = false
        evidencia2Capturada = false
        evidencia3Capturada = false
        evidencia4Capturada = false
        fotosExistentes = 0
        tieneRegistroSOC = false
        status = null
        ocultarSeccionesSOC()
    }

    //funcion para inahabilitar botones de fotos
    private fun configurarBotonesSegunFotos() {
        if (status == null) return

        // Configurar botón evidencia 1
        if (status?.FotosPosicion1!! > 0) {
            binding.btnEvidencia1.text = "👁️ Ver Foto 1"
            binding.btnEvidencia1.isEnabled = true
            binding.tvEstadoEvidencia1.text = "📷"
        } else {
            binding.btnEvidencia1.text = "📷 Foto 1"
            binding.btnEvidencia1.isEnabled = true
            binding.tvEstadoEvidencia1.text = "❌"
        }

        // Configurar botón evidencia 2
        if (status?.FotosPosicion2!! > 0) {
            binding.btnEvidencia2.text = "👁️ Ver Foto 2"
            binding.btnEvidencia2.isEnabled = true
            binding.tvEstadoEvidencia2.text = "📷"
        } else {
            binding.btnEvidencia2.text = "📷 Foto 2"
            binding.btnEvidencia2.isEnabled = true
            binding.tvEstadoEvidencia2.text = "❌"
        }

        // Si ya hay 2 fotos, mostrar botones para fotos 3 y 4
        if (status?.FotosPosicion1!! > 0 && status?.FotosPosicion2!! > 0) {
            mostrarBotonesEvidenciasAdicionales()
        }
    }


    private fun mostrarBotonesEvidenciasAdicionales() {
        // Mostrar layouts de evidencias 3 y 4
        binding.layoutEvidencia3.visibility = View.VISIBLE
        binding.layoutEvidencia4.visibility = View.VISIBLE

        // Configurar botón evidencia 3
        if (status?.FotosPosicion3!! > 0) {
            binding.btnEvidencia3.text = "👁️ Ver Foto 3"
            binding.btnEvidencia3.isEnabled = true
            binding.tvEstadoEvidencia3.text = "📷"
        } else {
            binding.btnEvidencia3.text = "📷 Foto 3"
            binding.btnEvidencia3.isEnabled = true
            binding.tvEstadoEvidencia3.text = "❌"
        }

        // Configurar botón evidencia 4
        if (status?.FotosPosicion4!! > 0) {
            binding.btnEvidencia4.text = "👁️ Ver Foto 4"
            binding.btnEvidencia4.isEnabled = true
            binding.tvEstadoEvidencia4.text = "📷"
        } else {
            binding.btnEvidencia4.text = "📷 Foto 4"
            binding.btnEvidencia4.isEnabled = true
            binding.tvEstadoEvidencia4.text = "❌"
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }

}
