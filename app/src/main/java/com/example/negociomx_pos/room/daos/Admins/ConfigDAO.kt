package com.example.negociomx_pos.room.daos.Admins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Admins.Config

@Dao
interface ConfigDAO {
    @Insert
    suspend fun insert(cfg: Config)

    @Update
    suspend fun update(cfg: Config)

    @Query("UPDATE Config set Predeterminada=:predeterminada")
    suspend fun updateAllPredeterminada(predeterminada: Boolean)

    @Delete
    suspend fun delete(cfg: Config)

    @Query("SELECT * FROM Config")
    fun getAll(): LiveData<List<Config>>

    @Query("SELECT * FROM Config where (:idCfg is null or IdCfg=:idCfg) and (:idNube is null or IdNube=:idNube)" +
            " and (:idEmpresa is null or IdEmpresa=:idEmpresa) limit 1")
    fun getByFilters(idCfg:Int?, idNube:Int?, idEmpresa:String?): LiveData<Config>

    @Query("SELECT * FROM Config where IdNube=:idNube limit 1")
    fun getByIdNube(idNube:Int): LiveData<Config>

    @Query("SELECT * FROM Config where IdEmpresa=:idEmpresa")
    fun getByIdEmpresa(idEmpresa: Int): LiveData<Config>
}