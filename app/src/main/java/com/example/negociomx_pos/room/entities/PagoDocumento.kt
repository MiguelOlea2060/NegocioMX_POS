package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "PagoDocumento")
data class PagoDocumento(
    @PrimaryKey(autoGenerate = true)
    val IdPagoDocumento:Int=0,
    val IdNube:Int?=null,
    val IdDocumento:Int=0,
    val IdTipoDocumento:Int=0,
    val IdTipoDocumentoRelacionado:Int?=null,
    val IdDocumentoRelacionado:Int?=null,
    val IdCliente:Int?=null,
    val IdBanco:Int=0,
    val Descripcion:String="",
    val Consecutivo:Int=0,

    val ConsecutivoFolio:Int=0,
    val PrefijoFolio:String="",
    val Folio:String="",
    val Recibo:Boolean=false,
    val ImporteSaldoAnterior:Float=0F,
    val ImporteSaldoInsoluto:Float=0F,
    val PagoPadre:Boolean=false,
    val IdMetodoPago:Int=0,
    val Cantidad:Float?=null,
    val ValorUnitario:Float?=null,
    val Importe:Float?=null,
    val Activo:Boolean=false,
    val IdObservacionCancelacion:Int?=null,
    val ImporteSaldoInsolutoCliente:Float=0F,
    val IdPagoUnico:Int?=null,
    val MontoTotalPagos:Float?=null,
    val Version:Float?=null,

    val IdMoneda:Int?=null,
    val IdTipoPago:Int=0,
    val IdTipoFormaPago:Int?=null,
    val CantidadSaldada:Float=0F,

    val FechaPago:String?=null,
    val IdUsuarioAQuePago:Int=0,
    val IdUsuarioCancelo:Int?=null,
    var IdEmpresa:Int?=null
)