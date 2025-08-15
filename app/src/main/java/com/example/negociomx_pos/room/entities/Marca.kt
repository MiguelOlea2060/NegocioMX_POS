package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Marca(
    @PrimaryKey(autoGenerate = true)
    val IdMarca:Int=0,
    val IdNube:Int?=null,
    val Nombre:String,
    val Activa:Boolean,
    val Predeterminada:Boolean,
    var SinMarca:Boolean?=null,
    var IdEmpresa:Int?=null,
)
