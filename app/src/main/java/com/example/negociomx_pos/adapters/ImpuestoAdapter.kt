package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.entities.Impuesto

class ImpuestoAdapter(private val datos:List<Impuesto>, private val onClickListener:(Impuesto) -> Unit):
    RecyclerView.Adapter<ImpuestoAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val nombreClave:TextView
        val imgActivo:ImageView
        val tasa:TextView
        val btnEditar:ImageView

        init {
            nombreClave=view.findViewById(R.id.lblNombreClaveImpuestoCon)
            imgActivo=view.findViewById(R.id.imgActivoImpuestoCon)
            tasa=view.findViewById(R.id.lblTasaImpuestoCon)

            btnEditar=view.findViewById(R.id.imgEditImpuesto)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_impuesto,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount()=datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgActivo.setImageResource(R.drawable.icono_on_50x24)
        if(!datos[position].Activo)
            holder.imgActivo.setImageResource(R.drawable.icono_off_50x24)
        holder.tasa.text="Tasa: "+datos[position].Tasa.toString()+" %"
        holder.nombreClave.text=datos[position].Nombre+" (${datos[position].Clave})"

        holder.btnEditar.setOnClickListener{
            val impuesto=datos[position]
            onClickListener(impuesto)
        }
    }
}