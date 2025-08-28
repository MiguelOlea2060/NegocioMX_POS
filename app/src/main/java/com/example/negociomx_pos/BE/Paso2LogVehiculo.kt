package com.example.negociomx_pos.BE

data class Paso2LogVehiculo(
    var IdPaso2LogVehiculo: Int = 0,
    var IdVehiculo: Int = 0,
    var IdUsuarioNube: Int = 0,
    var FechaAltaFoto1: String? = null,
    var FechaAltaFoto2: String? = null,
    var FechaAltaFoto3: String? = null,
    var FechaAltaFoto4: String? = null,
    var Foto1: String? = null,
    var Foto2: String? = null,
    var Foto3: String? = null,
    var Foto4: String? = null,
    var TieneFoto1: Boolean = false,
    var TieneFoto2: Boolean = false,
    var TieneFoto3: Boolean = false,
    var TieneFoto4: Boolean = false,
    var NombreArchivoFoto1: String? = null,
    var NombreArchivoFoto2: String? = null,
    var NombreArchivoFoto3: String? = null,
    var NombreArchivoFoto4: String? = null
)
