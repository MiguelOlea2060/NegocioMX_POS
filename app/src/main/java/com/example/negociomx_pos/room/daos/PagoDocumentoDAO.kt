package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PagoDocumentoDAO {
    @Insert
    suspend fun insert(pagoDocumentoDAO: PagoDocumentoDAO)

    @Update
    suspend fun update(pagoDocumentoDAO: PagoDocumentoDAO)

    @Delete
    suspend fun delete(pagoDocumentoDAO: PagoDocumentoDAO)

    @Query("SELECT * FROM Cfg")
    fun getAll(): LiveData<List<PagoDocumentoDAO>>

    @Query("SELECT * FROM PagoDocumento where IdPagoDocumento=:idPagoDocumento")
    fun getById(idPagoDocumento:Int): LiveData<PagoDocumentoDAO>

    @Query("SELECT IdCliente, CantidadSaldada, Folio, FechaPago FROM PagoDocumento where IdCliente=:idCliente")
    fun getAdeudoClienteDetalles(idCliente: Int):LiveData<List<ClienteAdeudos>>

    @Query("SELECT PrefijoFolio as Folio FROM PagoDocumento")
    fun getAdeudoClienteDetalles():LiveData<FolioPago>
}

data class ClienteAdeudos(
    val IdCliente:Int=0,
    val CantidadSaldada:Float=0F,
    val Folio:String="",
    val FechaPago:String,
)

data class FolioPago(val Folio:String)