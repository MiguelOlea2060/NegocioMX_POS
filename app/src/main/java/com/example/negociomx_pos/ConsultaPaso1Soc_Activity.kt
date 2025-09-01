package com.example.negociomx_pos

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.DAL.DALPaso1SOC
import com.example.negociomx_pos.adapters.Paso1SOCAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConsultaPaso1Soc_Activity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var btnConsultar: Button
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var recyclerViewRegistros: RecyclerView
    private lateinit var layoutSinResultados: LinearLayout
    private lateinit var layoutEstadisticas: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView

    // Estadísticas
    private lateinit var tvTotalRegistros: TextView
    private lateinit var tvVehiculosUnicos: TextView
    private lateinit var tvTotalFotos: TextView

    private lateinit var dalConsultaSOC: DALPaso1SOC
    private lateinit var adapter: Paso1SOCAdapter
    private var fechaSeleccionada: String = ""
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    // NUEVA VARIABLE: Para controlar visibilidad del calendario
    private var calendarioVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_paso1_soc)

        inicializarComponentes()
        configurarEventos()
        configurarRecyclerView()

        establecerFechaActual()
        realizarConsultaInicial()

        // AGREGAR ESTA LÍNEA:
        Toast.makeText(this, "Estado inicial - calendarioVisible: $calendarioVisible", Toast.LENGTH_SHORT).show()
    }

    private fun inicializarComponentes() {
        calendarView = findViewById(R.id.calendarView)
        Toast.makeText(this, "CalendarView encontrado: ${calendarView != null}", Toast.LENGTH_SHORT).show()
        btnConsultar = findViewById(R.id.btnConsultar)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        recyclerViewRegistros = findViewById(R.id.recyclerViewRegistros)
        layoutSinResultados = findViewById(R.id.layoutSinResultados)
        layoutEstadisticas = findViewById(R.id.layoutEstadisticas)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)

        // Estadísticas
        tvTotalRegistros = findViewById(R.id.tvTotalRegistros)
        tvVehiculosUnicos = findViewById(R.id.tvVehiculosUnicos)
        tvTotalFotos = findViewById(R.id.tvTotalFotos)

        dalConsultaSOC = DALPaso1SOC()
    }

    private fun configurarEventos() {
        // NUEVO: Evento para mostrar/ocultar calendario al hacer clic en la fecha
        tvFechaSeleccionada.setOnClickListener {
            Toast.makeText(this, "Click detectado", Toast.LENGTH_SHORT).show()
            toggleCalendario()
        }

        // Evento del calendario (MODIFICADO: ahora oculta el calendario al seleccionar)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            fechaSeleccionada = formatoFecha.format(calendar.time)

            val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvFechaSeleccionada.text = formatoMostrar.format(calendar.time)

            // NUEVO: Ocultar calendario automáticamente después de seleccionar fecha
            ocultarCalendario()

            // NUEVO: Consultar automáticamente al seleccionar fecha
            consultarRegistrosPorFecha(fechaSeleccionada)
        }

        // Botón consultar (SIN CAMBIOS)
        btnConsultar.setOnClickListener {
            if (fechaSeleccionada.isNotEmpty()) {
                // NUEVO: Ocultar calendario al consultar
                ocultarCalendario()
                consultarRegistrosPorFecha(fechaSeleccionada)
            } else {
                Toast.makeText(this, "Seleccione una fecha", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // NUEVA FUNCIÓN: Alternar visibilidad del calendario
    private fun toggleCalendario() {
        Toast.makeText(this, "toggleCalendario llamado - Visible actual: $calendarioVisible", Toast.LENGTH_LONG).show()

        if (calendarioVisible) {
            Toast.makeText(this, "Intentando ocultar calendario", Toast.LENGTH_SHORT).show()
            ocultarCalendario()
        } else {
            Toast.makeText(this, "Intentando mostrar calendario", Toast.LENGTH_SHORT).show()
            mostrarCalendario()
        }
    }

    // NUEVA FUNCIÓN: Mostrar calendario
    private fun mostrarCalendario() {
        Toast.makeText(this, "mostrarCalendario() ejecutándose", Toast.LENGTH_SHORT).show()

        calendarView.visibility = View.VISIBLE
        calendarioVisible = true

        Toast.makeText(this, "Calendar visibility establecida a VISIBLE", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, "calendarioVisible = $calendarioVisible", Toast.LENGTH_SHORT).show()

        val fechaActual = tvFechaSeleccionada.text.toString()
        if (!fechaActual.contains("(Toque para ocultar)")) {
            tvFechaSeleccionada.text = "$fechaActual (Toque para ocultar)"
        }
    }

    // NUEVA FUNCIÓN: Ocultar calendario
    private fun ocultarCalendario() {
        calendarView.visibility = View.GONE
        calendarioVisible = false

        // Restaurar texto del campo fecha
        val fechaTexto = tvFechaSeleccionada.text.toString()
        if (fechaTexto.contains("(Seleccione fecha)")) {
            tvFechaSeleccionada.text = fechaTexto.replace(" (Seleccione fecha)", "")
        }
    }

    private fun configurarRecyclerView() {
        adapter = Paso1SOCAdapter(emptyList()) { registro ->
            // Manejar clic en item
            mostrarDetalleRegistro(registro)
        }

        recyclerViewRegistros.layoutManager = LinearLayoutManager(this)
        recyclerViewRegistros.adapter = adapter
    }

    private fun establecerFechaActual() {
        val fechaActual = Calendar.getInstance()
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fechaSeleccionada = formatoFecha.format(fechaActual.time)
        tvFechaSeleccionada.text = formatoMostrar.format(fechaActual.time)

        // Establecer fecha en el calendario
        calendarView.date = fechaActual.timeInMillis
    }

    private fun realizarConsultaInicial() {
        // Realizar consulta automática para el día actual
        consultarRegistrosPorFecha(fechaSeleccionada)
    }

    private fun consultarRegistrosPorFecha(fecha: String) {
        lifecycleScope.launch {
            try {
                mostrarCargando()

                // Consultar registros
                val registros = dalConsultaSOC.consultarPaso1SOCPorFecha(fecha)

                // Consultar estadísticas
                val estadisticas = dalConsultaSOC.obtenerEstadisticasPorFecha(fecha)

                ocultarCargando()

                if (registros.isNotEmpty()) {
                    mostrarResultados(registros, estadisticas)
                } else {
                    mostrarSinResultados()
                }

            } catch (e: Exception) {
                ocultarCargando()
                Toast.makeText(this@ConsultaPaso1Soc_Activity,
                    "Error consultando registros: ${e.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarResultados(registros: List<Paso1SOCItem>, estadisticas: Map<String, Int>) {
        // Ocultar mensaje sin resultados
        layoutSinResultados.visibility = View.GONE

        // Mostrar estadísticas
        layoutEstadisticas.visibility = View.VISIBLE
        tvTotalRegistros.text = "📋 ${estadisticas["TotalRegistros"] ?: 0} registros"
        tvVehiculosUnicos.text = "🚗 ${estadisticas["VehiculosUnicos"] ?: 0} vehículos"
        tvTotalFotos.text = "📸 ${estadisticas["TotalFotos"] ?: 0} fotos"

        // Mostrar lista
        recyclerViewRegistros.visibility = View.VISIBLE
        adapter.actualizarRegistros(registros)

        Toast.makeText(this,
            "✅ Se encontraron ${registros.size} registros",
            Toast.LENGTH_SHORT).show()
    }

    private fun mostrarSinResultados() {
        // Ocultar estadísticas y lista
        layoutEstadisticas.visibility = View.GONE
        recyclerViewRegistros.visibility = View.GONE

        // Mostrar mensaje sin resultados
        layoutSinResultados.visibility = View.VISIBLE

        val mensajeSinResultados = findViewById<TextView>(R.id.tvMensajeSinResultados)
        val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaMostrar = try {
            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fecha = formatoFecha.parse(fechaSeleccionada)
            formatoMostrar.format(fecha!!)
        } catch (e: Exception) {
            fechaSeleccionada
        }

        mensajeSinResultados.text = "para la fecha $fechaMostrar"
    }

    private fun mostrarCargando() {
        loadingContainer.visibility = View.VISIBLE
        btnConsultar.isEnabled = false
        btnConsultar.alpha = 0.5f

        // Mensajes dinámicos para el loading
        val mensajes = arrayOf(
            "Consultando registros..." to "Filtrando por fecha seleccionada",
            "Procesando datos..." to "Organizando información por vehículo",
            "Calculando estadísticas..." to "Obteniendo totales del día",
            "Finalizando..." to "Preparando resultados"
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

    private fun ocultarCargando() {
        loadingContainer.visibility = View.GONE
        btnConsultar.isEnabled = true
        btnConsultar.alpha = 1.0f

        // Limpiar handlers
        loadingHandler?.removeCallbacks(loadingRunnable!!)
        loadingHandler = null
        loadingRunnable = null
    }

    private fun mostrarDetalleRegistro(registro: Paso1SOCItem) {
        val mensaje = """
            🚗 DETALLE DEL REGISTRO
            
            VIN: ${registro.VIN}
            BL: ${registro.BL}
            Vehículo: ${registro.Marca} ${registro.Modelo} ${registro.Anio}
            Motor: ${registro.NumeroMotor}
            
            📊 DATOS SOC:
            Odómetro: ${registro.Odometro} km
            Batería: ${registro.Bateria}%
            Modo Transporte: ${if (registro.ModoTransporte) "Sí" else "No"}
            Requiere Recarga: ${if (registro.RequiereRecarga) "Sí" else "No"}
            
            📸 Fotos: ${registro.CantidadFotos}
            👤 Usuario: ${registro.UsuarioAlta}
            📅 Fecha: ${registro.FechaAlta}
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Detalle del Registro")
            .setMessage(mensaje)
            .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}
