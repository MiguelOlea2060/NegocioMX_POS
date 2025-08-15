package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.Categoria

class CategoriaAdapter(private val datos:List<Categoria>, private val onClickListener:(Categoria) -> Unit):
    RecyclerView.Adapter<CategoriaAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val nombre:TextView
        val imgStatus:ImageView
        val nombreStatus:TextView
        val btnEditar:ImageView

        init {
            nombre=view.findViewById(R.id.lblNombreCategoria)
            imgStatus=view.findViewById(R.id.imgActivaCategoria)
            nombreStatus=view.findViewById(R.id.lblStatusCategoria)

            btnEditar=view.findViewById(R.id.imgEditCategoria)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount()=datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nombreStatus.text="Activa"
        holder.imgStatus.setImageResource(R.drawable.icono_on_50x24)
        var preCad:String=""
        if(datos[position].Predeterminada) preCad=" (Pre)"
        if(!datos[position].Activa)
        {
            holder.imgStatus.setImageResource(R.drawable.icono_off_50x24)
            holder.nombreStatus.text="Inactiva"
        }
        holder.nombre.text=datos[position].Nombre+preCad

        holder.btnEditar.setOnClickListener{
            val categoria=datos[position]
            onClickListener(categoria)
        }
    }
}