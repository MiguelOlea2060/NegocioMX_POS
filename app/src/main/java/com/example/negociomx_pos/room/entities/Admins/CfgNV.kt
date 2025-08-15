package com.example.negociomx_pos.room.entities.Admins

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity =Config::class, parentColumns = ["IdCfg"], childColumns = ["IdCfg"])])
data class CfgNV(
    @PrimaryKey(autoGenerate = true)
    var IdCfgNV:Int=0,
    var IdNube:Int?=null,
    var IdCfgNVLocal:Int?=null,
    var IdCfg:String?=null,

    var ConsecutivoFolioNV:Int=0,
    var PrefijoFolioNV:String="",
    var IdTipoPagoPredeterminado:String?=null,
    var NombreTipoPagoPredeterminado:String?=null,
)
