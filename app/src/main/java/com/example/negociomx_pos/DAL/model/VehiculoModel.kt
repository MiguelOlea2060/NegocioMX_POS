package com.example.negociomx_pos.DAL.model

data class VehiculoModel(
    var IdVehiculo: Int=0,
    var Annio:Short=0,
    var Activo:Byte=0,

    var Vin:String="",
    var IdMarca:Int=0,
    var NombreMarca:String="",
    var IdModelo:Int=0,
    var NombreModelo:String="",
    var Motor:String=""
)
