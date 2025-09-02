package com.example.negociomx_pos

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.DAL.DALPaso1SOC
import com.example.negociomx_pos.adapters.Paso1SOCAdapter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ConsultaPaso1Soc_Activity : AppCompatActivity() {

    private lateinit var btnConsultar: Button
    private lateinit var tvFechaSeleccionada: TextView
    private lateinit var recyclerViewRegistros: RecyclerView
    private lateinit var layoutSinResultados: LinearLayout
    private lateinit var layoutEstadisticas: LinearLayout
    private lateinit var loadingContainer: LinearLayout
    private lateinit var tvLoadingText: TextView
    private lateinit var tvLoadingSubtext: TextView

    // Estadísticas
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
        btnConsultar = findViewById(R.id.btnConsultar)
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada)
        recyclerViewRegistros = findViewById(R.id.recyclerViewRegistros)
        layoutSinResultados = findViewById(R.id.layoutSinResultados)
        layoutEstadisticas = findViewById(R.id.layoutEstadisticas)
        loadingContainer = findViewById(R.id.loadingContainer)
        tvLoadingText = findViewById(R.id.tvLoadingText)
        tvLoadingSubtext = findViewById(R.id.tvLoadingSubtext)

        // Estadísticas
        tvVehiculosUnicos = findViewById(R.id.tvVehiculosUnicos)
        tvTotalFotos = findViewById(R.id.tvTotalFotos)

        dalConsultaSOC = DALPaso1SOC()
    }

    private fun configurarEventos() {
        // NUEVO: Evento para mostrar/ocultar calendario al hacer clic en la fecha
        tvFechaSeleccionada.setOnClickListener {
            mostrarCalendario()
        }

        // Botón consultar (SIN CAMBIOS)
        btnConsultar.setOnClickListener {
            Toast.makeText(this, "Mostrando calendario", Toast.LENGTH_SHORT).show()
            consultarRegistrosPorFecha(fechaSeleccionada)
        }
    }

    // NUEVA FUNCIÓN: Mostrar calendario
    private fun mostrarCalendario() {
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.item_calendario)

        val calendario: CalendarView = dialog.findViewById(R.id.dtFechaCal)
        val imgAceptar: ImageView = dialog.findViewById(R.id.imgAceptarCalendario)
        val imgCancelar: ImageView = dialog.findViewById(R.id.imgCancelarCalendario)

        var annio:Int=0
        var mes:Int=0
        var dia:Int=0
        calendario.setOnDateChangeListener{ view,year, month,dayOfMonth->
            annio=year
            mes=month+1
            dia=dayOfMonth
        }
        // Establecer fecha en el calendario
        val fechaActual :Calendar=Calendar.getInstance()
        if (fechaSeleccionada.length ==0)
        {
            fechaSeleccionada = formatoFecha.format(fechaActual.time)
            tvFechaSeleccionada.text = formatoMostrar.format(fechaActual.time)
            calendario.date=fechaActual.timeInMillis
        }
        else
        {
            var pedazos=fechaSeleccionada.split("-")
            annio=pedazos[0].toInt()
            mes=pedazos[1].toInt()
            dia=pedazos[2].toInt()

            calendario.setDate(
                SimpleDateFormat("yyyy-MM-dd").parse(fechaSeleccionada).getTime(),
                true,
                true)
        }

        imgAceptar.setOnClickListener{
            val fechaSel=fechaSeleccionada
            fechaSeleccionada= annio.toString()+"-"+mes.toString()+"-"+dia.toString()

            tvFechaSeleccionada.text=dia.toString()+"/"+mes.toString()+"/"+annio.toString()
            dialog.dismiss()

            if(!fechaSel.equals(fechaSeleccionada))
                consultarRegistrosPorFecha(fechaSeleccionada)
        }
        imgCancelar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        //calendarView.date = fechaActual.timeInMillis
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

                // Calculas estadísticas
                val estadisticas= mutableMapOf<String,Int>()
                var totalVehiculos=0
                var totalFotos=0
                if(registros!=null && registros.count()>0)
                {
                    totalVehiculos=registros.count()
                    registros.forEach { Unit->
                        totalFotos+= Unit.CantidadFotos
                    }
                }
                estadisticas["TotalRegistros"] = 1
                estadisticas["VehiculosUnicos"] = totalVehiculos
                estadisticas["TotalFotos"] = totalFotos

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
            Num. de Motor: ${registro.NumeroMotor}
            
            📊 DATOS SOC:
            Odómetro: ${registro.Odometro} km
            SOC: ${registro.Bateria}%
            Modo Transporte: ${if (registro.ModoTransporte) "Sí" else "No"}
            Requiere Recarga: ${if (registro.RequiereRecarga) "Sí" else "No"}
            
            📸 Fotos: ${registro.CantidadFotos}
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
