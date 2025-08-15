package com.example.negociomx_pos.BE

data class TipoPagoNube(
    var Id:String?=null,
    var IdLocal:String?=null,
    var Clave:String="",
    var Nombre:String="",
    var Pagado:Boolean=false,
    var ConBanco:Boolean=false,
    var Credito:Boolean=false,
    var DineroVirtual:Boolean=false,
    var Predeterminado:Boolean=false,
    var Activo:Boolean=false,
    var IdEmpresaNube:String?=null,
)
