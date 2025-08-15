package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Cliente
import java.time.LocalDateTime

@Dao
interface ClienteDAO {
    @Insert
    suspend fun insert(cliente: Cliente)

    @Update
    suspend fun update(cliente: Cliente)

    @Query("UPDATE Cliente set Predeterminado=:predeterminado")
    suspend fun updateAllPredeterminado(predeterminado: Int)

    @Delete
    suspend fun delete(cliente: Cliente)

    @Query("SELECT * FROM Cliente WHERE :activo is null OR Activo=:activo order by Predeterminado desc")
    fun getAll(activo:Boolean?): LiveData<List<Cliente>>

    @Query("SELECT * FROM Cliente WHERE (:activo is null OR Activo=:activo) order by Predeterminado desc")
    suspend fun getByFilters(activo:Boolean?): List<Cliente>

    @Query("SELECT * FROM Cliente where IdCliente=:idCliente")
    fun getById(idCliente: Int): LiveData<Cliente>

    @Query("SELECT * FROM Cliente where Nombre=:nombre and rfc=:rfc")
    fun getByNombreAndRfc(nombre:String, rfc:String): LiveData<Cliente>
}

