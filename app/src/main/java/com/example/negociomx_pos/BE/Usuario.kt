package com.example.negociomx_pos.BE

data class Usuario(
    var IdUsuario:Int=0,
    var Email:String="",
    var NombreCompleto:String="",
    var Contrasena:String="",
    var fechaUltAcceso:String="",
    var Domicilio:String="",
    var IdRol:Int=0,
    var Activo:Boolean=false,
    var Bloqueado:Boolean=false,
    var NombreUsuario:String="",
    var IdEmpresa:Int?=null,
    var CuentaVerificada:Boolean=false,
    var FechaCuentaVerificada:String="",
    var IdUsuarioVerificoCuenta:Boolean=false,
    var IdCliente:Int=0
)
