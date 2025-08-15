package com.example.negociomx_pos

import android.content.DialogInterface
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.widget.Button
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
import com.example.negociomx_pos.adapters.ImpuestoAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Categoria
import com.example.negociomx_pos.room.entities.Impuesto
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class impuesto_activity : AppCompatActivity() {
    lateinit var base: POSDatabase

    lateinit var txtNombre: EditText
    lateinit var txtClave: EditText
    lateinit var txtTasa: EditText
    lateinit var chkPre: CheckBox
    lateinit var chkActivo: CheckBox
    lateinit var rv: RecyclerView
    lateinit var lblEncabezado: TextView
    var idImpuesto: Int = 0

    lateinit var listaImpuestos: List<Impuesto>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impuesto)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        base = POSDatabase.getDatabase(applicationContext)

        txtNombre = findViewById(R.id.txtNombreImpuesto)
        txtClave = findViewById(R.id.txtClaveImpuesto)
        txtTasa = findViewById(R.id.txtTasaImpuesto)
        chkPre = findViewById(R.id.chkPredeterminadoImpuesto)
        chkActivo = findViewById(R.id.chkActivoImpuesto)
        lblEncabezado = findViewById(R.id.lblEncabezadoImpuesto)
        rv = findViewById(R.id.rvImpuesto)

        lblEncabezado.setText("Impuesto -> Nuevo")

        val btnGuardar = findViewById<ImageView>(R.id.btnGuardarImpuesto)
        val btnCerrar = findViewById<ImageView>(R.id.btnRegresarImpuesto)

        chkActivo.isChecked = true
        chkActivo.isEnabled = false

        txtNombre.requestFocus()
        btnGuardar.setOnClickListener {
            guardaImpuesto();
        }

        btnCerrar.setOnClickListener {
            finish()
        }

        muestraListaImpuestos()
    }

    private fun muestraListaImpuestos() {
        GlobalScope.launch {
            val lista = base.impuestoDAO().getAll()

            runOnUiThread {
                lista.observe(this@impuesto_activity)
                {
                    listaImpuestos = arrayListOf()
                    listaImpuestos = it

                    val adaptador = ImpuestoAdapter(listaImpuestos, { onItemSelected(it) })

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                }
            }
        }
    }

    private fun onItemSelected(impuesto: Impuesto) {
        if (idImpuesto > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Categoria modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosImpuesto(impuesto)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Desea modificar el Impuesto: ${impuesto.Nombre} ?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    muestraDatosImpuesto(impuesto)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
    }

    private fun muestraDatosImpuesto(impuesto: Impuesto) {
        lblEncabezado.setText("Impuesto -> Edición")
        idImpuesto = impuesto.IdImpuesto

        txtNombre.setText(impuesto.Nombre)
        txtClave.setText(impuesto.Clave)
        txtTasa.setText(impuesto.Tasa.toString())
        chkPre.isChecked = impuesto.Predeterminado

        chkActivo.isEnabled = true
        chkActivo.isChecked = impuesto.Activo
    }

    private fun guardaImpuesto() {
        val tasaCad: String = txtTasa.text.toString()
        var tasa: Float = 0F
        if (tasaCad.isNotEmpty() == true)
            tasa = tasaCad.toFloat()

        if (txtNombre.text.toString().length < 3) {
            txtNombre.error = "Es necesario suministrar un nombre minimo de 4 letras"
        } else if (tasaCad.isEmpty()) {
            txtClave.error = "Es necesario suministrar un clave valida"
        } else if (tasa < 0) {
            txtTasa.error = "Es necesario suministrar una Tasa mayor a 0"
        } else {
            val scope = MainScope()

            val nombre = txtNombre.text.toString()
            val clave = txtClave.text.toString()
            val predeterminada = chkPre.isChecked
            val activa = chkActivo.isChecked

            fun asyncFun() = scope.launch {
                if (idImpuesto == 0) {
                    base.impuestoDAO().insert(
                        Impuesto(
                            Nombre = nombre,
                            Activo = activa,
                            Predeterminado = predeterminada,
                            Clave = clave,
                            Tasa = tasa
                        )
                    )
                } else {
                    base.impuestoDAO().update(
                        Impuesto(
                            IdImpuesto = idImpuesto,
                            Clave = clave,
                            Nombre = nombre,
                            Activo = activa,
                            Predeterminado = predeterminada,
                            Tasa = tasa
                        )
                    )
                }

                Toast.makeText(
                    applicationContext, "Se ha guardado el Impuesto en el sistema",
                    Toast.LENGTH_LONG
                ).show()

                limpiarControles()
                muestraListaImpuestos()
            }

            asyncFun()
        }
    }

    private fun limpiarControles() {
        idImpuesto = 0
        txtNombre.text?.clear()
        txtClave.text?.clear()
        txtTasa.text?.clear()

        chkActivo.isEnabled = false
        chkActivo.isChecked = true
        chkPre.isChecked = false
        lblEncabezado.setText("Impuesto -> Nuevo")

        txtNombre.requestFocus()
    }
}