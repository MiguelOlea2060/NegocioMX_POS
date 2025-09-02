package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.ConsultaPaso2Item
import com.example.negociomx_pos.R

class Paso2Adapter(
    private var registros: List<ConsultaPaso2Item>,
    private val onItemClick: (ConsultaPaso2Item) -> Unit
) : RecyclerView.Adapter<Paso2Adapter.ViewHolder>() {

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
            .inflate(R.layout.item_consulta_paso2, parent, false)
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

        // Mostrar información de fechas de fotos
        val fechasInfo = mutableListOf<String>()
        if (registro.FechaAltaFoto1.isNotEmpty()) fechasInfo.add("Foto1: ${registro.FechaAltaFoto1.substring(0, 10)}")
        if (registro.FechaAltaFoto2.isNotEmpty()) fechasInfo.add("Foto2: ${registro.FechaAltaFoto2.substring(0, 10)}")
        if (registro.FechaAltaFoto3.isNotEmpty()) fechasInfo.add("Foto3: ${registro.FechaAltaFoto3.substring(0, 10)}")
        if (registro.FechaAltaFoto4.isNotEmpty()) fechasInfo.add("Foto4: ${registro.FechaAltaFoto4.substring(0, 10)}")

        holder.tvDatosSOC.text = if (fechasInfo.isNotEmpty()) {
            "Fechas de fotos: ${fechasInfo.joinToString(" | ")}"
        } else {
            "Sin fechas de fotos registradas"
        }

        holder.tvFotos.text = "📸 ${registro.CantidadFotos} foto(s)"

        // Mostrar la fecha más reciente
        val fechaReciente = listOf(
            registro.FechaAltaFoto1,
            registro.FechaAltaFoto2,
            registro.FechaAltaFoto3,
            registro.FechaAltaFoto4
        ).filter { it.isNotEmpty() }.maxOrNull() ?: ""

        holder.tvFechaHora.text = if (fechaReciente.isNotEmpty()) {
            fechaReciente.substring(0, 19) // YYYY-MM-DD HH:mm:ss
        } else {
            "Sin fecha"
        }

        // ✅ CONFIGURAR EL CLIC EN EL ITEM
        holder.itemView.setOnClickListener {
            onItemClick(registro)
        }
    }

    override fun getItemCount() = registros.size

    fun actualizarRegistros(nuevosRegistros: List<ConsultaPaso2Item>) {
        registros = nuevosRegistros
        notifyDataSetChanged()
    }
}
