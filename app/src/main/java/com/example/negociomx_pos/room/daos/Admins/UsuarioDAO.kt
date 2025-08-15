package com.example.negociomx_pos.room.daos.Admins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Admins.Usuario

@Dao
interface UsuarioDAO {
    @Insert
    suspend fun insert(usuario: Usuario)

    @Update
    suspend fun update(usuario: Usuario)

    @Delete
    suspend fun delete(usuario: Usuario)

    @Query("SELECT * FROM Cfg")
    fun getAll(): LiveData<List<Usuario>>

    @Query("SELECT * FROM Usuario where IdUsuario=:idUsuario")
    fun getById(idUsuario:Short): LiveData<Usuario>
}