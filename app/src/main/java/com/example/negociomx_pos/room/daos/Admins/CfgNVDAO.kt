package com.example.negociomx_pos.room.daos.Admins

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Admins.CfgNV

@Dao
interface CfgNVDAO {
    @Insert
    suspend fun insert(cfgNV: CfgNV)

    @Update
    suspend fun update(cfgNV: CfgNV)

    @Delete
    suspend fun delete(cfgNV: CfgNV)

    @Query("SELECT * FROM CfgNV")
    fun getAll(): LiveData<List<CfgNV>>

    @Query("SELECT * FROM CfgNV where (:idCfg is null or IdCfg=:idCfg) and (:idCfgNube is null or IdNube=:idCfgNube)")
    fun getByFilters(idCfg:Int?,idCfgNube:Int?): LiveData<CfgNV>

    @Transaction
    @Query("SELECT max(ConsecutivoFolioNV)+1 FROM CfgNV WHERE IdCfg=:idCfg")
    fun getFolioNV(idCfg: Short): LiveData<String>
}