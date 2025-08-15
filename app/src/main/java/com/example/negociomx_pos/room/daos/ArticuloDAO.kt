package com.example.negociomx_pos.room.daos

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.negociomx_pos.room.entities.Articulo

@Dao
interface ArticuloDAO {
    @Insert
    suspend fun insert(articulo: Articulo):Long

    @Insert
    suspend fun insertAll(articulos: List<Articulo>):Boolean
    {
        articulos.forEach{
            insert(it)
        }
        return true
    }

    @Update
    suspend fun update(articulo: Articulo)

    @Delete
    suspend fun delete(articulo: Articulo)

    @Query("SELECT * FROM Articulo where (:nombre='' or Nombre like :nombre)")
    fun getAll(nombre: String): LiveData<List<Articulo>>

    @Query("SELECT * FROM Articulo where IdArticulo=:idArticulo")
    fun getById(idArticulo:Int): LiveData<Articulo>

    @Query("SELECT * FROM Articulo where IdArticulo!=:idArticulo and Nombre=:nombre")
    fun getByIdDiferenteAndNombre(idArticulo: Int, nombre:String): LiveData<Articulo>

    @Query("SELECT * FROM Articulo where ((CodigoBarra=:codigoBarra and :codigoBarra!='') or Nombre=:nombre) limit 1")
    fun getByNombreOrCodigoBarra(nombre:String, codigoBarra:String): LiveData<Articulo>

    @Query("SELECT * FROM Articulo where IdNube=:idNube")
    fun getByIdNube(idNube:Int): LiveData<Articulo>

    @Query("SELECT Articulo.IdArticulo, Articulo.Nombre, Articulo.CodigoBarra, Articulo.PrecioVenta, " +
            "Articulo.Existencia, Categoria.Nombre as NombreCategoria, Articulo.IdUnidadMedida, Articulo.IdCategoria, " +
            "Articulo.Apartados, Articulo.IdStatus " +
            "FROM Articulo inner join Categoria on Articulo.IdCategoria=Categoria.IdCategoria  " +
            "where " +
            "(:idStatus is null or Articulo.IdStatus=:idStatus) and " +
            "(:likeNombre='' or Articulo.Nombre like :likeNombre or Articulo.CodigoBarra like :likeNombre or Articulo.Clave like :likeNombre)" +
            " order by Articulo.Nombre")
    suspend fun getAllByFilterClaveCodigoNombre(likeNombre: String, idStatus:Int?): List<ArticuloPOS>
}

data class ArticuloPOS(val IdArticulo:Int, val Nombre:String, val CodigoBarra:String, val PrecioVenta:Float, val Existencia:Float,
                       val IdCategoria:Short, val NombreCategoria:String, val Apartados:Float, val IdStatus:Int,
                       val IdUnidadMedida:Short)