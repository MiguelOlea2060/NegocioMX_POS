package com.example.negociomx_pos.BE

data class Paso3LogVehiculo(
    var IdPaso3LogVehiculo: Int = 0,
    var IdVehiculo: Int = 0,
    var IdUsuarioNube: Int = 0,
    var FechaAlta: String = "",
    var Foto: String = "",
    var TieneFoto: Boolean = false,
    var NombreArchivoFoto: String = ""
)