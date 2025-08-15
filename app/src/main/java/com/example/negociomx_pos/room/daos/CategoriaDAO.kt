package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Categoria

@Dao
interface CategoriaDAO {
    @Insert
    suspend fun insert(categoria: Categoria)

    @Insert
    suspend fun insertAll(categorias:List<Categoria>)
    {
        categorias.forEach{
            insert(it)
        }
    }

    @Update
    suspend fun update(categoria: Categoria)

    @Query("UPDATE Categoria set Predeterminada=:predeterminada")
    suspend fun updateAllPredeterminado(predeterminada: Boolean)

    @Delete
    suspend fun delete(categoria: Categoria)

    @Query("SELECT * FROM Categoria where :activa is null or Activa=:activa order by Predeterminada desc")
    fun getAll(activa:Boolean?): LiveData<List<Categoria>>

    @Query("SELECT * FROM Categoria where IdCategoria=:idCategoria")
    fun getById(idCategoria:Short): LiveData<Categoria>

    @Query("SELECT * FROM Categoria where Nombre=:nombre order by Predeterminada desc")
    fun getByNombre(nombre:String): LiveData<Categoria>
}