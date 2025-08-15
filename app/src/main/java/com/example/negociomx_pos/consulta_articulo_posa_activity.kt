package com.example.negociomx_pos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.negociomx_pos.adapters.ArticuloPOSAdapter
import com.example.negociomx_pos.room.daos.ArticuloPOS
import com.example.negociomx_pos.room.db.POSDatabase
import com.example.negociomx_pos.room.enums.TipoStatusArticuloEnum
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class consulta_articulo_posa_activity : AppCompatActivity() {
    private lateinit var base: POSDatabase

    lateinit var listaArticulos: List<ArticuloPOS>
    var nombreFilter: String = ""
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta_articulo_posa)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        base = POSDatabase.getDatabase(applicationContext)

        rv = findViewById(R.id.rvArticulosPOS)
        if (intent.extras?.isEmpty == false)
            nombreFilter = intent.extras?.getString("nombreFilter")!!

        muestraListaArticulos(nombreFilter)
    }

    private fun muestraListaArticulos(nombre: String) {
        val scope = MainScope()

        var nombreFilter: String = nombre
        if (nombre.isEmpty() == false)
            nombreFilter = "%" + nombre + "%"

        fun asyncFun() = scope.launch {
            var idStatus = TipoStatusArticuloEnum.Activo.value.toInt()
            val lista = base.articuloDAO().getAllByFilterClaveCodigoNombre(nombreFilter, idStatus)

            if (lista != null && lista.isNotEmpty() == true) {
                listaArticulos = arrayListOf()
                listaArticulos = lista

                if (lista.count() == 1) {
                    val art = lista.get(0)
                    regresaArticuloAActivityAnterior(art)
                } else {
                    val adaptador =
                        ArticuloPOSAdapter(listaArticulos) { articulo -> onItemSelected(articulo) }

                    rv.layoutManager = LinearLayoutManager(applicationContext)
                    rv.adapter = adaptador
                }
            } else {
                intent.putExtra("agrega", false)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
        asyncFun()
    }

    private fun regresaArticuloAActivityAnterior(articulo: ArticuloPOS) {
        val intent = Intent()
        intent.putExtra("idArticulo", articulo.IdArticulo)
        intent.putExtra("nombre", articulo.Nombre)
        intent.putExtra("cantidad", 1F)
        intent.putExtra("codigoBarra", articulo.CodigoBarra)
        intent.putExtra("precioVenta", articulo.PrecioVenta)
        intent.putExtra("agrega", true)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun onItemSelected(articulo: ArticuloPOS) {
        regresaArticuloAActivityAnterior(articulo)
    }
}