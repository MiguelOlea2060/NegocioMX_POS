package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Impuesto(
    @PrimaryKey(autoGenerate = true)
    val IdImpuesto:Int=0,
    val IdNube:Int?=null,

    val IdTipoImpuesto:Short=0,
    val Nombre:String="",
    val Clave:String="",
    val Tasa:Float=0F,
    val Activo:Boolean=true,
    val Predeterminado:Boolean=false,
    var IdEmpresa:Int?=null,
)
