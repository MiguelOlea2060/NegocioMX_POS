package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.TipoPago

@Dao
interface TipoPagoDAO {
    @Insert
    suspend fun insert(tipoPago: TipoPago)

    @Insert
    suspend fun insertAll(tiposPago: List<TipoPago>)
    {
        tiposPago.forEach{
            insert(it)
        }
    }

    @Update
    suspend fun update(tipoPago: TipoPago)

    @Query("UPDATE TipoPago set Predeterminado=:predeterminada")
    suspend fun updateAllPredeterminado(predeterminada: Boolean)

    @Delete
    suspend fun delete(tipoPago: TipoPago)

    @Query("SELECT * FROM TipoPago WHERE (:activo is null OR Activo=:activo) order by Predeterminado desc")
    fun getAll(activo:Boolean?): LiveData<List<TipoPago>>

    @Query("SELECT * FROM TipoPago WHERE (:activo is null OR Activo=:activo) order by Predeterminado desc")
    suspend fun getByFilters(activo:Boolean?): List<TipoPago>

    @Query("SELECT * FROM TipoPago where IdTipoPago=:idTipoPago")
    fun getById(idTipoPago:Short): LiveData<TipoPago>
}