package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.TipoPago

class TipoPagoAdapter(private val datos:List<TipoPago>, private val onClickListener:(Int, TipoPago) -> Unit):
    RecyclerView.Adapter<TipoPagoAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val nombreClave:TextView
        val imgActivo:ImageView
        val detalles:TextView
        val btnEditar:ImageView
        val btnEliminar:ImageView

        init {
            nombreClave=view.findViewById(R.id.lblNombreyClaveTipoPago)
            imgActivo=view.findViewById(R.id.imgActivoTipoPago)
            detalles=view.findViewById(R.id.lblDetallesTipoPago)

            btnEditar=view.findViewById(R.id.imgEditaTipoPago)
            btnEliminar=view.findViewById(R.id.imgEliminarTipoPago)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tipo_pago,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount()=datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgActivo.setImageResource(R.drawable.icono_on_50x24)
        if(!datos[position].Activo)
            holder.imgActivo.setImageResource(R.drawable.icono_off_50x24)

        var detalles:String=""
        if(datos[position].Credito==true)detalles+="Credito: Si"
        else detalles+="Credito: No"
        if(datos[position].Pagado==true)detalles+=", Pagado: Si"
        else detalles+=", Pagado: No"
        if(datos[position].ConBanco==true)detalles+=", Con banco: Si"
        else detalles+=", Con banco: No"

        holder.detalles.text=detalles
        holder.nombreClave.text=datos[position].Nombre+" (${datos[position].Clave})"

        holder.btnEditar.setOnClickListener{
            val impuesto=datos[position]
            onClickListener(1,impuesto)
        }
        holder.btnEliminar.setOnClickListener{
            val impuesto=datos[position]
            onClickListener(2,impuesto)
        }
    }
}