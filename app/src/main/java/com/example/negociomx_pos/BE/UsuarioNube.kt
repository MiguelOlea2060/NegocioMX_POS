package com.example.negociomx_pos.BE

data class UsuarioNube(
    var Id: String? = null,
    var IdLocal: String? = null,
    var NombreCompleto: String? = null,
    var Email: String? = null,
    var Password: String? = null,
    var IdRol: String? = null,
    var IdEmpresa: String? = null,
    var Activo: Boolean? = null,
    var CuentaVerificada: Boolean? = null,
    var RazonSocialEmpresa: String? = null,
    var NombreCuentaVerificada: String? = null,
    var RfcEmpresa: String? = null
)
//DATA CLASE CREADA POR MIGUEL