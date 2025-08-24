package com.example.negociomx_pos.BE


data class Modelo(
    var IdModelo: Int = 0,
    var Nombre: String = "",
    var IdMarcaAuto: Int = 0,
    var Activo: Boolean = true
) {
    override fun toString(): String {
        return Nombre
    }
}
//DATA CLASE MODIFICADA POR MIGUEL
