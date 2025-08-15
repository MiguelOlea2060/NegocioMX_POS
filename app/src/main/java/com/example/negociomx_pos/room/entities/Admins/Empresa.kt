package com.example.negociomx_pos.room.entities.Admins

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Empresa(
    @PrimaryKey(autoGenerate = true)
    var IdEmpresa:Int=0,
    var IdNube:Int?=null,
    var Rfc:String="",
    var RazonSocial:String="",
    var NombreComercial:String="",
    var Telefonos:String="",
    var Contactos:String="",
    var Email:String="",
    var PaginaWeb:String="",
    var CodigoPostal:Int=0,
    var IdRegimenFiscal:Short=0,
    var IdTipoContribuyente:Short=0,
    var Activa:Boolean=false,
    var Predeterminada:Boolean=false
)
