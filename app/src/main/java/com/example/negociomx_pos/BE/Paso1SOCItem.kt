package com.example.negociomx_pos.BE

data class Paso1SOCItem(
    var IdVehiculo: Int = 0,
    var IdPaso1LogVehiculo: Int = 0,
    var VIN: String = "",
    var BL: String = "",
    var IdMarca: Int = 0,
    var Marca: String = "",
    var IdModelo: Int = 0,
    var Modelo: String = "",
    var Anio: Int = 0,
    var NumeroMotor: String = "",
    var IdColor: Int = 0,
    var IdColorInterior: Int = 0,
    var ColorExterior: String = "",
    var ColorInterior: String = "",

    // Datos SOC
    var Odometro: Int = 0,
    var Bateria: Int = 0,
    var ModoTransporte: Boolean = false,
    var RequiereRecarga: Boolean = false,
    var FechaAlta: String = "",
    var UsuarioAlta: String = "",
    var CantidadFotos: Int = 0
)
