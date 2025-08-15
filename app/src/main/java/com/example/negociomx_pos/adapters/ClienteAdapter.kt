package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomOpenHelper
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.Cliente

class ClienteAdapter(private val datos:List<Cliente>, private val onClickListener:(Cliente)->Unit):
    RecyclerView.Adapter<ClienteAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val nombre:TextView
        val rfc:TextView
        val imgActivo:ImageView
        val telefono:TextView
        val btnEditar:ImageView

        init {
            nombre=view.findViewById(R.id.lblNombreCliente)
            rfc=view.findViewById(R.id.lblRfcCliente)
            imgActivo=view.findViewById(R.id.imgActivoCliente)
            telefono=view.findViewById(R.id.lblTelefonoCliente)
            btnEditar=view.findViewById(R.id.imgEditCliente)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)

        return  ViewHolder(view)
    }

    override fun getItemCount():Int{
        if(datos.isNotEmpty())
            return  datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgActivo.setImageResource(R.drawable.icono_on_50x24)
        if(!datos[position].Activo)
            holder.imgActivo.setImageResource(R.drawable.icono_off_50x24)
        var preCad:String=""
        if(datos[position].Predeterminado)
            preCad=" (Pre)"
        holder.nombre.text=datos[position].Nombre+preCad
        holder.rfc.text=datos[position].Rfc
        holder.telefono.text=datos[position].Telefonos
        holder.btnEditar.setOnClickListener{
            val cliente=datos[position]
            onClickListener(cliente)
        }
    }
}