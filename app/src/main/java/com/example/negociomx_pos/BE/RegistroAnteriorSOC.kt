package com.example.negociomx_pos.BE

data class RegistroAnteriorSOC(
    val IdPaso1LogVehiculo: Int = 0,
    val Vez: Short = 0,
    val Odometro: Int = 0,
    val Bateria: Int = 0,
    val ModoTransporte: Boolean = false,
    val RequiereRecarga: Boolean = false,
    val FechaAlta: String = "",
    val FotosPosicion1: Int = 0,
    val FotosPosicion2: Int = 0,
    val NombreArchivo1: String = "",
    val NombreArchivo2: String = "",
    val IdVehiculo: Int = 0,
    val VIN: String = ""
)