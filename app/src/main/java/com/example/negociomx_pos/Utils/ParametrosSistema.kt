package com.example.negociomx_pos.Utils

import com.example.negociomx_pos.BE.CfgNVNube
import com.example.negociomx_pos.BE.CfgNube
import com.example.negociomx_pos.BE.EmpresaNube
import com.example.negociomx_pos.BE.UsuarioNube
import com.example.negociomx_pos.room.entities.Admins.CfgNV
import com.example.negociomx_pos.room.entities.Admins.Config
import com.example.negociomx_pos.room.entities.Admins.Empresa
import com.example.negociomx_pos.room.enums.TipoDocumentoEnum
import com.example.negociomx_pos.room.enums.TipoUsoSistemaEnum
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ParametrosSistema {
    companion object{
        var firebaseAuth:FirebaseAuth= FirebaseAuth.getInstance()
        lateinit var firebaseUser:FirebaseUser
        lateinit var usuarioLogueado:UsuarioNube
        lateinit var empresaNube:EmpresaNube
        var empresaLocal:Empresa?=null
        lateinit var cfg:CfgNube
        var cfgLocal:Config?=null
        var TipoUsoSistema=TipoUsoSistemaEnum.OnLine
        lateinit var cfgNV:CfgNVNube
        var cfgNVLocal:CfgNV?=null
        val NombreBD:String="NEGOCIOMX-FB"
    }
}