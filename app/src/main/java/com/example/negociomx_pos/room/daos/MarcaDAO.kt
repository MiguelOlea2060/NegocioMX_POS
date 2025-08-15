package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Marca

@Dao
interface MarcaDAO {
    @Insert
    suspend fun insert(marca: Marca)

    @Insert
    suspend fun insertAll(marcas: List<Marca>)
    {
        marcas.forEach{
            insert(it)
        }
    }

    @Update
    suspend fun update(marca: Marca)

    @Query("UPDATE Marca set Predeterminada=:predeterminada")
    suspend fun updateAllPredeterminado(predeterminada: Boolean)

    @Delete
    suspend fun delete(marca: Marca)

    @Query("SELECT * FROM Marca where :activa is null or Activa=:activa order by Predeterminada desc")
    fun getAll(activa:Boolean?): LiveData<List<Marca>>

    @Query("SELECT * FROM Marca where IdMarca=:idMarca")
    fun getById(idMarca:Short): LiveData<Marca>

    @Query("SELECT * FROM Marca where Nombre=:nombre order by Predeterminada desc")
    fun getByNombre(nombre:String): LiveData<Marca>
}