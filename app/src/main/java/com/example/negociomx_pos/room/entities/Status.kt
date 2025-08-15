package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Status(
    @PrimaryKey(autoGenerate = true)
    val IdStatus:Int=0,
    val IdNube:Int?=null,
    val Nombre:String
)
