package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.negociomx_pos.room.entities.DocDet
import com.example.negociomx_pos.room.entities.Documento
import com.example.negociomx_pos.room.entities.DocumentoDetalle
import com.example.negociomx_pos.room.entities.DocyDetalles

@Dao
interface DocumentoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documento: Documento):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detalle: DocumentoDetalle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(detalles: List<DocumentoDetalle>)
    {
        detalles.forEach{
            insert(it)
        }
    }

    @Update
    suspend fun update(documento: Documento)

    @Update
    suspend fun update(detalle: DocumentoDetalle)

    @Update
    suspend fun updateAll(detalles:List<DocumentoDetalle>)
    {
        detalles.forEach {
            update(it)
        }
    }

    @Delete
    suspend fun delete(documento: Documento)

    @Delete
    suspend fun delete(detalle: DocumentoDetalle)

    @Query("SELECT * FROM Documento d left join DocumentoDetalle dd on d.IdDocumento=dd.IdDocumento")
    fun getAll(): LiveData<List<Documento>>

    @Transaction
    @Query("SELECT * FROM Documento WHERE IdDocumento = :idDocumento")
    fun getById(idDocumento: Int): LiveData<List<DocyDetalles>>

    @Transaction
    @Query("SELECT d.IdDocumento, c.Nombre as NombreCliente, d.Folio, d.Subtotal, d.IVA, d.Total, d.Activo, " +
            "(select count(*) from documentodetalle where IdDocumento=d.IdDocumento) as NumArticulos" +
            " FROM Documento d inner join Cliente c on d.IdCliente=c.IdCliente")
    fun getAllDoctos(): LiveData<List<DoctoVenta>>

    @Query("UPDATE Articulo SET Existencia=(Existencia - :Cantidad) WHERE IdArticulo=:idArticulo")
    suspend fun updateExistenciaArticulo(idArticulo:Int, Cantidad:Float)

    @Query("select  max(ConsecutivoFolio) from Documento ")
    suspend fun getLastConsecutivoFolioDocumento():Int?

    @Transaction
    @Query("SELECT dd.IdDocumentoDetalle IdDocDet, dd.IdNube, d.IdDocumento IdDoc, dd.Consecutivo, dd.IdArticulo, dd.IdServicio," +
            " a.Nombre NombreArticuloServicio, dd.EsPiezaSuela, dd.Cantidad, dd.PrecioUnitario, dd.Importe, dd.IdDescuento," +
            "dd.TasaDescuento, dd.MontoDescuento, dd.IdImpuesto, dd.TasaImpuesto, dd.MontoImpuesto, dd.PrecioCompra, d.IdStatus," +
            "d.Folio FolioDocumento " +
            " FROM Documento d left join DocumentoDetalle dd on d.IdDocumento=dd.IdDocumento " +
            " left join articulo a on dd.IdArticulo=a.IdArticulo left join Cliente c on d.IdCliente=c.IdCliente" +
            " WHERE (d.IdUsuarioAtendio=:idUsuario or :idUsuario is null) and (:idDocumento is null or d.IdDocumento=:idDocumento)" +
            " and (:idStatus is null or d.IdStatus=:idStatus)" +
            " order by d.IdDocumento asc")
    suspend fun getAllDocDet(idDocumento:Int?, idUsuario:Int?, idStatus:Int?): List<DocDet>
}

data class DoctoVenta(val IdDocumento:Int, val NombreCliente:String, val Folio:String, val NumArticulos:Float, val Subtotal:Float,
                       val IVA:Float, val Total:Float, val Activo:Boolean)

data class DoctoDetallesVenta(val IdDocumento:Int, val NombreCliente:String, val Folio:String, val NumArticulos:Int, val Subtotal:Float,
                      val Iva:Float, val Total:Float, val Activo:Boolean)




