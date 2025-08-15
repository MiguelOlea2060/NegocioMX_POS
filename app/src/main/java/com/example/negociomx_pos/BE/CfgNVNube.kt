package com.example.negociomx_pos.BE

data class CfgNVNube(
    var IdCfgNV:String?=null,
    var IdCfgNVLocal:String?=null,
    var IdCfg:String?=null,

    var ConsecutivoFolioNV:Int=0,
    var PrefijoFolioNV:String="",
    var IdTipoPagoPredeterminado:String?=null,
    var NombreTipoPagoPredeterminado:String?=null,
)
