package com.example.negociomx_pos.BE

data class Marca(
    var IdMarcaAuto: Int = 0,
    var Nombre: String = ""
) {
    override fun toString(): String {
        return Nombre
    }
}
//DATA CLASE CREADA POR MIGUEL