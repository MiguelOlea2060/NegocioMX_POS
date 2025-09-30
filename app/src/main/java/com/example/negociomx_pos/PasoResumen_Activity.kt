package com.example.negociomx_pos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.negociomx_pos.BE.VehiculoResumen
import com.example.negociomx_pos.DAL.DALVehiculo
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

class PasoResumen_Activity : AppCompatActivity() {

    private lateinit var etVIN: EditText
    private lateinit var btnBuscar: Button
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView

    private lateinit var layoutDatosVehiculo: LinearLayout
    private lateinit var layoutSOC: LinearLayout
    private lateinit var layoutAccesorios: LinearLayout
    private lateinit var layoutRepuve: LinearLayout

    private lateinit var tvMBL: TextView
    private lateinit var tvColorExterior: TextView
    private lateinit var tvColorInterior: TextView
    private lateinit var tvCombustible: TextView
    private lateinit var tvTiempoRegistrado: TextView

    private  lateinit var tvSocEstado:  TextView
    private lateinit var tvFechaSOC: TextView
    private lateinit var tvOdometro: TextView
    private lateinit var tvSOC: TextView

    private lateinit var tvAccesoriosEstado: TextView
    private lateinit var tvFechaAccesorios: TextView

    private lateinit var tvRepuveEstado: TextView
    private lateinit var tvFechaRepuve: TextView

    private val dalVehiculo = DALVehiculo()
    private var resumenActual: VehiculoResumen? = null

