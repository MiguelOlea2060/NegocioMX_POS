package com.example.negociomx_pos.DAL.model

data class VehiculoPlacaModel(
    var IdVehiculoPlacas:Int=0,
    var IdVehiculo: Int=0,
    var Placas:String="",
    var Annio:Short=0,
    var Activo:Byte=0,

    var Vin:String="",
    var IdMarca:Int=0,
    var NombreMarca:String="",
    var IdModelo:Int=0,
    var NombreModelo:String="",
    var Motor:String=""
)
