package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UnidadMedida")
data class UnidadMedida(
    @PrimaryKey(autoGenerate = true)
    val IdUnidadMedida:Int=0,
    val IdNube:Int?=null,
    val Nombre:String,
    val Abreviatura:String,
    val Activa:Boolean,
    var IdEmpresa:Int?=null,
)