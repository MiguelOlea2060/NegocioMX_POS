package com.example.negociomx_pos

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.DAL.DALCategoria
import com.example.negociomx_pos.Utils.ParametrosSistema
import com.example.negociomx_pos.adapters.CategoriaAdapter
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.entities.Categoria
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class categoria_activity : AppCompatActivity() {

    lateinit var base: POSDatabase

    lateinit var txtNombre: EditText
    lateinit var chkPre: CheckBox
    lateinit var chkActiva: CheckBox
    lateinit var rv: RecyclerView
    lateinit var lblEncabezado: TextView
    var idCategoria: Int = 0
    lateinit var imgNueva: ImageView
    lateinit var dalCat: DALCategoria

    var categoriaNube: Boolean = false
    lateinit var listaCategorias: List<Categoria>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categoria)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        base = POSDatabase.getDatabase(applicationContext)
        categoriaNube = false
        dalCat = DALCategoria()

        txtNombre = findViewById(R.id.txtNombreCategoria)
        chkPre = findViewById(R.id.chkPredeterminadaCategoria)
        chkActiva = findViewById(R.id.chkActivaCategoria)
        lblEncabezado = findViewById(R.id.lblEncabezadoCategoria)
        rv = findViewById(R.id.rvCategoria)

        lblEncabezado.setText("Categoria -> Nueva")

        val btnGuardar = findViewById<ImageView>(R.id.btnGuardarCategoria)
        val btnCerrar = findViewById<Button>(R.id.btnCerrarCategoria)
        val btnTomarFoto = findViewById<Button>(R.id.btnTomarFotoCategoria)
        val btnRegresar = findViewById<ImageView>(R.id.btnRegresarCategoria)
        imgNueva = findViewById<ImageView>(R.id.btnNuevaCategoriaAlta)

        imgNueva.isVisible = false
        chkActiva.isChecked = true
        chkActiva.isEnabled = false

        txtNombre.requestFocus()
        btnTomarFoto.setOnClickListener {
            startForResult.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        btnRegresar.setOnClickListener {
            finish()
        }
        btnGuardar.setOnClickListener {
            guardaCategoria();
        }
        imgNueva.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Hay un Cliente modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    imgNueva.isVisible = false
                    limpiarControles()
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        }
        btnCerrar.setOnClickListener {
            finish()
        }

        if (intent.extras?.isEmpty == false) {
            categoriaNube = intent.extras?.getBoolean("categoriaNube", false)!!
        }

        muestraListaCategorias()
    }

    private fun muestraListaCategorias() {
        if (categoriaNube == false) {
            GlobalScope.launch {
                val lista = base.categoriaDAO().getAll(null)

                runOnUiThread {
                    lista.observe(this@categoria_activity) {
                        listaCategorias = arrayListOf()
                        listaCategorias = it

                        val adaptador = CategoriaAdapter(listaCategorias, { onItemSelected(it) })

                        rv.layoutManager = LinearLayoutManager(applicationContext)
                        rv.adapter = adaptador
                    }
                }
            }
        } else {
            var idEmpresa: String = ParametrosSistema.cfg.IdEmpresa.toString()

            var lista = dalCat.getAll(idEmpresa, null) { res ->
                if (res != null) {

                }
            }
        }
    }

    private fun onItemSelected(categoria: Categoria) {
        if (idCategoria > 0) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Pregunta")
            builder.setMessage("Categoria modificandose. Aun asi desea continuar?")
                .setPositiveButton("ACEPTAR", DialogInterface.OnClickListener { dialog, id ->
                    imgNueva.isVisible = true
                    muestraDatosCategoria(categoria)
                })
                .setNegativeButton("CANCELAR", { dialog, id ->

                })
            builder.show()
        } else
            muestraDatosCategoria(categoria)
    }

    private fun muestraDatosCategoria(categoria: Categoria) {
        lblEncabezado.setText("Categoria -> Edición")
        idCategoria = categoria.IdCategoria

        txtNombre.setText(categoria.Nombre)
        chkPre.isChecked = categoria.Predeterminada

        chkActiva.isEnabled = true
        chkActiva.isChecked = categoria.Activa
    }

    private fun guardaCategoria() {
        if (txtNombre.text.toString().length < 4) {
            txtNombre.error = "Es necesario suministrar un nombre minimo de 4 letras"
        } else {
            val scope = MainScope()

            val nombre = txtNombre.text.toString()
            val predeterminada = chkPre.isChecked
            val activa = chkActiva.isChecked

            fun asyncFun() = scope.launch {
                if (idCategoria <= 0) {
                    if (predeterminada)
                        base.categoriaDAO().updateAllPredeterminado(false)

                    base.categoriaDAO().insert(
                        Categoria(
                            Nombre = nombre,
                            Activa = activa,
                            Predeterminada = predeterminada,
                            Orden = 1,
                        )
                    )
                } else {
                    if (predeterminada)
                        base.categoriaDAO().updateAllPredeterminado(false)
                    base.categoriaDAO().update(
                        Categoria(
                            IdCategoria = idCategoria,
                            Nombre = nombre,
                            Activa = activa,
                            Predeterminada = predeterminada,
                            Orden = 1,
                        )
                    )
                }

                Toast.makeText(
                    applicationContext, "Se ha guardado el Categoria en el sistema",
                    Toast.LENGTH_LONG
                ).show()

                limpiarControles()
            }

            asyncFun()
        }
    }

    private fun limpiarControles() {
        idCategoria = 0
        txtNombre.text?.clear()

        chkActiva.isEnabled = false
        chkActiva.isChecked = true

        imgNueva.isVisible = false
        chkPre.isChecked = false
        lblEncabezado.setText("Categoria -> Nueva")

        txtNombre.requestFocus()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val imageBitmap = intent?.extras?.get("data") as Bitmap
                val imageFoto = findViewById<ImageView>(R.id.imgFotoCategoria)

                imageFoto.setImageBitmap(imageBitmap)
            }
        }
}