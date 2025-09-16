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
import com.example.negociomx_pos.BE.ConsultaPaso4Item
import com.example.negociomx_pos.DAL.DALPaso4
import com.example.negociomx_pos.adapters.Paso4Adapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ConsultaPaso4Llantas_Activity : AppCompatActivity() {

    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var recyclerViewRegistros: RecyclerView
    private lateinit var layoutEstadisticas: LinearLayout
    private lateinit var layoutSinResultados: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView
    private lateinit var tvVehiculosUnicos: TextView
    private lateinit var tvLlantasVerificadas: TextView
    private lateinit var tvTotalFotos: TextView
    private lateinit var tvMensajeSinResultados: TextView

    private lateinit var adapter: Paso4Adapter
    private val dalConsultaPaso4 = DALPaso4()
    private var fechaSeleccionada: String = ""
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_paso4_llantas)

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
        tvLlantasVerificadas = findViewById(R.id.tvLlantasVeirifcadas)
        tvTotalFotos = findViewById(R.id.tvTotalFotos)
        tvMensajeSinResultados = findViewById(R.id.tvMensajeSinResultados)
    }

    private fun configurarRecyclerView() {
        adapter = Paso4Adapter(emptyList()) { registro ->
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
        tvFechaSeleccionada.isEnabled=false
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
                Log.e("ConsultaPaso4", "Error parseando fecha: ${e.message}")
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
                tvFechaSeleccionada.isEnabled=true

                ejecutarConsulta()
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

                Log.d("ConsultaPaso4", "🔍 Ejecutando consulta para fecha: $fechaSeleccionada")

                // Consultar registros
                val registros = dalConsultaPaso4.consultarPaso4PorFecha(fechaSeleccionada)

                // Consultar estadísticas
                val estadisticas = mutableMapOf<String,Int>()
                //dalConsultaPaso4.obtenerEstadisticasPaso4PorFecha(fechaSeleccionada)
                var totalVehiculos:Int=0
                var totalFotos:Int=0
                var totalVerificaciones:Int=0
                if(registros!=null && registros.count()>0)
                {
                    totalVehiculos=registros.count()
                    registros.forEach {
                        it->
                        totalFotos+= it.LlantasConFoto
                        totalVerificaciones+=it.LlantasVerificadas
                    }
                }
                estadisticas["TotalRegistros"] = totalVerificaciones
                estadisticas["VehiculosUnicos"] = totalVehiculos
                estadisticas["TotalFotos"] = totalFotos

                ocultarCarga()
                if (registros.isNotEmpty()) {
                    mostrarResultados(registros, estadisticas)
                    Toast.makeText(this@ConsultaPaso4Llantas_Activity,
                        "✅ Se encontraron ${registros.size} registros",
                        Toast.LENGTH_SHORT).show()
                } else {
                    mostrarSinResultados()
                    Toast.makeText(this@ConsultaPaso4Llantas_Activity,
                        "No se encontraron registros para la fecha seleccionada",
                        Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                ocultarCarga()
                Log.e("ConsultaPaso4", "💥 Error en consulta: ${e.message}")
                Toast.makeText(this@ConsultaPaso4Llantas_Activity,
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
            "Consultando registros..." to "Buscando datos de Paso 4 Llantas",
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

    private fun mostrarResultados(registros: List<ConsultaPaso4Item>, estadisticas: Map<String, Int>) {
        // Actualizar adapter
        adapter.actualizarRegistros(registros)

        // Mostrar estadísticas
        tvVehiculosUnicos.text = "🚗 ${estadisticas["VehiculosUnicos"] ?: 0} vehículos"
        tvLlantasVerificadas.text = " ${estadisticas["TotalRegistros"] ?: 0} Verificadas"
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

    private fun mostrarDetalleRegistro(registro: ConsultaPaso4Item) {
        val dialog = AlertDialog.Builder(this)

        // Crear el contenido del diálogo
        val mensaje = StringBuilder()
        mensaje.append("🚗 INFORMACIÓN DEL VEHÍCULO\n\n")
        mensaje.append("VIN: ${registro.VIN}\n")
        mensaje.append("BL: ${registro.BL}\n")
        mensaje.append("Marca: ${registro.Marca}\n")
        mensaje.append("Modelo: ${registro.Modelo}\n")
        mensaje.append("Año: ${registro.Anio}\n")
        mensaje.append("Color Exterior: ${registro.ColorExterior}\n")
        mensaje.append("Color Interior: ${registro.ColorInterior}\n\n")

        mensaje.append("🛞 INFORMACIÓN DE LLANTAS\n\n")
        mensaje.append("Total de llantas: ${registro.CantidadLlantas}\n")
        mensaje.append("Llantas verificadas: ${registro.LlantasVerificadas}\n")
//        mensaje.append("Llantas con foto: ${registro.LlantasConFoto}\n\n")

        mensaje.append("📍 DETALLE POR POSICIÓN:\n")
        mensaje.append("🔸 Delantera Izq: ${if (registro.Llanta1Verificada) "✅ Verificada" else "❌ No verificada"} ${if (registro.Llanta1TieneFoto) "📷" else ""}\n")
        mensaje.append("🔸 Delantera Der: ${if (registro.Llanta2Verificada) "✅ Verificada" else "❌ No verificada"} ${if (registro.Llanta2TieneFoto) "📷" else ""}\n")
        mensaje.append("🔸 Trasera Izq: ${if (registro.Llanta3Verificada) "✅ Verificada" else "❌ No verificada"} ${if (registro.Llanta3TieneFoto) "📷" else ""}\n")
        mensaje.append("🔸 Trasera Der: ${if (registro.Llanta4Verificada) "✅ Verificada" else "❌ No verificada"} ${if (registro.Llanta4TieneFoto) "📷" else ""}\n\n")

        mensaje.append("📅 Fecha de registro: ${registro.FechaAlta.substring(0, 19)}\n")

        dialog.setTitle("📋 Detalle del Registro de Llantas")
        dialog.setMessage(mensaje.toString())
        dialog.setPositiveButton("Cerrar") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        loadingHandler?.removeCallbacks(loadingRunnable!!)
    }
}
