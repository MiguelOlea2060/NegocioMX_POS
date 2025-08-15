package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.DispositivoAccesoUnico
import com.example.negociomx_pos.R
import org.w3c.dom.Text

class DispositivoAccesoAdapter(private val datos:List<DispositivoAccesoUnico>,
                               private val onClickListener:(Int, DispositivoAccesoUnico) -> Unit):
    RecyclerView.Adapter<DispositivoAccesoAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val idDispositivo:TextView
        val imgStatus:ImageView
        val cantidadLogueos:TextView
        val nombreEmpresa:TextView
        val btnDetallesDispositivo:ImageView

        init {
            idDispositivo=view.findViewById(R.id.lblNombreDispositivoAcceso)
            imgStatus=view.findViewById(R.id.imgActivoDispositivoAcceso)
            cantidadLogueos=view.findViewById(R.id.lblCantLogueosDispositivoAcceso)
            nombreEmpresa=view.findViewById(R.id.lblEmpresaDispositivoAcceso)

            btnDetallesDispositivo=view.findViewById(R.id.imgDetallesDispositivoAcceso)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dispositivo_acceso,parent,false)

        return  ViewHolder(view)
    }

    override fun getItemCount()=datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.cantidadLogueos.text="Cantidad logueos: "+datos[position].CantidadLogueos.toString()
        holder.imgStatus.setImageResource(R.drawable.icono_on_50x24)
        if(datos[position].Activo==null || datos[position].Activo==false)
            holder.imgStatus.setImageResource(R.drawable.icono_off_50x24)
        holder.idDispositivo.text="Id dispositivo: "+datos[position].IdDispositivo
        holder.nombreEmpresa.text="Sin Empresa asignada"

        holder.btnDetallesDispositivo.setOnClickListener{
            val dispositivo=datos[position]
            onClickListener(2, dispositivo)
        }
    }
}