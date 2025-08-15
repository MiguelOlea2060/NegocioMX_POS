package com.example.negociomx_pos.room.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "Documento")
data class Documento (
    @PrimaryKey(autoGenerate = true)
    val IdDocumento:Int=0,
    val IdNube:Int?=null,
    val Folio:String="",
    val ConsecutivoFolio:Int=0,
    val PrefijoFolio:String="",
    val IdCliente:Int=0,
    val IdTipoDocumento:Int=0,
    val Subtotal:Float,
    val IVA:Float,
    val Total:Float,

    val FechaAlta:String="",
    val IdImpuesto:Int=0,
    val TasaImpuesto:Float,
    val IdTipoPago:Int,
    val CodigoBarra:String,
    val IdTipoDescuento:Byte=0,
    val IdDescuento:Int=0,
    val TasaDescuento:Float=0F,
    val MontoDescuento:Float=0F,

    val IdStatus:Int=0,
    val IdUsuarioAtendio:Int=0,
    val Pagado:Boolean=false,
    val Activo:Boolean=true
)

@Entity(tableName = "DocumentoDetalle")
data class DocumentoDetalle(
    @PrimaryKey(autoGenerate = true)
    val IdDocumentoDetalle:Int=0,
    val IdNube:Int?=null,
    val IdDocumento:Int=0,
    val Consecutivo:Int=0,
    val IdArticulo:Int=0,
    val EsPiezaSuela:Boolean=false,

    val Cantidad:Float=0F,

    val PrecioUnitario:Float=9F,
    val Importe:Float=0F,
    val IdServicio:Int?=null,

    val IdDescuento:Int=0,
    val TasaDescuento:Float=0F,
    val MontoDescuento:Float=0F,
    val IdImpuesto:Int=0,
    val TasaImpuesto:Float=0F,
    val MontoImpuesto:Float=0F,
    val PrecioCompra:Float=0F,
    val IdStatus:Int=0,
    var IdEmpresa:Int?=null,
)

data class DocyDetalles(
    @Embedded val Doc:Documento,
    @Relation(
        parentColumn = "IdDocumento",
        entityColumn = "IdDocumento"
    )
    val Detalles:List<DocumentoDetalle>
)