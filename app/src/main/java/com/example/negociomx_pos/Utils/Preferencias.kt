package com.example.negociomx_pos.Utils

import android.content.Context

class Preferencias(var context: Context) {
    val SHARED_NAME="DatosLocales"
    val SHARED_USERNAME="NombreUsuario"
    val SHARED_PWD="Password"
    val SHARED_RECORDARCCESO="RecordarAcceso"

    val storage=context.getSharedPreferences(SHARED_NAME,0)

    fun saveUsername(name: String)
    {
        storage.edit().putString(SHARED_USERNAME, name).apply()
    }
    fun savePassword(password:String)
    {
        storage.edit().putString(SHARED_PWD,password).apply()
    }
    fun saveRecordarAcceso(recordar:Boolean)
    {
        storage.edit().putBoolean(SHARED_RECORDARCCESO,recordar).apply()
    }

    fun getUsername():String
    {
        return storage.getString(SHARED_USERNAME,"")!!
    }
    fun getRecordarAcceso():Boolean
    {
        return storage.getBoolean(SHARED_RECORDARCCESO,false)
    }
    fun getPassword():String
    {
        return storage.getString(SHARED_PWD,"")!!
    }
}