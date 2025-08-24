package com.example.negociomx_pos.BE

data class Transmision(
    var IdTransmision: Int = 0,
    var Nombre: String = "",
    var Activo: Boolean = true
) {
    override fun toString(): String {
        return Nombre
    }
}
//Data clase modificadad por miguel