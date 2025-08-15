package com.example.negociomx_pos.BE

data class DispositivoAcceso(
    var IdDispositivo:String?=null,
    var FechaAlta:String?=null,
    var IdEmpresa:String?=null,
    var Imei:String?=null,
    var Activo:Boolean?=null
)

data class Intento(
    var Id: String?=null,
    var IdDispositivo: String?=null,
    var FechaIntento: String?=null
)

data class EmpresaDispositivoAcceso(
    var Id:String?=null,
    var IdEmpresa:String?=null,
    var IdDispositivoAcceso:String?=null,
)

data class DispositivoAccesoUnico(
    var IdDispositivo:String?=null,
    var FechaAlta:String?=null,
    var IdEmpresa:String?=null,
    var Imei:String?=null,
    var Activo:Boolean?=null,
    var CantidadLogueos:Int=0,
    var NombreEmpresa:String?=null
)