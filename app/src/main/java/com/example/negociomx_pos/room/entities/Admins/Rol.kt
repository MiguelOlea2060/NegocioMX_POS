package com.example.negociomx_pos.room.entities.Admins

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Rol(
    @PrimaryKey(autoGenerate = true)
    val IdRol:Int=0,
    val IdNube:Int?=null,
    val Nombre:String="",
    val Activo:Boolean=false
)
