package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.UnidadMedida

class UnidadMedidaAdapter(private  val datos:List<UnidadMedida>, private val onClickListener:(UnidadMedida)->Unit):
RecyclerView.Adapter<UnidadMedidaAdapter.ViewHolder>()
{
    class  ViewHolder(view:View):RecyclerView.ViewHolder(view)
    {
        val nombre:TextView
        val abreviatura:TextView
        val card:CardView

        init {
            nombre=view.findViewById(R.id.lblNombreUM)
            abreviatura=view.findViewById(R.id.lblAbreviaturaUM )
            card=view.findViewById(R.id.cardUM)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_unidadmedida,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount()=datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombre.text=datos[position].Nombre
        holder.abreviatura.text =datos[position].Abreviatura
        holder.card.setOnClickListener{
            val um=datos[position]
            onClickListener(um)
        }
    }

}