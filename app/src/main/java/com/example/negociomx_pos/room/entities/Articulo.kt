package com.example.negociomx_pos.room.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "Articulo", foreignKeys = [ForeignKey(
    entity = Categoria::class, childColumns = ["IdCategoria"], parentColumns = ["IdCategoria"])])
data class Articulo (
    @PrimaryKey(autoGenerate = true)
    val IdArticulo:Int=0,
    var IdNube:Int?=null,
    val Clave:String="",
    val Nombre:String,
    val NombreCorto:String="",
    val IdUnidadMedida:Int=0,
    val CodigoBarra:String,
    val CodigoBarraPaquete:String="",
    val IdMarca:Int=1,
    val IdCategoria:Int=1,

    val Existencia:Float=0F,
    val Apartados:Float=0F,
    val CantidadBloqueada:Float=0F,
    val CantidadPiezasUnidad:Short=1,
    val CantidadPiezasUnidadVendidas:Short=0,
    val CantidadPiezasUnidadBloqueadas:Short=0,

    val ArticuloFicticio:Boolean=false,
    val PrecioCompra:Float=0F,
    val PrecioVenta:Float,
    val PrecioVentaPieza:Float=0F,
    val PrecioVentaMayoreo1:Float=0F,
    val PrecioVentaMayoreo2:Float=0F,
    val NombreArchivoFoto:String="",

    val IdStatus:Int,
    //val Foto:Blob?,
    val IdTipoProducto:Int=1,
    val ManejaCodigoBarra:Boolean,
    var IdEmpresa:Int?=null,
)