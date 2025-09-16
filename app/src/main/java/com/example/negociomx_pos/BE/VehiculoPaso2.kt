package com.example.negociomx_pos.BE

data class VehiculoPaso2(
    var Id: String = "",
    var VIN: String = "",
    var Marca: String = "",
    var Modelo: String = "",
    var Anio: Int = 0,
    var ColorExterior: String = "",
    var ColorInterior: String = "",
    var Placa: String = "",
    var NumeroSerie: String = "",
    var IdEmpresa: String = "",
    var FechaCreacion: String = "",
    var FechaModificacion: String = "",
    var TipoCombustible:String="",
    var TipoVehiculo:String="",
    var BL:String="",

    var IdPaso2LogVehiculo:Int?=null,
    var TieneFoto1:Boolean?=null,
    var TieneFoto2:Boolean?=null,
    var TieneFoto3:Boolean?=null,
    var TieneFoto4:Boolean?=null,
    var NombreArchivoFoto1:String="",
    var NombreArchivoFoto2:String="",
    var NombreArchivoFoto3:String="",
    var NombreArchivoFoto4:String=""
)
