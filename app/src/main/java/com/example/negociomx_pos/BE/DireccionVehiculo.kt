package com.example.negociomx_pos.BE


data class DireccionVehiculo(
    var IdDireccionVehiculo: Int = 0,
    var Nombre: String = "",
    var Activo: Boolean = true
) {
    override fun toString(): String {
        return Nombre
    }
}
//Data class modificada por miguel