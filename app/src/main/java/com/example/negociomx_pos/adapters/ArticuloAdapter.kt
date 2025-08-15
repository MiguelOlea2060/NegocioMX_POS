package com.example.negociomx_pos.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.R
import com.example.negociomx_pos.foto_activity
import com.example.negociomx_pos.room.BLL.BLLUtil
import com.example.negociomx_pos.room.entities.Articulo
import com.example.negociomx_pos.ventasa_activity

class ArticuloAdapter(private val datos:List<Articulo>, private val onClickListener:(Articulo) -> Unit):
    RecyclerView.Adapter<ArticuloAdapter.ViewHolder>()
{
        lateinit var context: Context
        val bllUtil=BLLUtil()
    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        val nombre: TextView
        val precioVenta:TextView
        val codigoBarra:TextView
        val existencia:TextView
        val imgStatus: ImageView
        val btnEditar: ImageView
        val imgFotoArticulo:ImageView

        init {
            nombre=view.findViewById(R.id.lblNombreArticuloConsulta)
            codigoBarra=view.findViewById(R.id.lblCodigoBarraConsulta)
            precioVenta=view.findViewById(R.id.lblPrecioVentaConsulta)
            imgStatus=view.findViewById(R.id.imgActivoArticuloConsulta)
            existencia=view.findViewById(R.id.lblExistenciaArticuloConsulta)
            imgFotoArticulo=view.findViewById(R.id.imgFotoArticuloConsulta)

            btnEditar=view.findViewById(R.id.imgEditArticuloConsulta)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context)
            .inflate(R.layout.item_articulo,parent,false)

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
        if(datos[position].IdStatus!=1)
            holder.imgStatus.setImageResource(R.drawable.icono_off_50x24)

        val nombreArchivoFoto=datos[position].NombreArchivoFoto
        val bitmap=bllUtil.getBitmapFromFilename(nombreArchivoFoto)
        if(bitmap!=null) {
            holder.imgFotoArticulo.setImageBitmap(bitmap)
            holder.imgFotoArticulo.setOnClickListener{
                /*val dialog= Dialog( context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.foto_dialog)

                val imgFoto=dialog.findViewById<ImageView>(R.id.imgFotoDialogo)
                val btnGuardarD:ImageView=dialog.findViewById(R.id.imgAceptarDialogoFoto)

                imgFoto.setImageBitmap(bitmap)
                btnGuardarD.setOnClickListener{
                    dialog.dismiss()
                }

                dialog.show()*/

                val intent= Intent(context, foto_activity::class.java)
                intent.putExtra("nombreArchivoFoto",nombreArchivoFoto)
                context.startActivity(intent)
            }
        }
        holder.nombre.text=datos[position].Nombre
        holder.codigoBarra.text=datos[position].CodigoBarra
        holder.precioVenta.text="$ "+datos[position].PrecioVenta.toString()
        holder.existencia.text="Exist.: "+"%.0f".format(datos[position].Existencia)
        holder.btnEditar.setOnClickListener{
            val articulo=datos[position]
            onClickListener(articulo)
        }
    }

}