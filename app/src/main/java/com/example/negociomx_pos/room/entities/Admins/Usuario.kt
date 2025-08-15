package com.example.negociomx_pos.room.entities.Admins

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Rol::class, parentColumns = ["IdRol"], childColumns = ["IdRol"])])
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val IdUsuario:Int=0,
    val IdNube:Int?=null,
    val IdRol:Int=0,
    val NombreUsuario:String="",
    val Password:String="",
    val NombreCompleto:String="",
    val Email:String="",
    val Activo:Boolean=false,
    val Bloqueado:Boolean=false,

    val CuentaBiometricoCell:Boolean=false,
    val CuentaVerificada:Boolean?=null,
    val IdUsuarioVerificoCuenta:Int?=null,
    val FechaCuentaVerificada:String="",

    val IdCliente:Int=0,
    val IdEmpresa:Int?=null,
    val PIN:Int=0
)
