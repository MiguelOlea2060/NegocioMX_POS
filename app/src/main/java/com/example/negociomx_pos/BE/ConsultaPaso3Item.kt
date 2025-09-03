package com.example.negociomx_pos.BE

data class ConsultaPaso3Item(
    var IdVehiculo: Int = 0,
    var IdPaso3LogVehiculo: Int = 0,
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

    // Datos específicos de Paso3
    var FechaAlta: String = "",
    var NombreArchivoFoto: String = "",
    var TieneFoto: Boolean = false,
    var IdUsuarioNube: Int = 0
)