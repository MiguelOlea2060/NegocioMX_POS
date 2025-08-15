package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.ArticuloNube
import com.example.negociomx_pos.R

class ArticuloActAdapter(private val datos:List<ArticuloNube>, private val onClickListener:(ArticuloNube) -> Unit
):
    RecyclerView.Adapter<ArticuloActAdapter.ViewHolder>()
{
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val nombre: TextView
        val codigoBarra:TextView
        val clave:TextView
        val precioVenta:TextView
        val existencia:TextView

        init {
            nombre=view.findViewById(R.id.lblNombreArticuloAct)
            codigoBarra=view.findViewById(R.id.lblCodigobarraArticuloAct)
            clave=view.findViewById(R.id.lblClaveArticuloAct)
            precioVenta=view.findViewById(R.id.lblPrecioVentaArticuloAct)
            existencia=view.findViewById(R.id.lblExistenciaArticuloAct)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo_act,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datos.isNotEmpty())
            return datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text=datos[position].Nombre
        holder.codigoBarra.text=datos[position].CodigoBarra
        holder.clave.text=datos[position].Clave
        holder.precioVenta.text="P.V: $ "+"%.2f".format(datos[position].PrecioVenta)
        holder.existencia.text="Exist.: "+"%.0f".format(datos[position].Existencia)
        /*holder.card.setOnClickListener{
            val articulo=datos[position]
            onClickListener(articulo)
        }*/
    }

}