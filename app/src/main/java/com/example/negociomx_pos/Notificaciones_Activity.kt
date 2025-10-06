package com.example.negociomx_pos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.PasoNumLogVehiculoNotificacion
import com.example.negociomx_pos.DAL.DALNotificaciones
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.NotificacionesAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Notificaciones_Activity : AppCompatActivity() {

    private lateinit var rvNotificaciones: RecyclerView
    private lateinit var tvContador: TextView
    private lateinit var layoutSinNotificaciones: LinearLayout
    private lateinit var btnVolver: ImageView

    private lateinit var dalNotificaciones: DALNotificaciones
    private lateinit var adapter: NotificacionesAdapter
    private var listaNotificaciones: List<PasoNumLogVehiculoNotificacion> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        inicializarVistas()
        inicializarDAL()
        configurarRecyclerView()
        configurarListeners()
        cargarNotificaciones()
    }

    private fun inicializarVistas() {
        rvNotificaciones = findViewById(R.id.rvNotificaciones)
        tvContador = findViewById(R.id.tvContadorNotificaciones)
        layoutSinNotificaciones = findViewById(R.id.layoutSinNotificaciones)
        btnVolver = findViewById(R.id.btnVolverNotificaciones)
    }

    private fun inicializarDAL() {
        dalNotificaciones = DALNotificaciones()
    }

    private fun configurarRecyclerView() {
        adapter = NotificacionesAdapter(listaNotificaciones) { notificacion ->
            onNotificacionClick(notificacion)
        }
        rvNotificaciones.layoutManager = LinearLayoutManager(this)
        rvNotificaciones.adapter = adapter
    }

    private fun configurarListeners() {
        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarNotificaciones() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val notificaciones = withContext(Dispatchers.IO) {
                    dalNotificaciones.obtenerListadoNotificaciones()
                }

                listaNotificaciones = notificaciones
                adapter.actualizarLista(notificaciones)

                actualizarUI(notificaciones.size)

            } catch (e: Exception) {
                Log.e("Notificaciones_Activity", "Error cargando notificaciones: ${e.message}")
                Toast.makeText(
                    this@Notificaciones_Activity,
                    "Error al cargar notificaciones",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun actualizarUI(cantidad: Int) {
        tvContador.text = cantidad.toString()

        if (cantidad == 0) {
            rvNotificaciones.visibility = View.GONE
            layoutSinNotificaciones.visibility = View.VISIBLE
        } else {
            rvNotificaciones.visibility = View.VISIBLE
            layoutSinNotificaciones.visibility = View.GONE
        }
    }

    private fun onNotificacionClick(notificacion: PasoNumLogVehiculoNotificacion) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Marcar como vista
                val marcada = withContext(Dispatchers.IO) {
                    dalNotificaciones.marcarNotificacionComoVista(
                        notificacion.IdPasoNumLogVehiculoNotificacion,
                        ParametrosSistema.usuarioLogueado.IdUsuario
                    )
                }

                if (marcada) {
                    // Navegar a la actividad correspondiente según el paso
                    when (notificacion.Paso.toInt()) {
                        1 -> {
                            val intent = Intent(this@Notificaciones_Activity, Paso1SOC_Activity::class.java)
                            intent.putExtra("idVehiculo", notificacion.IdVehiculo)
                            intent.putExtra("vin", notificacion.Vin)
                            startActivity(intent)
                        }
                        2 -> {
                            val intent = Intent(this@Notificaciones_Activity, Paso2SOC_Activity::class.java)
                            intent.putExtra("idVehiculo", notificacion.IdVehiculo)
                            intent.putExtra("vin", notificacion.Vin)
                            startActivity(intent)
                        }
                        3 -> {
                            val intent = Intent(this@Notificaciones_Activity, Paso3Repuve_Activity::class.java)
                            intent.putExtra("idVehiculo", notificacion.IdVehiculo)
                            intent.putExtra("vin", notificacion.Vin)
                            startActivity(intent)
                        }
                    }

                    // Recargar notificaciones
                    cargarNotificaciones()
                }

            } catch (e: Exception) {
                Log.e("Notificaciones_Activity", "Error marcando notificación: ${e.message}")
                Toast.makeText(
                    this@Notificaciones_Activity,
                    "Error al procesar notificación",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar notificaciones al volver a la actividad
        cargarNotificaciones()
    }
}