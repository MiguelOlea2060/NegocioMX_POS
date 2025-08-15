package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.daos.DoctoVenta
import com.example.negociomx_pos.room.entities.DocDet
import com.example.negociomx_pos.room.entities.DocyDetalles

class VentasAAdapter(private val datos:List<DoctoVenta>, private val onClickListener:(DoctoVenta, Int) -> Unit
):
    RecyclerView.Adapter<VentasAAdapter.ViewHolder>()
{
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val folio: TextView
        val cliente: TextView
        val subtotal:TextView
        val numArticulos:TextView
        val iva:TextView
        val total:TextView
        val card:CardView
        val imgDetalles:ImageView
        val imgStatus:ImageView
        val imgImprimeTicket:ImageView

        init {
            folio=view.findViewById(R.id.lblFolioVentaA)
            cliente=view.findViewById(R.id.lblClienteVentaA)
            subtotal=view.findViewById(R.id.lblSubtotalVentaA)
            iva=view.findViewById(R.id.lblMontoImpuestoVentaA)
            total=view.findViewById(R.id.lblTotalVentaA)
            numArticulos=view.findViewById(R.id.lblTotalArticulosVentaA)

            imgStatus=view.findViewById(R.id.imgStatusVentaA)
            imgDetalles=view.findViewById(R.id.imgVerDetallesVentaA)
            imgImprimeTicket=view.findViewById(R.id.imgImprimirTicketVentaA)

            card=view.findViewById(R.id.cardVentaA)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ventasa,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datos.isNotEmpty())
            return datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cliente.text=datos[position].NombreCliente
        holder.folio.text=datos[position].Folio
        holder.numArticulos.text="%.0f".format(datos[position].NumArticulos)+" Articulos"
        holder.subtotal.text="Subtotal\n$ "+ "%.0f".format(datos[position].Subtotal)
        holder.iva.text="IVA\n$ "+"%.0f".format(datos[position].IVA)
        holder.total.text="Total\n$ "+"%.0f".format(datos[position].Total)

        holder.imgStatus.setImageResource(R.drawable.icono_on_50x24)
        if(!datos[position].Activo)
            holder.imgStatus.setImageResource(R.drawable.icono_off_50x24)

        holder.imgDetalles.setOnClickListener{
            val articulo=datos[position]
            onClickListener(articulo,1)
        }
        holder.imgImprimeTicket.setOnClickListener{
            val articulo=datos[position]
            onClickListener(articulo,2)
        }
    }

}