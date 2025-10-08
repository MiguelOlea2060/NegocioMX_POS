package com.example.negociomx_pos.BE

data class CfgApp(
    var IdCfgApp:Short?=null,
    var IdConfiguracion:Int=0,
    var ManejaSeleccionBloquePosXTablero:Boolean=false,
    var IdEmpresa:Int?=null,
    var RfcEmpresa:String="",

    ////Esto se manejara para guardar los archivos fotos o lo que sea dentro de BD
    var ManejaGuardadoArchivosEnBD:Boolean?=null,
    var ManejaSeleccionObsMovimientoLocal:Boolean?=null,
    var ManejaSeleccionObsEnTaller:Boolean?=null,
    var FormatoCarpetaArchivos:String="",
    var UrlGuardadoArchivos:String="",
    var ReglasNotificaciones :String="",
    var UrlAPIControllerGuardadoArchivos:String="",
    var UrlAPIControllerVisualArchivos:String="",
    var CarpetaGuardadoArchivosNube:String=""
)
