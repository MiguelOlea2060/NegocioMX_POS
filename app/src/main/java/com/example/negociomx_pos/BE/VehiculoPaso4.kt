package com.example.negociomx_pos.BE

data class VehiculoPaso4(
    var Id: String = "",
    var VIN: String = "",
    var Marca: String = "",
    var Modelo: String = "",
    var Anio: Int = 0,
    var ColorExterior: String = "",
    var ColorInterior: String = "",
    var NumeroSerie: String = "",
    var IdEmpresa: String = "",
    var FechaCreacion: String = "",
    var FechaModificacion: String = "",
    var TipoCombustible:String="",
    var TipoVehiculo:String="",
    var BL:String="",

    var IdPaso4LogVehiculo:Int?=null,
    var Verificada1:Boolean?=null,
    var Verificada2:Boolean?=null,
    var Verificada3:Boolean?=null,
    var Verificada4:Boolean?=null,
    var TieneFoto1:Boolean?=null,
    var TieneFoto2:Boolean?=null,
    var TieneFoto3:Boolean?=null,
    var TieneFoto4:Boolean?=null,
)
