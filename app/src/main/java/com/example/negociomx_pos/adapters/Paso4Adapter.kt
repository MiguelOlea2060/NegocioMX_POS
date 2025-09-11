package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.ConsultaPaso4Item
import com.example.negociomx_pos.R

class Paso4Adapter(
    private var registros: List<ConsultaPaso4Item>,
    private val onItemClick: (ConsultaPaso4Item) -> Unit
) : RecyclerView.Adapter<Paso4Adapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVIN: TextView = view.findViewById(R.id.tvVIN)
        val tvBL: TextView = view.findViewById(R.id.tvBL)
        val tvMarcaModelo: TextView = view.findViewById(R.id.tvMarcaModelo)
        val tvAnio: TextView = view.findViewById(R.id.tvAnio)
        val tvColores: TextView = view.findViewById(R.id.tvColores)
        val tvNumeroMotor: TextView = view.findViewById(R.id.tvNumeroMotor)
        val tvDatosSOC: TextView = view.findViewById(R.id.tvDatosSOC)
        val tvFotos: TextView = view.findViewById(R.id.tvFotos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consulta_paso4, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val registro = registros[position]

        holder.tvVIN.text = "VIN: ${registro.VIN}"
        holder.tvBL.text = "BL: ${registro.BL}"
        holder.tvMarcaModelo.text = "${registro.Marca} ${registro.Modelo}"
        holder.tvAnio.text = "Año: ${registro.Anio}"
        holder.tvColores.text = "Colores -> Ext: ${registro.ColorExterior} | Int: ${registro.ColorInterior}"
        holder.tvNumeroMotor.text = "Número de Motor: ${registro.NumeroMotor}"

        // Mostrar información específica de Paso 4 - Llantas
        val estadoLlantas = mutableListOf<String>()
        if (registro.Llanta1Verificada) estadoLlantas.add("DI✅") else estadoLlantas.add("DI❌")
        if (registro.Llanta2Verificada) estadoLlantas.add("DD✅") else estadoLlantas.add("DD❌")
        if (registro.Llanta3Verificada) estadoLlantas.add("TI✅") else estadoLlantas.add("TI❌")
        if (registro.Llanta4Verificada) estadoLlantas.add("TD✅") else estadoLlantas.add("TD❌")

        holder.tvDatosSOC.text = "LLANTAS: ${estadoLlantas.joinToString(" | ")} | ${registro.LlantasVerificadas}/${registro.CantidadLlantas} verificadas"

        holder.tvFotos.text = "📸 ${registro.LlantasConFoto} foto(s)"

        // ✅ CONFIGURAR EL CLIC EN EL ITEM
        holder.itemView.setOnClickListener {
            onItemClick(registro)
        }
    }

    override fun getItemCount() = registros.size

    fun actualizarRegistros(nuevosRegistros: List<ConsultaPaso4Item>) {
        registros = nuevosRegistros
        notifyDataSetChanged()
    }
}
