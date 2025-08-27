package com.example.negociomx_pos.BE

data class Vehiculo(
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
    var Activo: Boolean = true,
    var FechaCreacion: String = "",
    var FechaModificacion: String = "",
    var TipoCombustible:String="",
    var TipoVehiculo:String="",
    var BL:String="",

    // ✅ CAMPOS SOC (State of Charge)
    var Odometro: Int = 0,
    var Bateria: Int = 0,
    var ModoTransporte: Boolean = false,
    var RequiereRecarga: Boolean = false,
    var Evidencia1: String = "",
    var Evidencia2: String = "",
    var FechaActualizacion: String = ""

)
//DARA CLASE CREADA POR MIGUEL