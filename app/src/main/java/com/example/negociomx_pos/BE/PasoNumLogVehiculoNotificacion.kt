package com.example.negociomx_pos.BE

import java.util.Date

data class PasoNumLogVehiculoNotificacion(
    val IdPasoNumLogVehiculoNotificacion: Int = 0,
    val IdPasoNumLogVehiculo: Int = 0,
    val IdVehiculo: Int = 0,
    val Paso: Short = 0,
    val Posicion: Short = 0,
    val Vez: Short = 0,
    val FechaAlta: Date? = null,
    val FechaActividad: Date? = null,
    val FechaRealizada: Date? = null,
    val Realizada: Boolean? = null,
    val FechaVisto: Date? = null,
    val Visto: Boolean? = null,
    val IdUsuarioAlta: Int = 0,
    val IdUsuarioRealizada: Int? = null,
    val IdUsuarioVisto: Int? = null,
    val Activo: Boolean = true,
    val Vin: String = ""
)