package com.example.negociomx_pos.room.daos.Admins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Admins.Empresa

@Dao
interface EmpresaDAO {
    @Insert
    suspend fun insert(empresa: Empresa)

    @Update
    suspend fun update(empresa: Empresa)

    @Delete
    suspend fun delete(empresa: Empresa)

    @Query("SELECT * FROM Empresa")
    fun getAll(): LiveData<List<Empresa>>

    @Query("SELECT e.* FROM Empresa e inner join Config c on e.IdEmpresa=c.IdEmpresa  where c.IdCfg=:idCfg")
    fun getByIdCfg(idCfg:Int): LiveData<Empresa>

    @Query("SELECT * FROM Empresa where (:idEmpresa is null or IdEmpresa=:idEmpresa) and (:idNube is null or IdNube=:idNube)")
    fun getByFilters(idEmpresa:Int?, idNube:Int?): LiveData<Empresa>
}