package com.example.negociomx_pos.BE

data class VehiculoPaso1(
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
    var FechaActualizacion: String = "",

    var FechaAltaPaso1:String="",
    var IdPaso1LogVehiculo:Int?=null,

    var FotosPosicion1: Int = 0,
    var FotosPosicion2: Int = 0,
    var FotosPosicion3: Int = 0,
    var FotosPosicion4: Int = 0,

    var NombreArchivo1:String="",
    var NombreArchivo2:String="",
    var NombreArchivo3:String="",
    var NombreArchivo4:String="",

    var Vez:Short?=null,
    var IdPasoNumLogVehiculoNotificacion:Int?=null,
    var IdPasoNumLogVehiculo:Int?=null,
)
