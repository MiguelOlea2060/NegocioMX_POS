package com.example.negociomx_pos.room.entities.Admins

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [ForeignKey(entity = Empresa::class, parentColumns = ["IdEmpresa"], childColumns = ["IdEmpresa"])])
data class Config(
    @PrimaryKey(autoGenerate = true)
    var IdCfg: Int = 0,
    var IdNube: Int? = null,
    var IdCfgLocal: String? = null,
    var IdEmpresa: Int? = null,
    var Predeterminada: Boolean = false,
    var ConsecutivoFolioPago: Int = 0,
    var PrefijoFolioPago: String = "",
    var NombreTipoPagoPredeterminado: String? = null,
    var Activa: Boolean? = null,
)