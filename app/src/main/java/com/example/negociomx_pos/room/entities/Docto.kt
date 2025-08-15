package com.example.negociomx_pos.room.entities

data class Doc(
    val IdDoc:Int=0,
    val IdNube:Int?=null,
    val Folio:String,
    val IdCliente:Int=0,
    val IdTipoDocumento:Short=0,
    val Subtotal:Float,
    val IVA:Float,
    val Total:Float,

    val IdImpuesto:Int=0,
    val TasaImpuesto:Float,
    val IdTipoPago:Int,
    val CodigoBarra:String,
    val IdTipoDescuento:Byte=0,
    val IdDescuento:Int=0,
    val TasaDescuento:Float,
    val MontoDescuento:Float,

    val IdStatus: Int=0,
    val IdUsuarioAtendio:Int=0,
    val Pagado:Boolean,
    val Activo:Boolean,
    val Detalles:List<DocDet>?,
    var IdEmpresa:Int?=null,
    )

data class DocDet
(
    val IdDocDet:Int=0,
    val IdNube:Int?=null,
    val IdDoc:Int=0,
    val FolioDocumento:String="",
    val Consecutivo:Int?=0,
    val IdArticulo:Int?=0,
    val IdServicio:Int?=null,
    val NombreArticuloServicio:String?,
    val EsPiezaSuela:Boolean?=false,

    var Cantidad:Float?=0F,

    var PrecioUnitario:Float?=9F,
    var Importe:Float?=0F,

    val IdDescuento:Int?=0,
    val TasaDescuento:Float?=0F,
    val MontoDescuento:Float?=0F,
    val IdImpuesto:Int?=0,
    val TasaImpuesto:Float?=0F,
    val MontoImpuesto:Float?=0F,
    val PrecioCompra:Float?=0F,
)
