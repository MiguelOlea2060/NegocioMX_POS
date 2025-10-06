package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.PasoNumLogVehiculoNotificacion
import com.example.negociomx_pos.R
import java.text.SimpleDateFormat
import java.util.*

class NotificacionesAdapter(
    private var listaNotificaciones: List<PasoNumLogVehiculoNotificacion>,
    private val onNotificacionClick: (PasoNumLogVehiculoNotificacion) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVin: TextView = itemView.findViewById(R.id.tvVinNotificacion)
        val tvPaso: TextView = itemView.findViewById(R.id.tvPasoNotificacion)
        val tvPosicion: TextView = itemView.findViewById(R.id.tvPosicionNotificacion)
        val tvVez: TextView = itemView.findViewById(R.id.tvVezNotificacion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFechaNotificacion)

        fun bind(notificacion: PasoNumLogVehiculoNotificacion) {
            tvVin.text = "VIN: ${notificacion.Vin}"
            tvPaso.text = "Paso ${notificacion.Paso}"
            tvPosicion.text = notificacion.Posicion.toString()
            tvVez.text = notificacion.Vez.toString()

            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            tvFecha.text = "Fecha: ${formato.format(notificacion.FechaAlta ?: Date())}"

            itemView.setOnClickListener {
                onNotificacionClick(notificacion)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(listaNotificaciones[position])
    }

    override fun getItemCount(): Int = listaNotificaciones.size

    fun actualizarLista(nuevaLista: List<PasoNumLogVehiculoNotificacion>) {
        listaNotificaciones = nuevaLista
        notifyDataSetChanged()
    }
}