package com.example.negociomx_pos.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.R

class EmpresaAdapter(private val datos:List<EmpresaNube>,private val muestraAsignar:Boolean,
                     private val onClickListener:(Int, EmpresaNube)->Unit):
    RecyclerView.Adapter<EmpresaAdapter.ViewHolder>()
{
    class ViewHolder(view: View):RecyclerView.ViewHolder(view)
    {
        val razonSocial:TextView
        val nombreComercial:TextView
        val rfc:TextView
        val imgActivo:ImageView
        val telefono:TextView
        val btnAsignaEmpresa:ImageView
        val btnEditarEmpresa:ImageView

        init {
            razonSocial=view.findViewById(R.id.lblRazonSocialEmpresa)
            nombreComercial=view.findViewById(R.id.lblNombreComercialEmpresa)
            rfc=view.findViewById(R.id.lblRfcEmpresaNube)
            imgActivo=view.findViewById(R.id.imgActivaEmpresaNube)
            telefono=view.findViewById(R.id.lblTelefonoEmpresaNube)

            btnAsignaEmpresa=view.findViewById(R.id.imgAsignarEmpresaNube)
            btnEditarEmpresa=view.findViewById(R.id.imgEditaEmpresaNube)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa_nube, parent, false)

        return  ViewHolder(view)
    }

    override fun getItemCount():Int{
        if(datos.isNotEmpty())
            return  datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgActivo.setImageResource(R.drawable.icono_on_50x24)
        if(!datos[position].Activa)
            holder.imgActivo.setImageResource(R.drawable.icono_off_50x24)
        holder.razonSocial.text=datos[position].RazonSocial
        holder.nombreComercial.text=datos[position].NombreComercial
        holder.rfc.text=datos[position].Rfc
        holder.telefono.text=datos[position].Telefonos
        holder.btnAsignaEmpresa.setOnClickListener{
            val empresa=datos[position]
            onClickListener(1,empresa)
        }
        holder.btnEditarEmpresa.setOnClickListener{
            val empresa=datos[position]
            onClickListener(2,empresa)
        }
    }
}