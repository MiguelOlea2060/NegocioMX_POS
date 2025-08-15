package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val IdCliente:Int=0,
    var IdNube:Int?=null,
    val Nombre:String,
    val RazonSocial:String,
    val Rfc:String,
    val Email:String="",
    val Telefonos:String="",
    val CodigoPostal:Int=0,

    val DiasCredito:Short=0,
    val MontoCredito:Float=0F,
    val Contactos:String="",
    val Predeterminado:Boolean=false,
    val MontoAdeudo:Float=0F,
    val Activo:Boolean=true,
    var IdEmpresa:Int?=null,
)

