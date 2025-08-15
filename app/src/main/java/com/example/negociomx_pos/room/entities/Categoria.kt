package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Categoria")
data class Categoria(
    @PrimaryKey(autoGenerate = true)
    val IdCategoria:Int=0,
    val IdNube:Int?=null,
    val Nombre:String,
    val Activa:Boolean,
    val Predeterminada:Boolean,
    val Orden:Short=0,
    var IdEmpresa:Int?=null,
)