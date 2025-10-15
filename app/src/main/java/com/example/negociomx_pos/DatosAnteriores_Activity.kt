package com.example.negociomx_pos

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.RegistroAnteriorSOC
import com.example.negociomx_pos.DAL.DALVehiculo
import com.example.negociomx_pos.Utils.BLLUtils
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.DatosAnterioresAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatosAnteriores_Activity : AppCompatActivity() {

    private lateinit var btnRegresar: ImageView
    private lateinit var tvVIN: TextView
    private lateinit var tvVezActual: TextView
    private lateinit var tvMensajeVacio: TextView
    private lateinit var rvRegistrosAnteriores: RecyclerView
    private lateinit var loadingContainer: LinearLayout
    private lateinit var layoutInfoVehiculo: LinearLayout

    private val dalVehiculo = DALVehiculo()
    private var bllUtil: BLLUtils? = null

    private var vin: String = ""
    private var vezActual: Short = 0
    private var idVehiculo: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_anteriores)

        bllUtil = BLLUtils()

        inicializarVistas()
        obtenerDatosIntent()
        configurarEventos()
        cargarRegistrosAnteriores()
    }

    private fun inicializarVistas() {
        btnRegresar = findViewById(R.id.btnRegresar)
        tvVIN = findViewById(R.id.tvVIN)
        tvVezActual = findViewById(R.id.tvVezActual)
        tvMensajeVacio = findViewById(R.id.tvMensajeVacio)
        rvRegistrosAnteriores = findViewById(R.id.rvRegistrosAnteriores)
        loadingContainer = findViewById(R.id.loadingContainer)
        layoutInfoVehiculo = findViewById(R.id.layoutInfoVehiculo)

        // Configurar RecyclerView
        rvRegistrosAnteriores.layoutManager = LinearLayoutManager(this)
    }

    private fun obtenerDatosIntent() {
        vin = intent.getStringExtra("Vin") ?: ""
        vezActual = intent.getShortExtra("VezActual", 0)
        idVehiculo = intent.getIntExtra("IdVehiculo", 0)

        Log.d("DatosAnteriores", "📊 VIN: $vin, Vez Actual: $vezActual, IdVehiculo: $idVehiculo")
    }

    private fun configurarEventos() {
        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun cargarRegistrosAnteriores() {
        if (vin.isEmpty() || vezActual <= 0) {
            Toast.makeText(this, "❌ Datos inválidos: VIN o Vez no proporcionados", Toast.LENGTH_LONG).show()
            tvMensajeVacio.visibility = View.VISIBLE
            tvMensajeVacio.text = "❌ No hay datos suficientes para consultar el historial"
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("DatosAnteriores", "🔄 Cargando registros anteriores...")
                mostrarCargando(true)

                // ✅ CONSULTAR REGISTROS ANTERIORES USANDO EL NUEVO MeTODO
                val registros = dalVehiculo.consultarRegistrosAnterioresPaso1(vin, vezActual)

                withContext(Dispatchers.Main) {
                    mostrarCargando(false)

                    if (registros.isEmpty()) {
                        // ✅ NO HAY REGISTROS ANTERIORES
                        tvMensajeVacio.visibility = View.VISIBLE
                        tvMensajeVacio.text = "ℹ️ Este es el primer registro del vehículo.\nNo hay historial anterior."
                        rvRegistrosAnteriores.visibility = View.GONE
                        layoutInfoVehiculo.visibility = View.GONE
                    } else {
                        // ✅ HAY REGISTROS - MOSTRAR EN RECYCLERVIEW
                        tvMensajeVacio.visibility = View.GONE
                        rvRegistrosAnteriores.visibility = View.VISIBLE
                        layoutInfoVehiculo.visibility = View.VISIBLE

                        tvVIN.text = "VIN: $vin"
                        tvVezActual.text = "Se encontraron ${registros.size} registro(s) anterior(es))"

                        val adapter = DatosAnterioresAdapter(registros) { registro, posicion ->
                            verFotoHistorica(registro, posicion)
                        }
                        rvRegistrosAnteriores.adapter = adapter

                        Toast.makeText(
                            this@DatosAnteriores_Activity,
                            "✅ Se encontraron ${registros.size} registro(s) anterior(es)",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("DatosAnteriores", "💥 Error cargando registros: ${e.message}")
                withContext(Dispatchers.Main) {
                    mostrarCargando(false)
                    Toast.makeText(
                        this@DatosAnteriores_Activity,
                        "❌ Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun verFotoHistorica(registro: RegistroAnteriorSOC, posicion: Int) {
        lifecycleScope.launch {
            try {
                Toast.makeText(this@DatosAnteriores_Activity, "Cargando foto...", Toast.LENGTH_SHORT).show()

                val nombreArchivo = if (posicion == 1) registro.NombreArchivo1 else registro.NombreArchivo2

                if (nombreArchivo.isEmpty()) {
                    Toast.makeText(this@DatosAnteriores_Activity, "No hay foto disponible", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // ✅ CARGAR FOTO SEGÚN CONFIGURACIÓN
                var fotoBase64: String? = null

                if (ParametrosSistema.cfgApp != null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true) {
                    fotoBase64 = dalVehiculo.obtenerFotoBase64Paso1(registro.IdVehiculo, posicion)
                }

                withContext(Dispatchers.Main) {
                    if (ParametrosSistema.cfgApp != null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == true
                        && fotoBase64 != null && fotoBase64.isNotEmpty()) {
                        mostrarDialogoFoto(fotoBase64, posicion, registro.Vez)
                    } else if (ParametrosSistema.cfgApp != null && ParametrosSistema.cfgApp?.ManejaGuardadoArchivosEnBD == false) {
                        val urlCompletoFoto = ParametrosSistema.cfgApp?.UrlGuardadoArchivos + '/' +
                                ParametrosSistema.cfgApp?.CarpetaGuardadoArchivosNube?.replace("~/", "") +
                                '/' + nombreArchivo
                        mostrarDialogoFotoFromUrl(urlCompletoFoto, posicion, registro.Vez)
                    } else {
                        Toast.makeText(this@DatosAnteriores_Activity, "No se pudo cargar la foto", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("DatosAnteriores", "Error cargando foto: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DatosAnteriores_Activity, "Error cargando foto: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDialogoFoto(fotoBase64: String, posicion: Int, vez: Short) {
        try {
            val decodedBytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

            if (bitmap != null) {
                val dialog = android.app.AlertDialog.Builder(this)
                val imageView = android.widget.ImageView(this)

                imageView.setImageBitmap(bitmap)
                imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                imageView.adjustViewBounds = true

                dialog.setTitle("Evidencia $posicion - Vez $vez")
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
            Log.e("DatosAnteriores", "Error mostrando foto: ${e.message}")
            Toast.makeText(this, "Error mostrando foto", Toast.LENGTH_SHORT).show()
        }
    }

    private fun mostrarDialogoFotoFromUrl(url: String, posicion: Int, vez: Short) {
        lifecycleScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    bllUtil?.mLoad(url)
                }

                if (bitmap != null) {
                    val dialog = android.app.AlertDialog.Builder(this@DatosAnteriores_Activity)
                    val imageView = android.widget.ImageView(this@DatosAnteriores_Activity)

                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                    imageView.adjustViewBounds = true

                    dialog.setTitle("Evidencia $posicion - Vez $vez")
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
                    Toast.makeText(this@DatosAnteriores_Activity, "Error decodificando la imagen", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Log.e("DatosAnteriores", "Error mostrando foto: ${e.message}")
                Toast.makeText(this@DatosAnteriores_Activity, "Error mostrando foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarCargando(mostrar: Boolean) {
        loadingContainer.visibility = if (mostrar) View.VISIBLE else View.GONE
        rvRegistrosAnteriores.visibility = if (mostrar) View.GONE else View.VISIBLE
    }
}