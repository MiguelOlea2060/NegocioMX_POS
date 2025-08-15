package com.example.negociomx_pos.room.daos.Admins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Admins.Rol

@Dao
interface RolDAO {
    @Insert
    suspend fun insert(rol: Rol)

    @Update
    suspend fun update(rol: Rol)

    @Delete
    suspend fun delete(rol: Rol)

    @Query("SELECT * FROM Rol")
    fun getAll(): LiveData<List<Rol>>

    @Query("SELECT * FROM Rol where IdRol=:idRol")
    fun getById(idRol:Short): LiveData<Rol>
}