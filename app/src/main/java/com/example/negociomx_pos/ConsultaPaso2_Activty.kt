package com.example.negociomx_pos

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.ConsultaPaso2Item
import com.example.negociomx_pos.DAL.DALPaso2
import com.example.negociomx_pos.adapters.Paso2Adapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConsultaPaso2_Activity : AppCompatActivity() {

    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var recyclerViewRegistros: RecyclerView
    private lateinit var layoutEstadisticas: LinearLayout
    private lateinit var layoutSinResultados: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private lateinit var tvVehiculosUnicos: TextView
    private lateinit var tvTotalFotos: TextView
    private lateinit var tvMensajeSinResultados: TextView

    private lateinit var adapter: Paso2Adapter
    private val dalConsultaPaso2 = DALPaso2()
    private var fechaSeleccionada: String = ""
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_paso2_activty)

        inicializarVistas()
        configurarRecyclerView()
        configurarEventos()
        establecerFechaActual()
        ejecutarConsultaAutomatica()
    }

    private fun inicializarVistas() {
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        recyclerViewRegistros = findViewById(R.id.recyclerViewRegistros)
        layoutEstadisticas = findViewById(R.id.layoutEstadisticas)
        layoutSinResultados = findViewById(R.id.layoutSinResultados)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)
        tvVehiculosUnicos = findViewById(R.id.tvVehiculosUnicos)
        tvTotalFotos = findViewById(R.id.tvTotalFotos)
        tvMensajeSinResultados = findViewById(R.id.tvMensajeSinResultados)
    }

    private fun configurarRecyclerView() {
        adapter = Paso2Adapter(emptyList()) { registro ->
            // Manejar clic en item
            mostrarDetalleRegistro(registro)
        }
        recyclerViewRegistros.layoutManager = LinearLayoutManager(this)
        recyclerViewRegistros.adapter = adapter
    }

    private fun configurarEventos() {
        // Selector de fecha
        tvFechaSeleccionada.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Botón consultar
        findViewById<android.widget.Button>(R.id.btnConsultar).setOnClickListener {
            if (fechaSeleccionada.isNotEmpty()) {
                ejecutarConsulta()
            } else {
                Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun establecerFechaActual() {
        val fechaActual = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fechaSeleccionada = formatoFecha.format(fechaActual.time)
        tvFechaSeleccionada.text = formatoMostrar.format(fechaActual.time)
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()

        // Si ya hay una fecha seleccionada, usar esa como inicial
        if (fechaSeleccionada.isNotEmpty()) {
            try {
                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fecha = formatoFecha.parse(fechaSeleccionada)
                if (fecha != null) {
                    calendario.time = fecha
                }
            } catch (e: Exception) {
                Log.e("ConsultaPaso2", "Error parseando fecha: ${e.message}")
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaSeleccionadaCalendar = Calendar.getInstance()
                fechaSeleccionadaCalendar.set(year, month, dayOfMonth)

                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                fechaSeleccionada = formatoFecha.format(fechaSeleccionadaCalendar.time)
                tvFechaSeleccionada.text = formatoMostrar.format(fechaSeleccionadaCalendar.time)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun ejecutarConsultaAutomatica() {
        if (fechaSeleccionada.isNotEmpty()) {
            ejecutarConsulta()
        }
    }

    private fun ejecutarConsulta() {
        lifecycleScope.launch {
            try {
                mostrarCarga()
                ocultarResultados()

                Log.d("ConsultaPaso2", "🔍 Ejecutando consulta para fecha: $fechaSeleccionada")

                // Consultar registros
                val registros = dalConsultaPaso2.consultarPaso2PorFecha(fechaSeleccionada)

                // Consultar estadísticas
                val estadisticas = dalConsultaPaso2.obtenerEstadisticasPaso2PorFecha(fechaSeleccionada)

                ocultarCarga()

                if (registros.isNotEmpty()) {
                    mostrarResultados(registros, estadisticas)
                    Toast.makeText(this@ConsultaPaso2_Activity,
                        "✅ Se encontraron ${registros.size} registros",
                        Toast.LENGTH_SHORT).show()
                } else {
                    mostrarSinResultados()
                    Toast.makeText(this@ConsultaPaso2_Activity,
                        "No se encontraron registros para la fecha seleccionada",
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("ConsultaPaso2", "💥 Error en consulta: ${e.message}")
                Toast.makeText(this@ConsultaPaso2_Activity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG).show()
                mostrarSinResultados()
            }
        }
    }

    private fun mostrarCarga() {
        loadingContainer.visibility = View.VISIBLE

        // Mensajes dinámicos para la carga
        val mensajes = arrayOf(
            "Consultando registros..." to "Buscando datos de Paso 2",
            "Filtrando por fecha..." to "Aplicando filtros de búsqueda",
            "Organizando resultados..." to "Preparando información",
            "Calculando estadísticas..." to "Procesando datos encontrados"
        )

        var mensajeIndex = 0
        loadingHandler = Handler(Looper.getMainLooper())

        loadingRunnable = object : Runnable {
            override fun run() {
                if (mensajeIndex < mensajes.size && loadingContainer.visibility == View.VISIBLE) {
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
        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun mostrarResultados(registros: List<ConsultaPaso2Item>, estadisticas: Map<String, Int>) {
        // Actualizar adapter
        adapter.actualizarRegistros(registros)

        // Mostrar estadísticas
        tvVehiculosUnicos.text = "🚗 ${estadisticas["VehiculosUnicos"] ?: 0} vehículos"
        tvTotalFotos.text = "📸 ${estadisticas["TotalFotos"] ?: 0} fotos"

        // Mostrar vistas
        recyclerViewRegistros.visibility = View.VISIBLE
        layoutEstadisticas.visibility = View.VISIBLE
        layoutSinResultados.visibility = View.GONE
    }

    private fun mostrarSinResultados() {
        val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaMostrar = try {
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formatoFecha.parse(fechaSeleccionada)
            if (fecha != null) formatoMostrar.format(fecha) else fechaSeleccionada
        } catch (e: Exception) {
            fechaSeleccionada
        }

        tvMensajeSinResultados.text = "para la fecha $fechaMostrar"

        recyclerViewRegistros.visibility = View.GONE
        layoutEstadisticas.visibility = View.GONE
        layoutSinResultados.visibility = View.VISIBLE
    }

    private fun ocultarResultados() {
        recyclerViewRegistros.visibility = View.GONE
        layoutEstadisticas.visibility = View.GONE
        layoutSinResultados.visibility = View.GONE
    }

    private fun mostrarDetalleRegistro(registro: ConsultaPaso2Item) {
        val dialog = AlertDialog.Builder(this)

        // Crear el contenido del diálogo
        val mensaje = StringBuilder()
        mensaje.append("🚗 INFORMACIÓN DEL VEHÍCULO\n\n")
        mensaje.append("VIN: ${registro.VIN}\n")
        mensaje.append("BL: ${registro.BL}\n")
        mensaje.append("Marca: ${registro.Marca}\n")
        mensaje.append("Modelo: ${registro.Modelo}\n")
        mensaje.append("Año: ${registro.Anio}\n")
        mensaje.append("Motor: ${registro.NumeroMotor}\n")
        mensaje.append("Color Exterior: ${registro.ColorExterior}\n")
        mensaje.append("Color Interior: ${registro.ColorInterior}\n\n")

        mensaje.append("📸 INFORMACIÓN DE FOTOS\n\n")
        mensaje.append("Total de fotos: ${registro.CantidadFotos}\n\n")

        if (registro.FechaAltaFoto1.isNotEmpty()) {
            mensaje.append("📷 Foto 1: ${registro.FechaAltaFoto1.substring(0, 19)}\n")
        }
        if (registro.FechaAltaFoto2.isNotEmpty()) {
            mensaje.append("📷 Foto 2: ${registro.FechaAltaFoto2.substring(0, 19)}\n")
        }
        if (registro.FechaAltaFoto3.isNotEmpty()) {
            mensaje.append("📷 Foto 3: ${registro.FechaAltaFoto3.substring(0, 19)}\n")
        }
        if (registro.FechaAltaFoto4.isNotEmpty()) {
            mensaje.append("📷 Foto 4: ${registro.FechaAltaFoto4.substring(0, 19)}\n")
        }

        if (registro.CantidadFotos == 0) {
            mensaje.append("❌ Sin fotos registradas\n")
        }

        dialog.setTitle("📋 Detalle del Registro Paso 2")
        dialog.setMessage(mensaje.toString())
        dialog.setPositiveButton("Cerrar") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        // Opcional: Agregar botón para ver fotos (si implementas visualización de fotos)
        if (registro.CantidadFotos > 0) {
            dialog.setNeutralButton("Ver Fotos") { _, _ ->
                // Aquí puedes implementar la visualización de fotos si lo deseas
                Toast.makeText(this, "Función de ver fotos - Por implementar", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}
