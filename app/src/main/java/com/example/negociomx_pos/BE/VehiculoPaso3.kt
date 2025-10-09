package com.example.negociomx_pos.BE

data class VehiculoPaso3(
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

    var IdVehiculo:Int?=null,
    var IdPaso3LogVehiculo:Int=0,
    var TieneFoto:Boolean=false,
    var NombreArchivoFoto:String="",
    var FechaAltaFoto:String="",

)
