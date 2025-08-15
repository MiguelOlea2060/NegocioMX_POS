package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TipoPago(
    @PrimaryKey(autoGenerate = true)
    var IdTipoPago:Int=0,
    var IdNube:Int?=null,
    var Clave:String="",
    var Nombre:String="",
    var Pagado:Boolean=false,
    var ConBanco:Boolean=false,
    var Credito:Boolean=false,
    var DineroVirtual:Boolean=false,
    var Predeterminado:Boolean=false,
    var Activo:Boolean=false,
    var IdEmpresa:Int?=null,
)
