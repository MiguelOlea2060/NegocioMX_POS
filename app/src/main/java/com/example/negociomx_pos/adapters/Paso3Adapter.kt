package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.ConsultaPaso3Item
import com.example.negociomx_pos.R

class Paso3Adapter(
    private var registros: List<ConsultaPaso3Item>,
    private val onItemClick: (ConsultaPaso3Item) -> Unit
) : RecyclerView.Adapter<Paso3Adapter.ViewHolder>() {

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
            .inflate(R.layout.item_consulta_paso3, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = registros[position]

        holder.tvVIN.text = "VIN: ${registro.VIN}"
        holder.tvBL.text = "BL: ${registro.BL}"
        holder.tvMarcaModelo.text = "${registro.Marca} ${registro.Modelo}"
        holder.tvAnio.text = "Año: ${registro.Anio}"
        holder.tvColores.text = "Colores -> Ext: ${registro.ColorExterior} | Int: ${registro.ColorInterior}"
        holder.tvNumeroMotor.text = "Numero de Motor: ${registro.NumeroMotor}"

        // Mostrar información específica de Paso 3 REPUVE
        holder.tvDatosSOC.text = "REPUVE: ${if (registro.TieneFoto) "✅ Foto registrada" else "❌ Sin foto"} | Archivo: ${registro.NombreArchivoFoto}"

        holder.tvFotos.text = "📸 ${if (registro.TieneFoto) "1" else "0"} foto(s)"

        // Mostrar fecha de alta
        holder.tvFechaHora.text = if (registro.FechaAlta.isNotEmpty()) {
            registro.FechaAlta.substring(0, 19) // YYYY-MM-DD HH:mm:ss
        } else {
            "Sin fecha"
        }

        // ✅ CONFIGURAR EL CLIC EN EL ITEM
        holder.itemView.setOnClickListener {
            onItemClick(registro)
        }
    }

    override fun getItemCount() = registros.size

    fun actualizarRegistros(nuevosRegistros: List<ConsultaPaso3Item>) {
        registros = nuevosRegistros
        notifyDataSetChanged()
    }
}