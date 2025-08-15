package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Impuesto

@Dao
interface ImpuestoDAO {
    @Insert
    suspend fun insert(impuesto: Impuesto)

    @Update
    suspend fun update(impuesto: Impuesto)

    @Delete
    suspend fun delete(impuesto: Impuesto)

    @Query("SELECT * FROM Impuesto")
    fun getAll(): LiveData<List<Impuesto>>

    @Query("SELECT * FROM Impuesto where IdImpuesto=:idImpuesto")
    fun getById(idImpuesto:Short): LiveData<Impuesto>
}