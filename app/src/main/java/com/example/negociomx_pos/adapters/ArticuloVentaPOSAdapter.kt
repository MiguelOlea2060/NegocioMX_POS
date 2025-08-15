package com.example.negociomx_pos.adapters

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.DocDet

class ArticuloVentaPOSAdapter(private val datos:List<DocDet>, private val onClickListener: (DocDet,Int) -> Unit
):
    RecyclerView.Adapter<ArticuloVentaPOSAdapter.ViewHolder>()
{
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val nombre: TextView
        val precioVenta:TextView
        val importe:TextView
        val cantidad:TextView
        var imgEliminar:ImageView
        var imgAgrega:ImageView

        init {
            nombre=view.findViewById(R.id.lblNombreArticuloVentaPOSA)
            precioVenta=view.findViewById(R.id.lblPrecioVentaArticuloVentaPOSA)
            cantidad=view.findViewById(R.id.lblCantidadArticuloVentaPOSA)
            importe=view.findViewById(R.id.lblImporteArticuloVentaPOSA)
            imgEliminar=view.findViewById(R.id.imgEliminaArticuloVentaPOS)
            imgAgrega=view.findViewById(R.id.imgAgregaCantidadArticuloVentaPOS)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulos_venta_pos,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datos.isNotEmpty())
            return datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dec = DecimalFormat("#,##0.00")

        holder.nombre.text=datos[position].NombreArticuloServicio
        holder.precioVenta.text="$ "+dec.format(datos[position].PrecioUnitario)
        holder.cantidad.text="%.0f".format(datos[position].Cantidad)
        holder.importe.text="$ "+dec.format(datos[position].Importe)

        holder.imgEliminar.setOnClickListener{
            val detalle=datos[position]
            onClickListener(detalle,1)
        }
        holder.imgAgrega.setOnClickListener{
            val detalle=datos[position]
            onClickListener(detalle,2)
        }
    }

}