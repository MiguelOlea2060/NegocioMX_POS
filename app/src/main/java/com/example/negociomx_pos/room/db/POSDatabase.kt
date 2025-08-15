package com.example.negociomx_pos.room.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.negociomx_pos.room.daos.Admins.ConfigDAO
import com.example.negociomx_pos.room.daos.Admins.CfgNVDAO
import com.example.negociomx_pos.room.daos.Admins.EmpresaDAO
import com.example.negociomx_pos.room.daos.ArticuloDAO
import com.example.negociomx_pos.room.daos.CategoriaDAO
import com.example.negociomx_pos.room.daos.ClienteDAO
import com.example.negociomx_pos.room.daos.DocumentoDAO
import com.example.negociomx_pos.room.daos.ImpuestoDAO
import com.example.negociomx_pos.room.daos.MarcaDAO
import com.example.negociomx_pos.room.daos.TipoPagoDAO
import com.example.negociomx_pos.room.daos.UnidadMedidaDAO
import com.example.negociomx_pos.room.entities.Admins.CfgNV
import com.example.negociomx_pos.room.entities.Admins.Config
import com.example.negociomx_pos.room.entities.Admins.Empresa
import com.example.negociomx_pos.room.entities.Articulo
import com.example.negociomx_pos.room.entities.Categoria
import com.example.negociomx_pos.room.entities.Cliente
import com.example.negociomx_pos.room.entities.Documento
import com.example.negociomx_pos.room.entities.DocumentoDetalle
import com.example.negociomx_pos.room.entities.Impuesto
import com.example.negociomx_pos.room.entities.Marca
import com.example.negociomx_pos.room.entities.PagoDocumento
import com.example.negociomx_pos.room.entities.TipoPago
import com.example.negociomx_pos.room.entities.UnidadMedida

@Database(entities = [Empresa::class, UnidadMedida::class, Categoria::class, Articulo::class, Cliente::class, Documento::class,
    DocumentoDetalle::class, Impuesto::class, TipoPago::class, Config::class,CfgNV::class, PagoDocumento::class, Marca::class],
    version = 1)
abstract class POSDatabase:RoomDatabase() {
    abstract fun unidadMedidaDAO():UnidadMedidaDAO
    abstract fun categoriaDAO():CategoriaDAO
    abstract fun articuloDAO():ArticuloDAO
    abstract fun clienteDAO():ClienteDAO
    abstract fun documentoDAO():DocumentoDAO
    abstract fun impuestoDAO():ImpuestoDAO
    abstract fun tipoPagoDAO():TipoPagoDAO
    abstract fun configDAO():ConfigDAO
    abstract fun cfgNVDAO():CfgNVDAO
    abstract fun empresaDAO():EmpresaDAO
    abstract fun marcaDAO(): MarcaDAO

    companion object{
        fun getDatabase(ctx:Context):POSDatabase
        {
            val db = Room.databaseBuilder(ctx, POSDatabase::class.java, "NegocioMX").build()
            return db
        }
    }
}