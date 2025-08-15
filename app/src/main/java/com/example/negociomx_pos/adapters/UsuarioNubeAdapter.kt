package com.example.negociomx_pos.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.R
import com.example.negociomx_pos.room.BLL.BLLUtil

class UsuarioNubeAdapter(private val datos:List<UsuarioNube>, private val onClickListener:(UsuarioNube) -> Unit):
    RecyclerView.Adapter<UsuarioNubeAdapter.ViewHolder>()
{
        lateinit var context: Context
        val bllUtil=BLLUtil()
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val nombreCompleto: TextView
        val emailUsuario:TextView
        val empresaUsuario:TextView
        val imgStatus: ImageView
        val btnEditar: ImageView

        init {
            nombreCompleto=view.findViewById(R.id.lblNombreCompletoUsuarioNube)
            empresaUsuario=view.findViewById(R.id.lblEmpresaUsuarioNube)
            emailUsuario=view.findViewById(R.id.lblEmailUsuarioNube)
            imgStatus=view.findViewById(R.id.imgActivoUsuarioNube)

            btnEditar=view.findViewById(R.id.imgEditaUsuarioNube)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_nube,parent,false)

        context=parent.context

        return  ViewHolder(view)
    }

    override fun getItemCount(): Int {
        if(datos.isNotEmpty())
            return datos.size
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imgStatus.setImageResource(R.drawable.icono_on_50x24)
        if(datos[position].Activo==null || datos[position].Activo==false)
            holder.imgStatus.setImageResource(R.drawable.icono_off_50x24)
        holder.nombreCompleto.text=datos[position].NombreCompleto
        holder.empresaUsuario.text=datos[position].RazonSocialEmpresa
        holder.emailUsuario.text=datos[position].Email
        holder.btnEditar.setOnClickListener{
            val usuario=datos[position]
            onClickListener(usuario)
        }
    }

}