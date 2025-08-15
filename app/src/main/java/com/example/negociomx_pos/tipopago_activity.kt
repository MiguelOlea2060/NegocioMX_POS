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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.adapters.TipoPagoAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Impuesto
import com.example.negociomx_pos.room.entities.TipoPago
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class tipopago_activity : AppCompatActivity() {
    lateinit var base: POSDatabase

    lateinit var txtNombre: EditText
    lateinit var txtClave: EditText
    lateinit var chkPagado: CheckBox
    lateinit var chkPre: CheckBox
    lateinit var chkActivo: CheckBox
    lateinit var rv: RecyclerView
    lateinit var lblEncabezado: TextView
    var idTipoPago: Int = 0

    lateinit var listaTiposPago: List<TipoPago>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tipopago)
        base = POSDatabase.getDatabase(applicationContext)

        txtNombre = findViewById(R.id.txtNombreTipoPago)
        txtClave=findViewById(R.id.txtClaveTipoPago)
        chkPagado=findViewById(R.id.chkPagadoTipoPago)
        chkPre = findViewById(R.id.chkPredeterminadoTipoPago)
        chkActivo = findViewById(R.id.chkActivoTipoPago)
        lblEncabezado=findViewById(R.id.lblEncabezadoTipoPago)
        rv = findViewById(R.id.rvTipoPago)

        lblEncabezado.setText("Tipo de pago -> Nuevo")

        val btnGuardar = findViewById<ImageView>(R.id.btnGuardarTipoPago)
        val btnRegresar = findViewById<ImageView>(R.id.btnRegresarTipoPago)

        chkActivo.isChecked=true
        chkActivo.isEnabled=false

        txtNombre.requestFocus()
        btnGuardar.setOnClickListener {
            guardaImpuesto();
        }

        btnRegresar.setOnClickListener {
            finish()
        }

        muestraListaImpuestos()
    }

    private fun muestraListaImpuestos() {
        GlobalScope.launch {
            val lista = base.tipoPagoDAO().getAll(true)

            runOnUiThread {
                lista.observe(this@tipopago_activity)
                {
                    listaTiposPago = arrayListOf()
                    listaTiposPago = it

                    val adaptador = TipoPagoAdapter(listaTiposPago, { comando,tipopago -> onItemSelected(comando,tipopago) })

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                }
            }
        }
    }
    private fun onItemSelected(comando:Int, tipoPago: TipoPago) {
        if (idTipoPago > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Categoria modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosImpuesto(tipoPago)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Desea modificar el Impuesto: ${tipoPago.Nombre} ?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosImpuesto(tipoPago)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
    }

    private fun muestraDatosImpuesto(tipoPago: TipoPago)
    {
        lblEncabezado.setText("Impuesto -> Edición")
        idTipoPago = tipoPago.IdTipoPago

        txtNombre.setText(tipoPago.Nombre)
        txtClave.setText(tipoPago.Clave)
        chkPagado.isChecked= tipoPago.Pagado
        chkPre.isChecked = tipoPago.Predeterminado

        chkActivo.isEnabled=true
        chkActivo.isChecked = tipoPago.Activo
    }

    private fun guardaImpuesto() {
        val tasaCad:String=chkPagado.text.toString()
        var tasa:Float=0F
        if(tasaCad.isNotEmpty()==true)
            tasa=tasaCad.toFloat()

        if(txtNombre.text.toString().length<3)
        {
            txtNombre.error="Es necesario suministrar un nombre minimo de 4 letras"
        }
        else if(tasaCad.isEmpty())
        {
            txtClave.error="Es necesario suministrar un clave valida"
        }
        else if(tasa<0)
        {
            chkPagado.error="Es necesario suministrar una Tasa mayor a 0"
        }
        else
        {
            val scope= MainScope()

            val nombre=txtNombre.text.toString()
            val clave=txtClave.text.toString()
            val predeterminada=chkPre.isChecked
            val activa=chkActivo.isChecked

            fun asyncFun() = scope.launch {
                if(idTipoPago==0) {
                    base.impuestoDAO().insert(
                        Impuesto(
                            Nombre = nombre,
                            Activo = activa,
                            Predeterminado = predeterminada,
                            Clave = clave,
                            Tasa = tasa
                        )
                    )
                }
                else
                {
                    base.impuestoDAO().update(
                        Impuesto(
                            IdImpuesto = idTipoPago,
                            Clave = clave,
                            Nombre = nombre,
                            Activo = activa,
                            Predeterminado = predeterminada,
                            Tasa = tasa
                        )
                    )
                }

                Toast.makeText(
                    applicationContext,"Se ha guardado el tipo de pago en el sistema",
                    Toast.LENGTH_LONG
                ).show()

                limpiarControles()
                muestraListaImpuestos()
            }

            asyncFun()
        }
    }

    private fun limpiarControles() {
        idTipoPago=0
        txtNombre.text?.clear()
        txtClave.text?.clear()

        chkPagado.isChecked=false
        chkActivo.isEnabled=false
        chkActivo.isChecked=true
        chkPre.isChecked=false
        lblEncabezado.setText("Impuesto -> Nuevo")

        txtNombre.requestFocus()
    }
}