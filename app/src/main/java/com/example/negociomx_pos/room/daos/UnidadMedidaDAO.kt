package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.UnidadMedida

@Dao
interface UnidadMedidaDAO {
    @Insert
    suspend fun insert(unidadmedida:UnidadMedida)

    @Insert
    suspend fun insertAll(unidadesmedida:List<UnidadMedida>)
    {
        unidadesmedida.forEach{
            insert(it)
        }
    }

    @Update
    suspend fun update(unidadmedida: UnidadMedida)

    @Delete
    suspend fun delete(unidadmedida: UnidadMedida)

    @Query("SELECT * FROM UnidadMedida WHERE (:activa is null or Activa=:activa) and (:idEmpresa is null or IdEmpresa=:idEmpresa)")
    fun getAll(idEmpresa:Int?,activa:Boolean?):LiveData<List<UnidadMedida>>

    @Query("SELECT * FROM UnidadMedida where IdUnidadMedida=:idUnidadMedida")
    fun getById(idUnidadMedida:Short):LiveData<UnidadMedida>
}