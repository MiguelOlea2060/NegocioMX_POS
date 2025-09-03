package com.example.negociomx_pos.DAL

import android.util.Log
import com.example.negociomx_pos.BE.ConsultaPaso3Item
import com.example.negociomx_pos.Utils.ConexionSQLServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DALPaso3 {

    // ✅ CONSULTAR REGISTROS PASO3 POR FECHA
    suspend fun consultarPaso3PorFecha(fecha: String): List<ConsultaPaso3Item> = withContext(Dispatchers.IO) {
        val registros = mutableListOf<ConsultaPaso3Item>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            Log.d("DALPaso3", "🔍 Consultando registros Paso3 para fecha: $fecha")

            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) {
                Log.e("DALPaso3", "❌ No se pudo obtener conexión")
                return@withContext registros
            }

            val query = """
                SELECT DISTINCT
                    v.IdVehiculo, 
                    p.IdPaso3LogVehiculo, 
                    v.Vin, 
                    ISNULL(b.BL, '') as BL, 
                    v.IdMarca, 
                    m.Nombre as Marca, 
                    v.IdModelo, 
                    mo.Nombre as Modelo, 
                    ISNULL(v.Annio, 0) as Anio,
                    vc.IdColor, 
                    vc.IdColorInterior, 
                    ISNULL(c.Nombre, '') as ColorExterior, 
                    ISNULL(c1.Nombre, '') as ColorInterior, 
                    ISNULL(v.Motor, '') as NumeroMotor,
                    CONVERT(varchar, p.FechaAlta, 120) as FechaAlta,
                    ISNULL(p.NombreArchivoFoto, '') as NombreArchivoFoto,
                    ISNULL(p.Tienefoto, 0) as TieneFoto,
                    ISNULL(p.IdUsuarioNube, 0) as IdUsuarioNube
                FROM dbo.Paso3LogVehiculo p 
                INNER JOIN dbo.vehiculo v ON p.IdVehiculo = v.IdVehiculo 
                INNER JOIN dbo.MarcaAuto m ON v.IdMarca = m.IdMarcaAuto
                INNER JOIN dbo.Modelo mo ON v.IdMarca = mo.IdMarca AND v.IdModelo = mo.IdModelo
                LEFT JOIN dbo.VehiculoColor vc ON v.IdVehiculo = vc.IdVehiculo
                LEFT JOIN dbo.Color c ON vc.IdColor = c.IdColor
                LEFT JOIN dbo.Color c1 ON vc.IdColorInterior = c1.IdColor
                LEFT JOIN dbo.bl b ON v.IdBL = b.IdBL
                WHERE CONVERT(date, p.FechaAlta) = ?
                ORDER BY v.Vin
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            resultSet = statement.executeQuery()

            while (resultSet.next()) {
                val registro = ConsultaPaso3Item(
                    IdVehiculo = resultSet.getInt("IdVehiculo"),
                    IdPaso3LogVehiculo = resultSet.getInt("IdPaso3LogVehiculo"),
                    VIN = resultSet.getString("Vin") ?: "",
                    BL = resultSet.getString("BL") ?: "",
                    IdMarca = resultSet.getInt("IdMarca"),
                    Marca = resultSet.getString("Marca") ?: "",
                    IdModelo = resultSet.getInt("IdModelo"),
                    Modelo = resultSet.getString("Modelo") ?: "",
                    Anio = resultSet.getInt("Anio"),
                    ColorExterior = resultSet.getString("ColorExterior") ?: "",
                    ColorInterior = resultSet.getString("ColorInterior") ?: "",
                    NumeroMotor = resultSet.getString("NumeroMotor") ?: "",
                    FechaAlta = resultSet.getString("FechaAlta") ?: "",
                    NombreArchivoFoto = resultSet.getString("NombreArchivoFoto") ?: "",
                    TieneFoto = resultSet.getBoolean("TieneFoto"),
                    IdUsuarioNube = resultSet.getInt("IdUsuarioNube")
                )

                registros.add(registro)
            }

            Log.d("DALPaso3", "✅ Se encontraron ${registros.size} registros para la fecha $fecha")

        } catch (e: Exception) {
            Log.e("DALPaso3", "💥 Error consultando registros Paso3: ${e.message}")
            e.printStackTrace()
        } finally {
            try {
                resultSet?.close()
                statement?.close()
                conexion?.close()
            } catch (e: Exception) {
                Log.e("DALPaso3", "Error cerrando recursos: ${e.message}")
            }
        }

        return@withContext registros
    }

    // ✅ OBTENER ESTADÍSTICAS DEL DÍA
    suspend fun obtenerEstadisticasPaso3PorFecha(fecha: String): Map<String, Int> = withContext(Dispatchers.IO) {
        val estadisticas = mutableMapOf<String, Int>()
        var conexion: Connection? = null
        var statement: PreparedStatement? = null
        var resultSet: ResultSet? = null

        try {
            conexion = ConexionSQLServer.obtenerConexion()
            if (conexion == null) return@withContext estadisticas

            val query = """
                SELECT 
                    COUNT(DISTINCT p.IdVehiculo) as VehiculosUnicos,
                    COUNT(*) as TotalRegistros,
                    SUM(CASE WHEN p.Tienefoto = 1 THEN 1 ELSE 0 END) as TotalFotos
                FROM dbo.Paso3LogVehiculo p
                WHERE CONVERT(date, p.FechaAlta) = ?
            """.trimIndent()

            statement = conexion.prepareStatement(query)
            statement.setString(1, fecha)
            resultSet = statement.executeQuery()

            if (resultSet.next()) {
                estadisticas["VehiculosUnicos"] = resultSet.getInt("VehiculosUnicos")
                estadisticas["TotalRegistros"] = resultSet.getInt("TotalRegistros")
                estadisticas["TotalFotos"] = resultSet.getInt("TotalFotos")
            }

        } catch (e: Exception) {
            Log.e("DALPaso3", "Error obteniendo estadísticas: ${e.message}")
        } finally {
            resultSet?.close()
            statement?.close()
            conexion?.close()
        }

        return@withContext estadisticas
    }
}