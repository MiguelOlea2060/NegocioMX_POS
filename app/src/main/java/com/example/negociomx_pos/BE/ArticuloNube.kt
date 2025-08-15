package com.example.negociomx_pos.BE

data class ArticuloNube (
    var Id:String?=null,
    var IdLocal:String?=null,
    var Nombre:String?=null,
    var Clave:String?=null,
    var NombreCorto:String?=null,
    var IdUnidadMedida:String?=null,
    var CodigoBarra:String?=null,
    var CodigoBarraPaquete:String?=null,
    var IdMarca:String?=null,
    var IdCategoria:String?=null,

    var Existencia:Float?=null,
    var Apartados:Float?=null,
    var CantidadBloqueada:Float?=null,
    var CantidadPiezasUnidad:Int?=null,
    var CantidadPiezasUnidadVendidas:Int?=null,
    var CantidadPiezasUnidadBloqueadas:Int?=null,

    var ArticuloFicticio:Boolean?=null,
    var PrecioCompra:Float?=null,
    var PrecioVenta:Float?=null,
    var PrecioVentaPieza:Float?=null,
    var PrecioVentaMayoreo1:Float?=null,
    var PrecioVentaMayoreo2:Float?=null,
    var NombreArchivoFoto:String?=null,

    var IdStatus:String?=null,
    var IdTipoProducto:Int?=null,
    var ManejaCodigoBarra:Boolean?=null,
    var IdEmpresa:String?=null,
)

data class ArticuloActNube(
    var Id:String?=null,
    var ActualizadoLocal:Boolean=false,
    var FechaActualizado:String?=null,
    var FechaAlta:String="",
    var IdArticulo:String?=null,
    var IdEmpresa:String?=null,
    var IdUsuario:String?=null
)