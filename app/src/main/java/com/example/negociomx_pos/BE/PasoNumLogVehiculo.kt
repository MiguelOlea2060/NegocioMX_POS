package com.example.negociomx_pos.BE

data class PasoNumLogVehiculo(
    var Paso:Int=0,
    var IdVehiculo: Int = 0,
    var IdPasoNumLogVehiculo: Int = 0,
    var VIN: String = "",
    var BL: String = "",
    var IdMarca: Int = 0,
    var Marca: String = "",
    var IdModelo: Int = 0,
    var Modelo: String = "",
    var Anio: Int = 0,
    var IdColor: Int = 0,
    var IdColorInterior: Int = 0,
    var ColorExterior: String = "",
    var ColorInterior: String = "",

    var NombreArchivoFoto1:String="",
    var NombreArchivoFoto2:String="",
    var NombreArchivoFoto3:String="",
    var NombreArchivoFoto4:String="",

    var CantidadFotos: Int = 0,

    var FechaAltaFoto1:String="",
    var FechaAltaFoto2:String="",
    var FechaAltaFoto3:String="",
    var FechaAltaFoto4:String="",

    var TieneFoto1:Boolean=false,
    var TieneFoto2:Boolean=false,
    var TieneFoto3:Boolean=false,
    var TieneFoto4:Boolean=false,
)