    // Launcher para escáner
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            etVIN.setText(result.contents)
            consultarResumen(result.contents)
        }
    }

    // Launcher para permisos
    private val permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            iniciarEscaneo()
        } else {
            Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paso_resumen)

        inicializarComponentes()
        configurarEventos()
        verificarPermisos()
    }

    private fun inicializarComponentes() {
        etVIN = findViewById(R.id.etVIN)
        btnBuscar = findViewById(R.id.btnBuscar)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)

        tvSocEstado = findViewById(R.id.tvSocEstado)
        layoutDatosVehiculo = findViewById(R.id.layoutDatosVehiculo)
        layoutSOC = findViewById(R.id.layoutSOC)
        layoutAccesorios = findViewById(R.id.layoutAccesorios)
        layoutRepuve = findViewById(R.id.layoutRepuve)

        tvMBL = findViewById(R.id.tvMBL)
        tvColorExterior = findViewById(R.id.tvColorExterior)
        tvColorInterior = findViewById(R.id.tvColorInterior)
        tvCombustible = findViewById(R.id.tvCombustible)
        tvTiempoRegistrado = findViewById(R.id.tvTiempoRegistrado)

        tvFechaSOC = findViewById(R.id.tvFechaSoc)
        tvOdometro = findViewById(R.id.tvOdometro)
        tvSOC = findViewById(R.id.tvSOC)

        tvAccesoriosEstado = findViewById(R.id.tvAccesoriosEstado)
        tvFechaAccesorios = findViewById(R.id.tvFechaAccesorios)

        tvRepuveEstado = findViewById(R.id.tvRepuveEstado)
        tvFechaRepuve = findViewById(R.id.tvFechaRepuve)
    }

    private fun configurarEventos() {
        etVIN.requestFocus()

        // Captura de Enter en el campo VIN
        etVIN.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                val vin = etVIN.text.toString().trim()
                if (vin.isNotEmpty() && vin.length >= 17) {
                    consultarResumen(vin)
                } else {
                    Toast.makeText(this, "Ingrese un VIN válido (mínimo 17 caracteres)", Toast.LENGTH_SHORT).show()
                }
                return@setOnKeyListener true
            }
            false
        }

        // Botón buscar
        btnBuscar.setOnClickListener {
            val vin = etVIN.text.toString().trim()
            if (vin.isNotEmpty() && vin.length >= 17) {
                consultarResumen(vin)
            } else {
                Toast.makeText(this, "Ingrese un VIN válido (mínimo 17 caracteres)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun verificarPermisos() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permisoLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun iniciarEscaneo() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
        options.setPrompt("Escanee el código VIN del vehículo")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        barcodeLauncher.launch(options)
    }

    private fun consultarResumen(vin: String) {
        lifecycleScope.launch {
            try {
                Log.d("PasoResumen", "🔍 Consultando resumen para VIN: $vin")
                mostrarCargando()

                val resumen = dalVehiculo.consultarResumenVehiculo(vin)

                ocultarCargando()

                if (resumen != null) {
                    resumenActual = resumen
                    mostrarResumen(resumen)
                    Toast.makeText(this@PasoResumen_Activity, "✅ Vehículo encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    ocultarTodasLasSecciones()
                    Toast.makeText(this@PasoResumen_Activity, "❌ Vehículo no encontrado", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCargando()
                Log.e("PasoResumen", "💥 Error consultando resumen: ${e.message}")
                Toast.makeText(this@PasoResumen_Activity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarResumen(resumen: VehiculoResumen) {
        // Mostrar datos del vehículo
        layoutDatosVehiculo.visibility = View.VISIBLE
        tvMBL.text = "MBL: ${resumen.BL}"
        tvColorExterior.text = "Color Ext.: ${resumen.ColorExterior}"
        tvColorInterior.text = "Color Int.: ${resumen.ColorInterior}"
        tvCombustible.text = "Combustible: ${resumen.TipoCombustible}"

        // Mostrar tiempo registrado
        val tiempoTexto = construirTextoTiempo(resumen)
        tvTiempoRegistrado.text = "Estadia: ${tiempoTexto}"

        // Mostrar SOC solo si existe
        if (resumen.Bateria > 0) {
            layoutSOC.visibility = View.VISIBLE
            tvSocEstado.text = "✅ Sí"
            tvFechaSOC.text = "Fecha: ${resumen.FechaPrimerSOC}"
            tvOdometro.text = "${resumen.Odometro}"
            tvSOC.text = "${resumen.Bateria}"
            tvFechaSOC.visibility = View.VISIBLE
        } else {
            layoutSOC.visibility = View.VISIBLE
            tvSocEstado.text = "❌ No"
            tvFechaSOC.visibility = View.GONE

        }

        // Mostrar Accesorios solo si existen fotos
        if (resumen.TieneAccesorios) {
            layoutAccesorios.visibility = View.VISIBLE
            tvAccesoriosEstado.text = "✅ Sí"
            tvAccesoriosEstado.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            tvFechaAccesorios.text = "Fecha: ${resumen.FechaPrimerAccesorio}"
            tvFechaAccesorios.visibility = View.VISIBLE
        } else {
            layoutAccesorios.visibility = View.VISIBLE
            tvAccesoriosEstado.text = "❌ No"
            tvFechaAccesorios.visibility = View.GONE
        }

        // Mostrar REPUVE solo si existen fotos
        if (resumen.TieneRepuve) {
            layoutRepuve.visibility = View.VISIBLE
            tvRepuveEstado.text = "✅ Sí"
            tvRepuveEstado.setTextColor(resources.getColor(android.R.color.holo_green_dark))
            tvFechaRepuve.text = "Fecha: ${resumen.FechaPrimerRepuve}"
            tvFechaRepuve.visibility = View.VISIBLE
        } else {
            layoutRepuve.visibility = View.VISIBLE
            tvRepuveEstado.text = "❌ No"
            tvFechaRepuve.visibility = View.GONE
        }
    }

    private fun construirTextoTiempo(resumen: VehiculoResumen): String {
        val partes = mutableListOf<String>()

        if (resumen.AniosRegistrado > 0) {
            val texto = if (resumen.AniosRegistrado == 1) "1 año" else "${resumen.AniosRegistrado} años"
            partes.add(texto)
        }

        if (resumen.MesesRegistrado > 0) {
            val texto = if (resumen.MesesRegistrado == 1) "1 mes" else "${resumen.MesesRegistrado} meses"
            partes.add(texto)
        }

        if (resumen.SemanasRegistrado > 0) {
            val texto = if (resumen.SemanasRegistrado == 1) "1 semana" else "${resumen.SemanasRegistrado} semanas"
            partes.add(texto)
        }

        if (resumen.DiasRestantes > 0) {
            val texto = if (resumen.DiasRestantes == 1) "1 día" else "${resumen.DiasRestantes} días"
            partes.add(texto)
        }

        return if (partes.isEmpty()) {
            "Registrado hoy"
        } else {
            partes.joinToString(" y ")
        }
    }

    private fun mostrarCargando() {
        loadingContainer.visibility = View.VISIBLE
        btnBuscar.isEnabled = false
        btnBuscar.alpha = 0.5f
    }

    private fun ocultarCargando() {
        loadingContainer.visibility = View.GONE
        btnBuscar.isEnabled = true
        btnBuscar.alpha = 1.0f
    }

    private fun ocultarTodasLasSecciones() {
        layoutDatosVehiculo.visibility = View.GONE
        layoutSOC.visibility = View.GONE
        layoutAccesorios.visibility = View.GONE
        layoutRepuve.visibility = View.GONE
    }
}