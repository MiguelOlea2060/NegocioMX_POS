package com.example.negociomx_pos

import android.content.DialogInterface
import android.os.Bundle
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.UnidadMedidaAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.UnidadMedida
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class unidadmedida_activity : AppCompatActivity() {

    lateinit var base: POSDatabase

    lateinit var lblEncUM:TextView
    lateinit var txtNombre:EditText
    lateinit var txtAbreviatura:EditText
    lateinit var chk:CheckBox
    lateinit var btnRegresar:ImageView
    var idEmpresaLocal:Int?=null
    var idEmpresaNube:Int?=null

    lateinit var rv:RecyclerView
    var idUnidadMedida:Int=0

    lateinit var listaUnidades:List<UnidadMedida>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unidadmedida)

        btnRegresar=findViewById(R.id.btnRegresarUM)
        lblEncUM=findViewById(R.id.lblEncabezadoAltaUM)
         txtNombre=findViewById<EditText>(R.id.txtNombreUM)
         txtAbreviatura=findViewById<EditText>(R.id.txtAbreviaturaUM)
         chk=findViewById<CheckBox>(R.id.chkActivoUM)
         rv=findViewById(R.id.rvUnidadMedida)
        idEmpresaLocal=null

        base=POSDatabase.getDatabase(applicationContext)

        if(ParametrosSistema.empresaLocal!=null)
            idEmpresaLocal=ParametrosSistema.empresaLocal!!.IdEmpresa
        if(ParametrosSistema.empresaNube!=null)
            idEmpresaNube=ParametrosSistema.empresaNube.Id!!.toInt()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lblEncUM.setText("Unidad - Nueva")
        chk.isChecked=true;
        chk.isEnabled=false
        txtNombre.requestFocus()

        val btnGuardar=findViewById<ImageView>(R.id.btnGuardarUM)
        btnGuardar.setOnClickListener{
            guardaUM()
        }
        btnRegresar.setOnClickListener{
           finish()
        }

        muestraListaUnidadesMedida()
    }

    private fun muestraListaUnidadesMedida() {
        GlobalScope.launch {
            val lista= base.unidadMedidaDAO().getAll(idEmpresaLocal,null)

            runOnUiThread{
                lista.observe(this@unidadmedida_activity)
                {
                    listaUnidades= arrayListOf()
                    listaUnidades=it

                    val adaptador=UnidadMedidaAdapter(listaUnidades,{onItemListener(it)})

                    rv.layoutManager=LinearLayoutManager(applicationContext)
                    rv.adapter=adaptador
                }
            }
        }
    }

    private fun onItemListener(um: UnidadMedida) {
        if (idUnidadMedida > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Categoria modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosUnidadMedida(um)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Desea modificar la Unidad de medida: ${um.Nombre}?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosUnidadMedida(um)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
    }

    private fun muestraDatosUnidadMedida(um: UnidadMedida) {
        idUnidadMedida=um.IdUnidadMedida

        lblEncUM.setText("Unidad -> Editando")
        txtNombre.setText(um.Nombre)
        txtAbreviatura.setText(um.Abreviatura)
        chk.isEnabled=true
        chk.isChecked=um.Activa
    }

    private fun guardaUM() {
        if (txtNombre.text.toString().length < 3) {
            txtNombre.error = "Debe suministrar un Nombre"
        } else if (txtAbreviatura.text.toString().length < 2) {
            txtAbreviatura.error = "Dene suministrar una Abreviatura"
        } else {

            val scope= MainScope()

            fun asyncFun() = scope.launch {
                if(idUnidadMedida== 0) {
                    base.unidadMedidaDAO().insert(
                        UnidadMedida(
                            Nombre = txtNombre.text.toString(),
                            Abreviatura = txtAbreviatura.text.toString(),
                            Activa = chk.isChecked,
                            IdEmpresa =idEmpresaLocal
                        )
                    )
                    Toast.makeText(
                        applicationContext,"Se ha guardado la Unidad de medida",
                        Toast.LENGTH_LONG
                    ).show()

                }
                else
                {
                    base.unidadMedidaDAO().update(
                        UnidadMedida(
                            IdUnidadMedida =idUnidadMedida,
                            Nombre = txtNombre.text.toString(),
                            Abreviatura = txtAbreviatura.text.toString(),
                            Activa = chk.isChecked
                        )
                    )
                    Toast.makeText(
                        applicationContext,"Se actualizo la Unidad de medida",
                        Toast.LENGTH_LONG
                    ).show()

                }
                limpiarControles()
            }

            asyncFun()
        }
    }

    private fun limpiarControles() {
        idUnidadMedida=0

        lblEncUM.setText("Unidad - Nueva")
        txtNombre.text?.clear()
        txtAbreviatura.text?.clear()

        chk.isEnabled=false
        chk.isChecked=true

        txtNombre.requestFocus()
    }
}