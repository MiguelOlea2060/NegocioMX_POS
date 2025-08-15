package com.example.negociomx_pos.BE



data class EmpresaNube(
    var Id:String?=null,
    var IdLocal:String?=null,
    var Rfc:String="",
    var RazonSocial:String="",
    var NombreComercial:String="",
    var Telefonos:String="",
    var Contactos:String="",
    var Email:String="",
    var PaginaWeb:String="",
    var CodigoPostal:Int=0,
    var IdRegimenFiscal:String?=null,
    var IdTipoContribuyente:String?=null,
    var Activa:Boolean=false,
    var Predeterminada:Boolean=false
)
