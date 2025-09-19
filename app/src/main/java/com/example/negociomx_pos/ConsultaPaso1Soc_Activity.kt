package com.example.negociomx_pos

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import android.widget.Button
//import android.widget.CalendarView
import android.app.DatePickerDialog
import android.util.Log
import android.widget.CheckBox
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
import com.example.negociomx_pos.Utils.ParametrosSistema
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
    private lateinit var chkTodosLosUsuario:CheckBox

    // Estadísticas
    private lateinit var tvVehiculosUnicos: TextView
    private lateinit var tvTotalFotos: TextView

    private lateinit var dalConsultaSOC: DALPaso1SOC
    private lateinit var adapter: Paso1SOCAdapter
    private var fechaSeleccionada: String = ""
    private var loadingHandler: Handler? = null
    private var loadingRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_paso1_soc)

        inicializarComponentes()
        configurarEventos()
        configurarRecyclerView()

        establecerFechaActual()
        realizarConsultaInicial()
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
        chkTodosLosUsuario=findViewById(R.id.chkTodosUsuarioPaso1)

        // Estadísticas
        tvVehiculosUnicos = findViewById(R.id.tvVehiculosUnicos)
        tvTotalFotos = findViewById(R.id.tvTotalFotos)

        dalConsultaSOC = DALPaso1SOC()
    }

    private fun configurarEventos() {
        // CAMBIAR esta línea:
        tvFechaSeleccionada.setOnClickListener {
            mostrarSelectorFecha()  // Cambiar de mostrarCalendario() a mostrarSelectorFecha()
        }

        chkTodosLosUsuario.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
                Toast.makeText(this, "Consultando todos los usuarios", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Consultando usuario actual", Toast.LENGTH_SHORT).show()
            consultarRegistrosPorFecha(fechaSeleccionada)
        }

        // Botón consultar (SIN CAMBIOS)
        btnConsultar.setOnClickListener {
            Toast.makeText(this, "Mostrando calendario", Toast.LENGTH_SHORT).show()
            consultarRegistrosPorFecha(fechaSeleccionada)
        }
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
                Log.e("ConsultaPaso1SOC", "Error parseando fecha: ${e.message}")
            }
        }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val fechaAux = fechaSeleccionada

                val fechaSeleccionadaCalendar = Calendar.getInstance()
                fechaSeleccionadaCalendar.set(year, month, dayOfMonth)

                val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoMostrar = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                fechaSeleccionada = formatoFecha.format(fechaSeleccionadaCalendar.time)
                tvFechaSeleccionada.text = formatoMostrar.format(fechaSeleccionadaCalendar.time)

                if (!fechaAux.equals(fechaSeleccionada)) {
                    consultarRegistrosPorFecha(fechaSeleccionada)
                }
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
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
                var idUsuario:Int?=ParametrosSistema.usuarioLogueado.IdUsuario
                if(chkTodosLosUsuario.isChecked)
                    idUsuario=null

                val registros = dalConsultaSOC.consultarPaso1SOCPorFecha(fecha, idUsuario)

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
        var fecha1:String=""
        var fecha2:String=""
        var fecha3:String=""
        var fecha4:String=""
        if(registro.FechaAltaFoto1.isNotEmpty() && registro.FechaAltaFoto1.toString().trim().isNotEmpty())
            fecha1="1 -> "+registro.FechaAltaFoto1
        if(registro.FechaAltaFoto2.isNotEmpty() && registro.FechaAltaFoto2.toString().trim().isNotEmpty())
            fecha2="2 -> "+registro.FechaAltaFoto2
        if(registro.FechaAltaFoto3.isNotEmpty() && registro.FechaAltaFoto3.toString().trim().isNotEmpty())
            fecha3="3 -> "+registro.FechaAltaFoto3
        if(registro.FechaAltaFoto4.isNotEmpty() && registro.FechaAltaFoto4.toString().trim().isNotEmpty())
            fecha4="4 -> "+registro.FechaAltaFoto4

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
            📅 ${"Fechas de Fotos"}
             ${fecha1}
             ${fecha2}
             ${fecha3}
             ${fecha4}
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
