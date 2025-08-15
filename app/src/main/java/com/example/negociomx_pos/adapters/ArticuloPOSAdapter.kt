package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.daos.ArticuloPOS
import com.example.negociomx_pos.room.entities.Articulo

class ArticuloPOSAdapter(private val datos:List<ArticuloPOS>, private val onClickListener:(ArticuloPOS) -> Unit
):
    RecyclerView.Adapter<ArticuloPOSAdapter.ViewHolder>()
{
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val nombre: TextView
        val precioVenta:TextView
        val existencia:TextView
        val card:CardView

        init {
            nombre=view.findViewById(R.id.lblNombreArticuloPOSA)
            precioVenta=view.findViewById(R.id.lblPrecioVentaArticuloPOSA)
            existencia=view.findViewById(R.id.lblExistenciaArticuloPOSA)
            card=view.findViewById(R.id.carArticuloPOS)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulos_posa,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datos.isNotEmpty())
            return datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text=datos[position].Nombre
        holder.precioVenta.text="$ "+datos[position].PrecioVenta.toString()
        holder.existencia.text="Exist.: "+"%.0f".format(datos[position].Existencia)
        holder.card.setOnClickListener{
            val articulo=datos[position]
            onClickListener(articulo)
        }
    }

}