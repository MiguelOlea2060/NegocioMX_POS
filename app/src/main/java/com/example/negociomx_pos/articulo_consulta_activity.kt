package com.example.negociomx_pos

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.adapters.ArticuloAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Articulo
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class articulo_consulta_activity : AppCompatActivity() {
    private lateinit var base: POSDatabase
    private lateinit var rv: RecyclerView

    private var idArticulo: Int = 0
    private lateinit var listaArticulos: List<Articulo>
    lateinit var txtNombreBuscar: EditText

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resul ->
            if (resul.resultCode == Activity.RESULT_OK) {
                var intent = resul.data
                var actualiza: Boolean = intent?.getBooleanExtra("actualiza", false)!!
                if (actualiza) {
                    val nombreFilter = txtNombreBuscar.text.toString()
                    muestraListaArticulos(nombreFilter)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_articulo_consulta)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        base = POSDatabase.getDatabase(applicationContext)

        txtNombreBuscar = findViewById(R.id.txtNombreBuscarConsulta)
        rv = findViewById(R.id.rvArticulo)

        txtNombreBuscar.requestFocus()
        txtNombreBuscar.setOnKeyListener({ v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                val nombreFilter = txtNombreBuscar.text.toString()
                muestraListaArticulos(nombreFilter)
                return@setOnKeyListener true
            }
            false
        })

        val btnBuscar = findViewById<ImageView>(R.id.imgBuscarArticulos)
        btnBuscar.setOnClickListener() {
            val nombreFilter = txtNombreBuscar.text.toString()
            muestraListaArticulos(nombreFilter)
        }

        muestraListaArticulos("")
    }

    private fun muestraListaArticulos(nombre: String) {
        val scope = MainScope()

        var nombreFilter: String = nombre
        if (nombreFilter.isEmpty() == false)
            nombreFilter = "%" + nombreFilter + "%"

        fun asyncFun() = scope.launch {
            val lista = base.articuloDAO().getAll(nombreFilter)

            lista.observe(this@articulo_consulta_activity) {
                if (it != null && it.isNotEmpty()) {
                    listaArticulos = arrayListOf()
                    listaArticulos = it

                    val adaptador =
                        ArticuloAdapter(listaArticulos) { articulo -> onItemSelected(articulo) }

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                } else {
                    finish()
                }
            }
        }
        asyncFun()
    }

    private fun regresaArticuloAActivity(articulo: Articulo) {
        val intent = Intent(this, articulo_alta_activity::class.java)

        intent.putExtra("idArticulo", articulo.IdArticulo)
        intent.putExtra("idCategoria", articulo.IdCategoria)
        intent.putExtra("nombre", articulo.Nombre)
        intent.putExtra("codigoBarra", articulo.CodigoBarra)
        intent.putExtra("idStatus", articulo.IdStatus)
        intent.putExtra("precioVenta", articulo.PrecioVenta)
        intent.putExtra("existencia", articulo.Existencia)
        intent.putExtra("idUnidadMedida", articulo.IdUnidadMedida)
        intent.putExtra("nombreFotoArchivo", articulo.NombreArchivoFoto)

        startForResult.launch(intent)
    }

    private fun onItemSelected(articulo: Articulo) {
        if (idArticulo > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Articulo modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    regresaArticuloAActivity(articulo)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Desea modificar Nombre:${articulo.Nombre} ?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    regresaArticuloAActivity(articulo)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
    }

    private fun muestraDatosArticuloConsultado(articulo: Articulo) {

    }
}