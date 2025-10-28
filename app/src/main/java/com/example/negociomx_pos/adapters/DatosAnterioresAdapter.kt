package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.RegistroAnteriorSOC
import com.example.negociomx_pos.R
import java.text.SimpleDateFormat
import java.util.Locale

class DatosAnterioresAdapter(
    private val registros: List<RegistroAnteriorSOC>,
    private val onVerFotoClick: (registro: RegistroAnteriorSOC, posicion: Int) -> Unit
) : RecyclerView.Adapter<DatosAnterioresAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVezNumero: TextView = view.findViewById(R.id.tvVezNumero)
        val tvFechaRegistro: TextView = view.findViewById(R.id.tvFechaRegistro)
        val tvOdometro: TextView = view.findViewById(R.id.tvOdometro)
        val tvBateria: TextView = view.findViewById(R.id.tvBateria)
        val tvModoTransporte: TextView = view.findViewById(R.id.tvModoTransporte)
        val tvRequiereRecarga: TextView = view.findViewById(R.id.tvRequiereRecarga)
        val btnVerFoto1: Button = view.findViewById(R.id.btnVerFoto1)
        val btnVerFoto2: Button = view.findViewById(R.id.btnVerFoto2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro_anterior, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = registros[position]

        // ✅ MOSTRAR DATOS DEL REGISTRO
        // ✅ CAMBIAR TEXTO SEGÚN VEZ
        val textoVez = when (registro.Vez.toInt()) {
            0 -> "1er registro"
            1 -> "2do registro"
            2 -> "3er registro"
            3 -> "4to registro"
            else -> "Registro numero ${registro.Vez + 1}"
        }

// ✅ OCULTAR ODÓMETRO Y MODO TRANSPORTE PARA VEZ >= 1





        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
        val formatoSalida = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())



        if (registro.Vez >= 1) {
            // Ocultar los LinearLayout que contienen Odómetro y ModoTransporte
            try {
                val fecha = formatoEntrada.parse(registro.FechaAlta)
                holder.tvFechaRegistro.text = "📅 ${formatoSalida.format(fecha)}"
            } catch (e: Exception) {
                // Si hay error al parsear, mostramos el valor original
                holder.tvFechaRegistro.text = "📅 ${registro.FechaAlta}"
            }
            holder.itemView.findViewById<LinearLayout>(R.id.layoutOdometro)?.visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.tvModoTransporte)?.visibility = View.GONE
            holder.itemView.findViewById<TextView>(R.id.tvRequiereRecarga)?.visibility = View.VISIBLE
            holder.tvBateria.text = "${registro.Bateria}%"
            holder.tvRequiereRecarga.text = "⚡ Recarga: ${if (registro.RequiereRecarga) "SÍ" else "NO"}"
        } else {
            // Mostrar para Vez 0
            try {
                val fecha = formatoEntrada.parse(registro.FechaAlta)
                holder.tvFechaRegistro.text = "📅 ${formatoSalida.format(fecha)}"
            } catch (e: Exception) {
                // Si hay error al parsear, mostramos el valor original
                holder.tvFechaRegistro.text = "📅 ${registro.FechaAlta}"
            }
            holder.itemView.findViewById<LinearLayout>(R.id.layoutOdometro)?.visibility = View.VISIBLE
            holder.itemView.findViewById<LinearLayout>(R.id.layoutModoTransporte)?.visibility = View.VISIBLE
            holder.tvBateria.text = "${registro.Bateria}%"
            holder.tvRequiereRecarga.text = "⚡ Recarga: ${if (registro.RequiereRecarga) "SÍ" else "NO"}"
            // Asignar valores
            holder.tvOdometro.text = "${registro.Odometro} km"
            holder.tvModoTransporte.text = "🚛 Modo Transporte: ${if (registro.ModoTransporte) "SÍ" else "NO"}"
        }




       holder.tvVezNumero.text = textoVez



        // ✅ CONFIGURAR BOTONES DE FOTOS
        if (registro.FotosPosicion1 > 0 && registro.NombreArchivo1.isNotEmpty()) {
            holder.btnVerFoto1.isEnabled = true
            holder.btnVerFoto1.alpha = 1.0f
            holder.btnVerFoto1.setOnClickListener {
                onVerFotoClick(registro, 1)
            }
        } else {
            holder.btnVerFoto1.isEnabled = false
            holder.btnVerFoto1.alpha = 0.5f
            holder.btnVerFoto1.text = "❌ Sin Foto 1"
        }

        if (registro.FotosPosicion2 > 0 && registro.NombreArchivo2.isNotEmpty()) {
            holder.btnVerFoto2.isEnabled = true
            holder.btnVerFoto2.alpha = 1.0f
            holder.btnVerFoto2.setOnClickListener {
                onVerFotoClick(registro, 2)
            }
        } else {
            holder.btnVerFoto2.isEnabled = false
            holder.btnVerFoto2.alpha = 0.5f
            holder.btnVerFoto2.text = "❌ Sin Foto 2"
        }
    }

    override fun getItemCount() = registros.size
}