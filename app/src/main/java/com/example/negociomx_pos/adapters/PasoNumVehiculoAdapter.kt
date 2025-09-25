package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.PasoNumLogVehiculo
import com.example.negociomx_pos.R

class PasoNumVehiculoAdapter(
    private var registros: List<PasoNumLogVehiculo>,
    private val onItemClick: (PasoNumLogVehiculo) -> Unit,
    private val onDescargarFotosClick: (PasoNumLogVehiculo) -> Unit
) : RecyclerView.Adapter<PasoNumVehiculoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVIN: TextView = view.findViewById(R.id.tvVIN)
        val tvBL: TextView = view.findViewById(R.id.tvBL)
        val tvMarcaModelo: TextView = view.findViewById(R.id.tvMarcaModelo)
        val tvAnio: TextView = view.findViewById(R.id.tvAnio)
        val tvColores: TextView = view.findViewById(R.id.tvColores)
        val tvNumeroMotor: TextView = view.findViewById(R.id.tvNumeroMotor)
        val tvDatosSOC: TextView = view.findViewById(R.id.tvDatosSOC)
        val tvFotos: TextView = view.findViewById(R.id.tvFotos)
        val btnDescargarFotos: Button = view.findViewById(R.id.btnDescargarFotos)
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
        //holder.tvNumeroMotor.text = "Numero de Motor: ${registro.NumeroMotor}"

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

        // ✅ CONFIGURAR EL CLIC EN EL ITEM
        holder.itemView.setOnClickListener {
            onItemClick(registro)
        }
        // Habilitar/deshabilitar botón según cantidad de fotos
        holder.btnDescargarFotos.isEnabled = registro.CantidadFotos > 0
        holder.btnDescargarFotos.alpha = if (registro.CantidadFotos > 0) 1.0f else 0.5f

        holder.btnDescargarFotos.setOnClickListener {
            if (registro.CantidadFotos > 0) {
                onDescargarFotosClick(registro)
            }
        }
    }

    override fun getItemCount() = registros.size

    fun actualizarRegistros(nuevosRegistros: List<PasoNumLogVehiculo>) {
        registros = nuevosRegistros
        notifyDataSetChanged()
    }
}
