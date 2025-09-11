package com.example.negociomx_pos.BE

data class ConsultaPaso4Item(
    var IdVehiculo: Int = 0,
    var IdPaso4LogVehiculo: Int = 0,
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

    // Datos específicos de Paso4 - Llantas
    var FechaAlta: String = "",
    var CantidadLlantas: Int = 0,
    var LlantasVerificadas: Int = 0,
    var LlantasConFoto: Int = 0,
    var IdUsuarioNube: Int = 0,

    // Detalles por posición
    var Llanta1Verificada: Boolean = false,
    var Llanta1TieneFoto: Boolean = false,
    var Llanta2Verificada: Boolean = false,
    var Llanta2TieneFoto: Boolean = false,
    var Llanta3Verificada: Boolean = false,
    var Llanta3TieneFoto: Boolean = false,
    var Llanta4Verificada: Boolean = false,
    var Llanta4TieneFoto: Boolean = false
)
