package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.Paso1SOCItem
import com.example.negociomx_pos.R

class Paso1SOCAdapter(
    private var registros: List<Paso1SOCItem>,
    private val onItemClick: (Paso1SOCItem) -> Unit
) : RecyclerView.Adapter<Paso1SOCAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVIN: TextView = view.findViewById(R.id.tvVIN)
        val tvBL: TextView = view.findViewById(R.id.tvBL)
        val tvMarcaModelo: TextView = view.findViewById(R.id.tvMarcaModelo)
        val tvAnio: TextView = view.findViewById(R.id.tvAnio)
        val tvColores: TextView = view.findViewById(R.id.tvColores)
        val tvNumeroMotor: TextView = view.findViewById(R.id.tvNumeroMotor)
        val tvDatosSOC: TextView = view.findViewById(R.id.tvDatosSOC)
        val tvFotos: TextView = view.findViewById(R.id.tvFotos)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consulta_paso1_soc, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = registros[position]

        holder.tvVIN.text = "VIN: ${registro.VIN}"
        holder.tvBL.text = "BL: ${registro.BL}"
        holder.tvMarcaModelo.text = "${registro.Marca} ${registro.Modelo}"
        holder.tvAnio.text = "Año: ${registro.Anio}"
        holder.tvColores.text = "Colores -> Ext: ${registro.ColorExterior} | Int: ${registro.ColorInterior}"
        holder.tvNumeroMotor.text = "Motor: ${registro.NumeroMotor}"

        val modoTransporte = if (registro.ModoTransporte) "Sí" else "No"
        val requiereRecarga = if (registro.RequiereRecarga) "Sí" else "No"
        holder.tvDatosSOC.text = "Odómetro: ${registro.Odometro} km | SOC: ${registro.Bateria}% | Modo Transporte: $modoTransporte | Se Recargo: $requiereRecarga"

        holder.tvFotos.text = "📸 ${registro.CantidadFotos} foto(s)"
        holder.tvFechaHora.text = registro.FechaAlta

        holder.itemView.setOnClickListener {
            onItemClick(registro)
        }
    }

    override fun getItemCount() = registros.size

    fun actualizarRegistros(nuevosRegistros: List<Paso1SOCItem>) {
        registros = nuevosRegistros
        notifyDataSetChanged()
    }
}
