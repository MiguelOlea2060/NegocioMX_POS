package com.example.negociomx_pos.BE

data class VehiculoResumen(
    var IdVehiculo: Int = 0,
    var VIN: String = "",
    var BL: String = "",
    var Marca: String = "",
    var Modelo: String = "",
    var Anio: String = "",
    var ColorExterior: String = "",
    var ColorInterior: String = "",
    var TipoCombustible: String = "",
    var NumeroMotor: String = "",

    // Datos de tiempo registrado
    var FechaPrimerRegistro: String = "",
    var DiasRegistrado: Int = 0,
    var AniosRegistrado: Int = 0,
    var MesesRegistrado: Int = 0,
    var SemanasRegistrado: Int = 0,
    var DiasRestantes: Int = 0,

    // Datos SOC (Paso 1)

    var Odometro: Int = 0,
    var Bateria: Int = 0,
    var FechaPrimerSOC: String = "",

    // Datos Accesorios (Paso 2)
    var TieneAccesorios: Boolean = false,
    var CantidadFotosAccesorios: Int = 0,
    var FechaPrimerAccesorio: String = "",

    // Datos REPUVE (Paso 3)
    var TieneRepuve: Boolean = false,
    var CantidadFotosRepuve: Int = 0,
    var FechaPrimerRepuve: String = ""
)